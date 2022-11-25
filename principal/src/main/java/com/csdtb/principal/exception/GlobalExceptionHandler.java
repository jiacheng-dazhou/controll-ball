package com.csdtb.principal.exception;

import com.csdtb.common.ResponseResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-25
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseResult exceptionHandler(Exception e) {
        if (e instanceof GlobalException) {
            GlobalException exception = (GlobalException) e;
            return exception.getResponseResult();
        } else if (e instanceof BindException) {
            BindException exception = (BindException) e;
            return ResponseResult.error("参数校验异常:" + exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        }

        return ResponseResult.error("系统异常");
    }
}
