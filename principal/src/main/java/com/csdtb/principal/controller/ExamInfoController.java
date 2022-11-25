package com.csdtb.principal.controller;

import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.exam.AddExamDTO;
import com.csdtb.common.dto.exam.UpdateExamDTO;
import com.csdtb.principal.service.ExamInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 考试信息表模块
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-17
 */
@Slf4j
@RestController
@RequestMapping("/exam-info")
public class ExamInfoController {

    @Resource
    private ExamInfoService examInfoService;

    @PostMapping("/addExam")
    public ResponseResult addExam(@RequestBody @Validated AddExamDTO dto){
        return examInfoService.addExam(dto);
    }

    @GetMapping("/selectExamByPage")
    public ResponseResult selectExamByPage(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "startTime", required = false) String startTime
    ){
        return examInfoService.selectExamByPage(page,pageSize,name,startTime);
    }

    @PutMapping("/updateExam")
    public ResponseResult updateExam(@RequestBody @Validated UpdateExamDTO dto){
        return examInfoService.updateExam(dto);
    }

    @DeleteMapping("/deleteExam/{id}")
    public ResponseResult deleteExam(@PathVariable("id") Integer id){
        return examInfoService.deleteExam(id);
    }

    @GetMapping("/guidelines")
    public ResponseResult guidelines(){
        return examInfoService.guidelines();
    }

    @GetMapping("/selectExamDetail")
    public ResponseResult selectExamDetail(@RequestParam(value = "id") String id){
        return examInfoService.selectExamDetail(id);
    }
}
