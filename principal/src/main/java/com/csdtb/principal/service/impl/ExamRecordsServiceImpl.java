package com.csdtb.principal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.common.constant.UserEnum;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.common.vo.PageData;
import com.csdtb.common.vo.records.ExamRecordDetailVo;
import com.csdtb.common.vo.records.ExamRecordsPageVo;
import com.csdtb.database.entity.*;
import com.csdtb.database.mapper.ExamInfoMapper;
import com.csdtb.database.mapper.ExamRecordsDetailMapper;
import com.csdtb.database.mapper.ExamRecordsMapper;
import com.csdtb.database.mapper.UserLoginMapper;
import com.csdtb.principal.service.ExamRecordsService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试记录表 服务实现类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
@Service
public class ExamRecordsServiceImpl implements ExamRecordsService {

    @Resource
    private ExamRecordsMapper examRecordsMapper;

    @Resource
    private ExamRecordsDetailMapper examRecordsDetailMapper;

    @Resource
    private UserLoginMapper userLoginMapper;

    @Resource
    private ExamInfoMapper examInfoMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public ResponseResult selectRecordsByPage(Integer page, Integer pageSize, String examName, String userName, String token) {
        //根据角色条件过滤
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);

        Page<ExamRecordPageEntity> pageInfo = examRecordsMapper.selectPageByCondition(new Page(page, pageSize),
                user.getRole().equals(UserEnum.CONTROLLER.getRole()) ? user.getId() : null, examName, userName);

