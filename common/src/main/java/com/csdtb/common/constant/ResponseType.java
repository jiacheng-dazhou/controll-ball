package com.csdtb.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公共返回类型枚举
 */
@Getter
@AllArgsConstructor
public enum ResponseType {

    /**
     * 根据项目修改选用合适的错误码
     */

    //公用模块
    SUCCESS(0,"成功"),
    REQUEST_FAIL(-1, "失败");


    private Integer code;
    private String message;
}
