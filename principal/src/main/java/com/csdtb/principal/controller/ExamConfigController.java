package com.csdtb.principal.controller;

import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.examconfig.EditCalculateTaskDTO;
import com.csdtb.common.dto.examconfig.EditMonitorTaskDTO;
import com.csdtb.common.dto.examconfig.EditPrepareStageDTO;
import com.csdtb.principal.service.ExamConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 考核设置模块
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
@Slf4j
@RestController
@RequestMapping("/exam-config")
public class ExamConfigController {

    @Resource
    private ExamConfigService examConfigService;

    @PostMapping("/editMonitorTask")
    public ResponseResult editMonitorTask(@RequestBody List<EditMonitorTaskDTO> dtoList) {
        return examConfigService.editMonitorTask(dtoList);
    }

    @PostMapping("/editCalculateTask")
    public ResponseResult editCalculateTask(@RequestBody List<EditCalculateTaskDTO> dtoList) {
        return examConfigService.editCalculateTask(dtoList);
    }

    @PostMapping("/editPrepareStage")
    public ResponseResult editPrepareStage(@RequestBody @Validated EditPrepareStageDTO dto) {
        return examConfigService.editPrepareStage(dto);
    }

    @GetMapping("/selectExamConfig")
    public ResponseResult selectExamConfig() {
        return examConfigService.selectExamConfig();
    }
}
