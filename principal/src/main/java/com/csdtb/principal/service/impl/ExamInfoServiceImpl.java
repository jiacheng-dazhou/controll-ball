package com.csdtb.principal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.common.dto.exam.AddExamDTO;
import com.csdtb.common.dto.exam.UpdateExamDTO;
import com.csdtb.common.vo.PageData;
import com.csdtb.common.vo.exam.ExamDetailVo;
import com.csdtb.common.vo.exam.ExamGuidelinesVo;
import com.csdtb.common.vo.exam.ExamPageVo;
import com.csdtb.database.entity.BallMonitorTaskEntity;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.database.entity.PrepareStageEntity;
import com.csdtb.database.mapper.BallMonitorTaskMapper;
import com.csdtb.database.mapper.ExamInfoMapper;
import com.csdtb.database.mapper.PrepareStageMapper;
import com.csdtb.principal.exception.GlobalException;
import com.csdtb.principal.service.ExamInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试信息表 服务实现类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-17
 */
@Service
public class ExamInfoServiceImpl implements ExamInfoService {

    @Resource
    private ExamInfoMapper examInfoMapper;

    @Resource
    private PrepareStageMapper prepareStageMapper;

    @Resource
    private BallMonitorTaskMapper ballMonitorTaskMapper;

    @Override
    public ResponseResult addExam(AddExamDTO dto) {
        if (dto.getCalculateNumber() == null && dto.getCalculateRate() == null) {
            throw new GlobalException(ResponseResult.error("计算固定数量、固定频率必须包含其中至少一个"));
        }
        //考试开考时间必须大于等于当前时间+5分钟以后，结束时间需大于开考时间
        String startTime = dto.getStartTime();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nowStartTime = now.plusMinutes(5L);
        LocalDateTime examStartTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime examEndTime = examStartTime.plusMinutes(dto.getExamDuration());

        if (nowStartTime.isAfter(examStartTime)) {
            throw new GlobalException(ResponseResult.error("开考时间需大于等于"+nowStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"以后"));
        }

        //新增
        ExamInfoEntity examInfoEntity = new ExamInfoEntity();
        BeanUtils.copyProperties(dto,examInfoEntity);
        examInfoEntity.setEndTime(examEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        try {
            examInfoMapper.insert(examInfoEntity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseResult.error("新增考试异常"));
        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult selectExamByPage(Integer page, Integer pageSize, String name, String startTime) {
        //条件过滤
        LambdaQueryWrapper<ExamInfoEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(ExamInfoEntity::getName,name);
        }
        if (StringUtils.hasText(startTime)) {
            String examStartDayTime = startTime + " 00:00:00";
            String examEndDayTime = startTime + " 23:59:59";
            //查询当天的所有考试
            wrapper.between(ExamInfoEntity::getStartTime,examStartDayTime,examEndDayTime);
        }
        wrapper.orderByDesc(ExamInfoEntity::getStartTime);

        //分页查询
        Page<ExamInfoEntity> pageInfo = examInfoMapper.selectPage(new Page(page, pageSize), wrapper);
        if (CollectionUtils.isEmpty(pageInfo.getRecords())) {
            return ResponseResult.success(PageData.initPageVo(pageInfo));
        }

        //组装vo
        return ResponseResult.success(PageData.initPageVo(pageInfo,backVo(pageInfo.getRecords())));
    }

    @Override
    public ResponseResult updateExam(UpdateExamDTO dto) {
        //考试开考时间必须大于等于当前时间+5分钟以后，结束时间需大于开考时间
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nowStartTime = now.plusMinutes(5L);
        LocalDateTime examStartTime = LocalDateTime.parse(dto.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime examEndTime = examStartTime.plusMinutes(dto.getExamDuration());

        if (nowStartTime.isAfter(examStartTime)) {
            throw new GlobalException(ResponseResult.error("开考时间需大于等于"+nowStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"以后"));
        }

        if (examStartTime.isAfter(examEndTime)) {
            throw new GlobalException(ResponseResult.error("开考结束时间需大于开考开始时间"));
        }

        //如果当前修改的考试状态已经变更，则不能再修改
        ExamInfoEntity examInfoEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, dto.getId()));

        if (examInfoEntity == null) {
            throw new GlobalException(ResponseResult.error("考核已删除"));
        }

        if (examInfoEntity.getStatus() != ExamInfoEnum.NO_EXAMINATION.getStatus()) {
            throw new GlobalException(ResponseResult.error("考核即将开始或已经进行或已完成，已不能修改"));
        }

        ExamInfoEntity entity = new ExamInfoEntity();
        BeanUtils.copyProperties(dto,entity);
        entity.setEndTime(examEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        try {
            examInfoMapper.updateById(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseResult.error("修改考核异常"));
        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult deleteExam(Integer id) {

        try {
            examInfoMapper.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseResult.error("删除考核异常"));
        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult guidelines() {

        List<PrepareStageEntity> prepareStageList = prepareStageMapper.selectList(new LambdaQueryWrapper<PrepareStageEntity>().last("limit 1"));
        if (CollectionUtils.isEmpty(prepareStageList)) {
            return ResponseResult.success();
        }

        PrepareStageEntity entity = prepareStageList.get(0);
        ExamGuidelinesVo vo = new ExamGuidelinesVo();
        BeanUtils.copyProperties(entity,vo);

        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult selectExamDetail(String id) {
        ExamInfoEntity examInfoEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, id));

        if (examInfoEntity == null) {
            throw new GlobalException(ResponseResult.error("暂未获取到当前考核详情"));
        }

        ExamDetailVo vo = new ExamDetailVo();
        BeanUtils.copyProperties(examInfoEntity,vo);
        LocalDateTime startTime = LocalDateTime.parse(examInfoEntity.getStartTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime endTime = LocalDateTime.parse(examInfoEntity.getEndTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Duration duration = Duration.between(startTime, endTime);
        vo.setExamDuration(duration.toMinutes());

        //查询监控任务
        BallMonitorTaskEntity monitorTaskEntity = ballMonitorTaskMapper.selectOne(new LambdaQueryWrapper<BallMonitorTaskEntity>()
                .eq(BallMonitorTaskEntity::getLevel,vo.getMonitorLevel()));

        if (monitorTaskEntity != null) {
            ExamDetailVo.BallMonitorTaskVo monitorTaskVo = new ExamDetailVo.BallMonitorTaskVo();
            BeanUtils.copyProperties(monitorTaskEntity,monitorTaskVo);
            vo.setBallMonitorTaskVo(monitorTaskVo);
        }

        return ResponseResult.success(vo);
    }

    private List<ExamPageVo> backVo(List<ExamInfoEntity> entityList) {
        List<ExamPageVo> voList = new ArrayList<>(entityList.size());
        entityList.parallelStream().forEach(entity->{
            ExamPageVo vo = new ExamPageVo();
            BeanUtils.copyProperties(entity,vo);
            voList.add(vo);
        });
        return voList.stream()
                .sorted(Comparator.comparing(ExamPageVo::getStartTime).reversed())
                .collect(Collectors.toList());
    }
}
