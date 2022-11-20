package com.csdtb.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-18
 **/
@Getter
@AllArgsConstructor
public enum UserEnum {

    ADMIN(0,"管理员"),
    EXAMINE(1,"考核员"),
    CONTROLLER(2,"管制员");

    private Integer role;
    private String description;
}
