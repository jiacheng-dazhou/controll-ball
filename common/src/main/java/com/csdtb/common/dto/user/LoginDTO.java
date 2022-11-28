package com.csdtb.common.dto.user;

import com.csdtb.common.annotation.IsPassword;
import com.csdtb.common.annotation.IsPhone;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-11
 **/
@Data
public class LoginDTO {
    @NotNull(message = "账号不能为空")
    @IsPhone
    private Long account;
    @NotBlank(message = "密码不能为空")
    @IsPassword
    private String password;
}
