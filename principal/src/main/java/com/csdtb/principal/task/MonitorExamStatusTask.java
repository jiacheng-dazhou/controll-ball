package com.csdtb.principal.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.constant.CalculationQuestionsEnum;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.database.entity.CalculateTaskEntity;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.database.mapper.CalculateTaskMapper;
import com.csdtb.database.mapper.ExamInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
//@Component
@Slf4j
public class MonitorExamStatusTask implements ApplicationRunner {

    @Resource
    private ExamInfoMapper examInfoMapper;

    @Resource
    private CalculateTaskMapper calculateTaskMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //开启异步线程监控考核，修改考核状态(1-未开考，2-即将开始——开考前五分钟)，3-进行中——考试中，4-已完成——考试结束)
        CompletableFuture.runAsync(()->{
            while (true){
                //获取当前时间
                LocalDateTime now = LocalDateTime.now();

                //分组获取需要变更考核状态的考试集合
                List<ExamInfoEntity> examList = examInfoMapper.selectList(new LambdaQueryWrapper<ExamInfoEntity>()
                        .in(ExamInfoEntity::getStatus,
                                ExamInfoEnum.NO_EXAMINATION.getStatus(),
                                ExamInfoEnum.BEGIN_IN_A_MINUTE.getStatus(),
                                ExamInfoEnum.IN_PROGRESS.getStatus())
                        .between(ExamInfoEntity::getStartTime,(now.toLocalDate()+" 00:00:00"),(now.toLocalDate()+" 23:59:59")));
                if (!CollectionUtils.isEmpty(examList)) {
                    Map<Integer, List<ExamInfoEntity>> examStatusMap = examList.stream().collect(Collectors.groupingBy(ExamInfoEntity::getStatus));
                    for (int status : examStatusMap.keySet()) {
                        List<ExamInfoEntity> examStatusList = examStatusMap.get(status);
                        switch (status){
                            case 1:
                                //未开考
                                validateNoExamination(examStatusList,now);
                                break;
                            case 2:
                                //即将开考
                                validateBeginInAMinute(examStatusList,now);
                                break;
                            case 3:
                                //进行中
                                validateInProgress(examStatusList,now);
                                break;
                            default:
                                throw new NoSuchElementException("暂不支持此考核类型");
                        }
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Transactional
    protected void validateInProgress(List<ExamInfoEntity> examList, LocalDateTime now) {
        List<ExamInfoEntity> examStatusList = examList.stream().filter(exam -> {
            String endTime = exam.getEndTime();
            LocalDateTime examEndTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            //考核结束时间小于当前时间，状态变更已完成
            if (examEndTime.isBefore(now)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(examStatusList)) {
            try {
                examInfoMapper.updateStatusByIds(examStatusList.stream()
                        .map(ExamInfoEntity::getId)
                        .collect(Collectors.toList()),ExamInfoEnum.COMPLETED.getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional
    protected void validateBeginInAMinute(List<ExamInfoEntity> examList, LocalDateTime now) {
        List<ExamInfoEntity> examStatusList = examList.stream().filter(exam -> {
            String startTime = exam.getStartTime();
            LocalDateTime examStartTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            //开考时间小于当前时间，变更状态为进行中
            if (examStartTime.isBefore(now)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(examStatusList)) {
            try {
                examInfoMapper.updateStatusByIds(examStatusList.stream()
                        .map(ExamInfoEntity::getId)
                        .collect(Collectors.toList()),ExamInfoEnum.IN_PROGRESS.getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional
    protected void validateNoExamination(List<ExamInfoEntity> examList, LocalDateTime now) {
        List<ExamInfoEntity> examStatusList = examList.stream().filter(exam -> {
            String startTime = exam.getStartTime();
            LocalDateTime examStartTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            //开考时间小于五分钟，变更状态为即将开考
            if (examStartTime.isBefore(now.plusMinutes(5L))) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(examStatusList)) {
            examStatusList.parallelStream().forEach(exam->{
                //生成考试题库
                exam.setQuestions(showCalculationQuestions(exam));
            });
            try {
                examInfoMapper.updateQuestionsByIds(examStatusList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String showCalculationQuestions(ExamInfoEntity examInfoEntity) {
        //获取当前难度的题库信息
        CalculateTaskEntity calculateTaskEntity = calculateTaskMapper.selectOne(new LambdaQueryWrapper<CalculateTaskEntity>()
                .eq(CalculateTaskEntity::getLevel, examInfoEntity.getCalculateLevel()));
        //根据考试计算固定数量，或者固定频率，计算出出题量
        int totalQuestions;
        if (examInfoEntity.getCalculateNumber() != null && examInfoEntity.getCalculateNumber() != 0) {
            //固定数量
            totalQuestions = examInfoEntity.getCalculateNumber();
        } else if (examInfoEntity.getCalculateRate() != null && examInfoEntity.getCalculateRate() != 0) {
            //固定频率
            LocalDateTime startTime = LocalDateTime.parse(examInfoEntity.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTime = LocalDateTime.parse(examInfoEntity.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            long startMill = startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            long endMill = endTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            totalQuestions = (BigDecimal.valueOf(endMill)
                    .subtract(BigDecimal.valueOf(startMill)))
                    .divide(BigDecimal.valueOf(1000L))
                    .divide(BigDecimal.valueOf(examInfoEntity.getCalculateRate()))
                    .setScale(0,BigDecimal.ROUND_UP)
                    .intValue();
        } else {
            //默认给60道题
            totalQuestions = 60;
        }
        //根据总题量，以及出题比例生成题库
        int easyQuestions = BigDecimal.valueOf(totalQuestions * calculateTaskEntity.getCalculateLevel1() * 0.01)
                .setScale(0,BigDecimal.ROUND_UP).intValue();
        int generalQuestions = BigDecimal.valueOf(totalQuestions * calculateTaskEntity.getCalculateLevel2() * 0.01)
                .setScale(0,BigDecimal.ROUND_UP).intValue();
        int difficultyQuestions = BigDecimal.valueOf(totalQuestions * calculateTaskEntity.getCalculateLevel3() * 0.01)
                .setScale(0,BigDecimal.ROUND_UP).intValue();

        List<String> questions = new ArrayList<>(easyQuestions + generalQuestions + difficultyQuestions);
        questions.addAll(CalculationQuestionsEnum.getQuestionsByLevel(CalculationQuestionsEnum.EASY_LEVEL.getLevel(), easyQuestions));
        questions.addAll(CalculationQuestionsEnum.getQuestionsByLevel(CalculationQuestionsEnum.GENERAL_LEVEL.getLevel(), generalQuestions));
        questions.addAll(CalculationQuestionsEnum.getQuestionsByLevel(CalculationQuestionsEnum.DIFFICULTY_LEVEL.getLevel(), difficultyQuestions));

        //打乱题库生成顺序
        List<String> randomQuestions = new ArrayList<>(questions.size());
        questions.parallelStream().forEach(question->{
            randomQuestions.add(question);
        });

        StringBuilder builder = new StringBuilder();
        randomQuestions.forEach(question->{
            builder.append(question + ",");
        });

        return builder.substring(0, builder.lastIndexOf(","));
    }
}
