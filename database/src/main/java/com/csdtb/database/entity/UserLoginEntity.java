package com.csdtb.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 账号管理表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user_login")
public class UserLoginEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账户id(唯一)
     */
    @TableField("account")
    private Long account;

    /**
     * 密码MD5
     */
    @TableField("password")
    private String password;

    /**
     * 用户姓名
     */
    @TableField("username")
    private String username;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 性别(0-男,1-女)
     */
    @TableField("sex")
    private Boolean sex;

    /**
     * 管制单位
     */
    @TableField("control_unit")
    private String controlUnit;

    /**
     * 职称
     */
    @TableField("positional_title")
    private String positionalTitle;

    /**
     * 职务
     */
    @TableField("positional_job")
    private String positionalJob;

    /**
     * 所属角色(0-管理员，1-考核员，2-管制员)
     */
    @TableField("role")
    private Integer role;

    /**
     * 删除标志:1:删除，0：未删除
     */
    @TableField("is_delete")
    private Boolean isDelete;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
