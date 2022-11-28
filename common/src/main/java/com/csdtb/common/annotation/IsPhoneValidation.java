package com.csdtb.common.annotation;

import cn.hutool.core.util.ReUtil;
import com.csdtb.common.constant.RegexType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-28
 **/
public class IsPhoneValidation implements ConstraintValidator<IsPhone,Long> {

    private boolean required = false;

    @Override
    public void initialize(IsPhone isPhone) {
        this.required = isPhone.required();
    }

    @Override
    public boolean isValid(Long phone, ConstraintValidatorContext constraintValidatorContext) {
        if(required){
            return checkAccount(phone);
        }else {
            if (phone == null) {
                return true;
            }else{
                return checkAccount(phone);
            }
        }
    }

    private boolean checkAccount(Long account) {
        String accountStr = String.valueOf(account);
        boolean flag1 = ReUtil.isMatch(RegexType.TELECOM.getType(), accountStr);
        boolean flag2 = ReUtil.isMatch(RegexType.UNICOM.getType(), accountStr);
        boolean flag3 = ReUtil.isMatch(RegexType.MOBILE.getType(), accountStr);

        return flag1 || flag2 || flag3;
    }
}