        List<ExamRecordPageEntity> entityList = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(entityList)) {
            return ResponseResult.success(PageData.initPageVo(pageInfo));
        }

        return ResponseResult.success(PageData.initPageVo(pageInfo, backVo(entityList)));
    }

    @Override
    public ResponseResult selectRecordDetail(Integer id, String token) {
        //获取当前登录人信息
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);

        //根据考核记录id，获取考核、用户信息
        ExamRecordsEntity recordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                .eq(ExamRecordsEntity::getId, id));

        if (recordsEntity == null) {
            return ResponseResult.error("暂未查询到当前考核记录");
        }

        //获取考试记录信息
        ExamInfoEntity examEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, recordsEntity.getExamId()));
        if (examEntity == null) {
            return ResponseResult.error("暂未查询到当前考核信息");
        }
        //获取用户信息
        UserLoginEntity userEntity = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getId, recordsEntity.getUserId()));
        if (userEntity == null) {
            return ResponseResult.error("暂未查询到当前考生信息");
        }
        //获取考核记录信息
        List<ExamRecordsDetailEntity> detailEntityList = examRecordsDetailMapper.selectList(new LambdaQueryWrapper<ExamRecordsDetailEntity>()
                .eq(ExamRecordsDetailEntity::getExamRecordsId, recordsEntity.getId()));

        //组装数据
        ExamRecordDetailVo examRecordDetailVo = new ExamRecordDetailVo();
        //基础信息
        backBaseInfo(examRecordDetailVo, examEntity, userEntity);
        //监控任务
        backMonitorInfo(examRecordDetailVo, examEntity, detailEntityList, user);
        //计算任务
        backCalculateInfo(examRecordDetailVo, examEntity, detailEntityList, user);

        return ResponseResult.success(examRecordDetailVo);
    }

    private void backCalculateInfo(ExamRecordDetailVo vo, ExamInfoEntity examEntity, List<ExamRecordsDetailEntity> detailEntityList, UserDTO user) {
        ExamRecordDetailVo.CalculateVo calculateVo = new ExamRecordDetailVo.CalculateVo();
        calculateVo.setLevel(examEntity.getCalculateLevel());

        List<ExamRecordsDetailEntity> calculateList = detailEntityList.stream()
                .filter(item -> item.getType().equals(ExamInfoEnum.CALCULATE.getStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(calculateList)) {
            calculateVo.setTitleCount(0);
            vo.setCalculateVo(calculateVo);
            return;
        }
        calculateVo.setTitleCount(calculateList.size());

        //正确率
        long calculateRight = calculateList.stream().filter(item -> item.getIsCorrect().equals(Boolean.TRUE)).count();
        calculateVo.setCalculateRightRate(BigDecimal.valueOf(calculateRight * 100 / calculateList.size())
                .setScale(2, RoundingMode.HALF_UP) + "%");

        //管制员不看详情
        if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
            vo.setCalculateVo(calculateVo);
            return;
        }

        List<ExamRecordDetailVo.CalculateVo.CalculateDetailVo> calculateDetailVoList = new ArrayList<>(calculateList.size());
        calculateList.parallelStream().forEach(entity -> {
            ExamRecordDetailVo.CalculateVo.CalculateDetailVo detailVo = new ExamRecordDetailVo.CalculateVo.CalculateDetailVo();
            BeanUtils.copyProperties(entity, detailVo);
            calculateDetailVoList.add(detailVo);
        });

        //排序
        calculateVo.setCalculateDetailVoList(calculateDetailVoList.stream()
                .sorted(Comparator.comparing(ExamRecordDetailVo.CalculateVo.CalculateDetailVo::getStartTime))
                .collect(Collectors.toList()));

        vo.setCalculateVo(calculateVo);
    }

    private void backMonitorInfo(ExamRecordDetailVo vo, ExamInfoEntity examEntity, List<ExamRecordsDetailEntity> detailEntityList, UserDTO user) {
        ExamRecordDetailVo.MonitorVo monitorVo = new ExamRecordDetailVo.MonitorVo();
        monitorVo.setMonitorTime(vo.getNowTime());
        monitorVo.setLevel(examEntity.getMonitorLevel());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            monitorVo.setBoundsTotal(0);
            monitorVo.setCrashTotal(0);
            monitorVo.setTurnbackTotal(0);
        } else {
            Map<Integer, List<ExamRecordsDetailEntity>> detailListMap = detailEntityList.stream()
                    .collect(Collectors.groupingBy(ExamRecordsDetailEntity::getType));
            List<ExamRecordsDetailEntity> turnbackList = detailListMap.get(ExamInfoEnum.TURNBACK.getStatus());
            List<ExamRecordsDetailEntity> boundsList = detailListMap.get(ExamInfoEnum.BOUNDS.getStatus());
            List<ExamRecordsDetailEntity> crashList = detailListMap.get(ExamInfoEnum.CRASH.getStatus());

            //监控统计
            monitorVo.setTurnbackTotal(turnbackList.size());
            monitorVo.setBoundsTotal(boundsList.size());
            monitorVo.setCrashTotal(crashList.size());

            long turnBackRight = turnbackList.stream()
                    .filter(item -> item.getIsCorrect().equals(Boolean.TRUE))
                    .count();
            monitorVo.setTurnbackRightRate(CollectionUtils.isEmpty(turnbackList) ?
                    "0.00%" : BigDecimal.valueOf(turnBackRight * 100 / turnbackList.size())
                    .setScale(2, RoundingMode.HALF_UP) + "%");
            long boundsRight = boundsList.stream()
                    .filter(item -> item.getIsCorrect().equals(Boolean.TRUE))
                    .count();
            monitorVo.setBoundsRightRate(CollectionUtils.isEmpty(boundsList) ?
                    "0.00%" : BigDecimal.valueOf(boundsRight * 100 / boundsList.size())
                    .setScale(2, RoundingMode.HALF_UP) + "%");
            long crashRight = crashList.stream()
                    .filter(item -> item.getIsCorrect().equals(Boolean.TRUE))
                    .count();
            monitorVo.setCrashRightRate(CollectionUtils.isEmpty(crashList) ?
                    "0.00%" : BigDecimal.valueOf(crashRight * 100 / crashList.size())
                    .setScale(2, RoundingMode.HALF_UP) + "%");

            //管制员不看详情
            if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
                vo.setMonitorVo(monitorVo);
                return;
            }

            List<ExamRecordsDetailEntity> monitorDetailList = detailEntityList.stream().filter(item -> !item.getType().equals(ExamInfoEnum.CALCULATE.getStatus())).collect(Collectors.toList());
            List<ExamRecordDetailVo.MonitorVo.MonitorDetailVo> detailVoList = new ArrayList<>(monitorDetailList.size());
            monitorDetailList.parallelStream().forEach(detailEntity -> {
                ExamRecordDetailVo.MonitorVo.MonitorDetailVo monitorDetailVo = new ExamRecordDetailVo.MonitorVo.MonitorDetailVo();
                BeanUtils.copyProperties(detailEntity, monitorDetailVo);
                detailVoList.add(monitorDetailVo);
            });

            //排序
            monitorVo.setMonitorDetailVoList(detailVoList.stream()
                    .sorted(Comparator.comparing(ExamRecordDetailVo.MonitorVo.MonitorDetailVo::getStartTime))
                    .collect(Collectors.toList()));

            vo.setMonitorVo(monitorVo);
        }
    }

    private void backBaseInfo(ExamRecordDetailVo vo, ExamInfoEntity examEntity, UserLoginEntity userEntity) {
        BeanUtils.copyProperties(userEntity, vo);
        vo.setStartTime(examEntity.getStartTime());
        //考核总时长
        vo.setTotalTime(toLocalTime(examEntity.getStartTime(), examEntity.getEndTime()));
        //考核时长
        if (examEntity.getStatus().equals(ExamInfoEnum.IN_PROGRESS.getStatus())) {
            //考试进行中
            vo.setNowTime(toLocalTime(examEntity.getStartTime(), LocalDateTime.now().toString()));
        } else {
            //考试结束
            vo.setNowTime(vo.getTotalTime());
        }
        //休息次数
        Duration duration = Duration.between(LocalTime.parse("00:00:00"), LocalTime.parse(vo.getNowTime()));
        long breakNumber = duration.toMinutes() /
                (examEntity.getMonitorDuration() + examEntity.getMonitorSleepDuration());
        vo.setBreakNumber((int) breakNumber);
        //休息总时长
        Duration breakTime = Duration.ofMinutes(breakNumber * examEntity.getMonitorSleepDuration());
        vo.setBreakTotalTime(toLocalTime(breakTime));
    }

    /**
     * 计算时间差值（格式：HH:mm:ss）
     *
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 返回（格式：HH:mm:ss）
     */
    private String toLocalTime(String startTime, String endTime) {
        LocalDateTime examStartTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime examEndTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Duration duration = Duration.between(examStartTime, examEndTime);
        int hours = (int) duration.toHours();
        int minute = (int) (duration.toMinutes() - duration.toHours() * 60);
        int seconds = (int) (duration.getSeconds() - duration.toMinutes() * 60);
        return LocalTime.of(hours, minute, seconds).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String toLocalTime(Duration duration) {
        int hours = (int) duration.toHours();
        int minute = (int) (duration.toMinutes() - duration.toHours() * 60);
        int seconds = (int) (duration.getSeconds() - duration.toMinutes() * 60);
        return LocalTime.of(hours, minute, seconds).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private List<ExamRecordsPageVo> backVo(List<ExamRecordPageEntity> entityList) {
        List<ExamRecordsPageVo> voList = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> {
            ExamRecordsPageVo vo = new ExamRecordsPageVo();
            BeanUtils.copyProperties(entity, vo);
            voList.add(vo);
        });
        return voList;
    }
}
