package com.csdtb.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-09
 * @Description 正则表达式匹配类型枚举
 **/
@Getter
@AllArgsConstructor
public enum RegexType {

    TELECOM("(?:^(?:\\+86)?1(?:33|49|53|7[37]|8[019]|9[19])\\d{8}$)|(?:^(?:\\+86)?1349\\d{7}$)|(?:^(?:\\+86)?1410\\d{7}$)|(?:^(?:\\+86)?170[0-2]\\d{7}$)","中国电信号码格式验证"),
    UNICOM("(?:^(?:\\+86)?1(?:3[0-2]|4[56]|5[56]|66|7[156]|8[56])\\d{8}$)|(?:^(?:\\+86)?170[47-9]\\d{7}$)","中国联通号码格式验证"),
    MOBILE("(?:^(?:\\+86)?1(?:3[4-9]|4[78]|5[0-27-9]|78|8[2-478]|98|95)\\d{8}$)|(?:^(?:\\+86)?1440\\d{7}$)|(?:^(?:\\+86)?170[356]\\d{7}$)","中国移动号码格式验证"),
    PASSWORD("^(?![0-9]+$)(?![a-zA-Z]+$).*{8,}$","密码验证：密码必须包含密码中必须包含字母、数字，至少8个字符");

    private String type;
    private String description;
}
