package com.csdtb.principal.controller;

import com.csdtb.common.ResponseResult;
import com.csdtb.principal.service.ExamRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 考试记录表模块
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
@Slf4j
@RestController
@RequestMapping("/exam-records")
public class ExamRecordsController {

    @Resource
    private ExamRecordsService examRecordsService;

    @GetMapping("/selectRecordsByPage")
    public ResponseResult selectRecordsByPage(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "examName", required = false) String examName,
            @RequestParam(value = "userName", required = false) String userName,
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        return examRecordsService.selectRecordsByPage(page,pageSize,examName,userName,token);
    }

    @GetMapping("/selectRecordDetail")
    public ResponseResult selectRecordDetail(@RequestParam("id")Integer id,HttpServletRequest request){
        String token = request.getHeader("Authorization");
        return examRecordsService.selectRecordDetail(id,token);
    }

    @GetMapping("/exportRecordDetail")
    public void exportRecordDetail(@RequestParam("id")Integer id, HttpServletRequest request, HttpServletResponse response){
        String token = request.getHeader("Authorization");
        examRecordsService.exportRecordDetail(id,token,response);
    }
}
