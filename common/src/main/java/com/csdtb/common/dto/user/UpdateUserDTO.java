package com.csdtb.common.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-09
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    /**
     * id
     */
    @NotNull
    private Long id;
    /**
     * 密码
     */
    private String password;

    /**
     * 用户姓名
     */
    private String username;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别(0-男,1-女)
     */
    private Boolean sex;

    /**
     * 管制单位
     */
    private String controlUnit;

    /**
     * 职称
     */
    private String positionalTitle;

    /**
     * 职务
     */
    private String positionalJob;

    /**
     * 所属角色(0-管理员，1-考核员，2-管制员)
     */
    private Integer role;
}
