package com.csdtb.common;

import com.csdtb.common.constant.ResponseType;
import lombok.Data;

/**
 * 公共返回实体
 * @param <T>
 */
@Data
public class ResponseResult<T> {
    public static final Integer OK_CODE = 0;

    private Integer code;
    private String message;
    private T data;

    /**
     * 正常数据返回
     *
     * @param data 数据对象
     * @return ResponseResult
     */
    public static <T> ResponseResult success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setData(data);
        result.setCode(ResponseType.SUCCESS.getCode());
        return result;
    }
    /**
     * 正常数据返回
     *
     * @return ResponseResult
     */
    public static ResponseResult success() {
        ResponseResult result = new ResponseResult<>();
        result.setCode(ResponseType.SUCCESS.getCode());
        result.setMessage(ResponseType.SUCCESS.getMessage());
        return result;
    }

    /**
     * 正常数据返回
     *
     * @param data    数据对象
     * @param message 自定义成功信息
     * @return ResponseResult
     */
    public static ResponseResult success(Object data, String message) {
        ResponseResult<Object> result = new ResponseResult<>();
        result.setData(data);
        result.setCode(ResponseType.SUCCESS.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 错误数据返回
     *
     * @param responseType 错误数据
     * @return ResponseResult
     */
    public static ResponseResult error(ResponseType responseType) {
        ResponseResult<Object> result = new ResponseResult<>();
        result.setCode(responseType.getCode());
        result.setMessage(responseType.getMessage());
        return result;
    }

    /**
     * 错误数据返回
     *
     * @param message 错误信息
     * @return ResponseResult
     */
    public static ResponseResult error(String message) {
        ResponseResult<Object> result = new ResponseResult<>();
        result.setCode(ResponseType.REQUEST_FAIL.getCode());
        result.setMessage(message);
        return result;
    }

}
