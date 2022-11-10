package com.csdtb.common.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-09
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPageVo {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 账户id(唯一)
     */
    private Long account;

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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
