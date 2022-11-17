package com.csdtb.principal.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.database.mapper.ExamInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            try {
                examInfoMapper.updateStatusByIds(examStatusList.stream()
                        .map(ExamInfoEntity::getId)
                        .collect(Collectors.toList()), ExamInfoEnum.BEGIN_IN_A_MINUTE.getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
