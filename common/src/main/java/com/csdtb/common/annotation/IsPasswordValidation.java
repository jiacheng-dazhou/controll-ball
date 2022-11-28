package com.csdtb.common.annotation;

import cn.hutool.core.util.ReUtil;
import com.csdtb.common.constant.RegexType;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-28
 **/
public class IsPasswordValidation implements ConstraintValidator<IsPassword,String> {

    private boolean required = false;
    @Override

    public void initialize(IsPassword isPassword) {
        this.required = isPassword.required();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if(required){
            return ReUtil.isMatch(RegexType.PASSWORD.getType(), password);
        }else{
            if (StringUtils.isEmpty(password)) {
                return true;
            }else{
                return ReUtil.isMatch(RegexType.PASSWORD.getType(), password);
            }
        }
    }
}
