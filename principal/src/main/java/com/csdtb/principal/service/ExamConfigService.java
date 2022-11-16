package com.csdtb.principal.service;

import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.examconfig.EditCalculateTaskDTO;
import com.csdtb.common.dto.examconfig.EditMonitorTaskDTO;
import com.csdtb.common.dto.examconfig.EditPrepareStageDTO;

import java.util.List;

/**
 * <p>
 * 考核设置 服务类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
public interface ExamConfigService {

    /**
     * 编辑监控任务
     * @param dtoList
     * @return
     */
    ResponseResult editMonitorTask(List<EditMonitorTaskDTO> dtoList);

    /**
     * 编辑计算任务
     * @param dtoList
     * @return
     */
    ResponseResult editCalculateTask(List<EditCalculateTaskDTO> dtoList);

    /**
     * 编辑准备阶段
     * @param dto
     * @return
     */
    ResponseResult editPrepareStage(EditPrepareStageDTO dto);

    /**
     * 查询考核配置
     * @return
     */
    ResponseResult selectExamConfig();
}
