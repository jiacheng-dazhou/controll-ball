package com.csdtb.principal.exception;

import com.csdtb.common.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-25
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException{

    private ResponseResult responseResult;
}
