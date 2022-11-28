package com.csdtb.principal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.examconfig.EditCalculateTaskDTO;
import com.csdtb.common.dto.examconfig.EditMonitorTaskDTO;
import com.csdtb.common.dto.examconfig.EditPrepareStageDTO;
import com.csdtb.common.vo.examconfig.SelectExamConfigVo;
import com.csdtb.database.entity.BallMonitorTaskEntity;
import com.csdtb.database.entity.CalculateTaskEntity;
import com.csdtb.database.entity.PrepareStageEntity;
import com.csdtb.database.mapper.BallMonitorTaskMapper;
import com.csdtb.database.mapper.CalculateTaskMapper;
import com.csdtb.database.mapper.PrepareStageMapper;
import com.csdtb.principal.exception.GlobalException;
import com.csdtb.principal.service.ExamConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 考核设置 服务实现类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
@Service
public class ExamConfigServiceImpl implements ExamConfigService {

    @Resource
    private BallMonitorTaskMapper ballMonitorTaskMapper;
    @Resource
    private CalculateTaskMapper calculateTaskMapper;
    @Resource
    private PrepareStageMapper prepareStageMapper;

    @Override
    @Transactional
    public ResponseResult editMonitorTask(List<EditMonitorTaskDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResponseResult.success();
        }
        //组装数据
        List<BallMonitorTaskEntity> list = new ArrayList<>(dtoList.size());
        dtoList.parallelStream().forEach(dto -> {
            BallMonitorTaskEntity entity = new BallMonitorTaskEntity();
            BeanUtils.copyProperties(dto, entity);
            list.add(entity);
        });
        //批量更新
        try {
            ballMonitorTaskMapper.updateListById(list);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new GlobalException(ResponseResult.error("更新失败"));
        }
        return ResponseResult.success();
    }

    @Override
    @Transactional
    public ResponseResult editCalculateTask(List<EditCalculateTaskDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResponseResult.success();
        }
        //组装数据
        List<CalculateTaskEntity> list = new ArrayList<>(dtoList.size());
        dtoList.parallelStream().forEach(dto -> {
            CalculateTaskEntity entity = new CalculateTaskEntity();
            BeanUtils.copyProperties(dto, entity);
            list.add(entity);
        });
        //批量更新
        try {
            calculateTaskMapper.updateListById(list);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new GlobalException(ResponseResult.error("更新失败"));
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult editPrepareStage(EditPrepareStageDTO dto) {
        PrepareStageEntity entity = new PrepareStageEntity();
        BeanUtils.copyProperties(dto, entity);
        try {
            prepareStageMapper.updateById(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseResult.error("更新失败"));
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult selectExamConfig() {
        //查询监控任务
        List<BallMonitorTaskEntity> monitorList = ballMonitorTaskMapper.selectList(new LambdaQueryWrapper<BallMonitorTaskEntity>()
                .orderByAsc(BallMonitorTaskEntity::getLevel));
        //查询计算任务
        List<CalculateTaskEntity> calculateList = calculateTaskMapper.selectList(new LambdaQueryWrapper<CalculateTaskEntity>()
                .orderByAsc(CalculateTaskEntity::getLevel));
        //查询准备阶段
        List<PrepareStageEntity> prepareStageList = prepareStageMapper.selectList(new LambdaQueryWrapper<PrepareStageEntity>().last("limit 1"));

        //组装数据
        List<SelectExamConfigVo.BallMonitorTaskVo> monitorTaskVoList = new ArrayList<>(monitorList.size());
        monitorList.parallelStream().forEach(item -> {
            SelectExamConfigVo.BallMonitorTaskVo monitorTaskVo = new SelectExamConfigVo.BallMonitorTaskVo();
            BeanUtils.copyProperties(item, monitorTaskVo);
            monitorTaskVoList.add(monitorTaskVo);
        });

        List<SelectExamConfigVo.CalculateTaskVo> calculateTaskVoList = new ArrayList<>(calculateList.size());
        calculateList.parallelStream().forEach(item -> {
            SelectExamConfigVo.CalculateTaskVo calculateTaskVo = new SelectExamConfigVo.CalculateTaskVo();
            BeanUtils.copyProperties(item, calculateTaskVo);
            calculateTaskVoList.add(calculateTaskVo);
        });

        PrepareStageEntity prepareStageEntity = prepareStageList.get(0);
        SelectExamConfigVo.PrepareStageVo prepareStageVo = new SelectExamConfigVo.PrepareStageVo();
        BeanUtils.copyProperties(prepareStageEntity, prepareStageVo);

        SelectExamConfigVo vo = SelectExamConfigVo.builder()
                .monitorTaskVoList(monitorTaskVoList
                        .stream()
                        .sorted(Comparator.comparingInt(SelectExamConfigVo.BallMonitorTaskVo::getLevel))
                        .collect(Collectors.toList()))
                .calculateTaskVoList(calculateTaskVoList
                        .stream()
                        .sorted(Comparator.comparingInt(SelectExamConfigVo.CalculateTaskVo::getLevel))
                        .collect(Collectors.toList()))
                .prepareStageVo(prepareStageVo)
                .build();

        return ResponseResult.success(vo);
    }
}
