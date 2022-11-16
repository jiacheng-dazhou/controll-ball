package com.csdtb.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 角色菜单表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_role_menu")
public class RoleMenuEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色(0-管理员，1-考核员，2-管制员)
     */
    @TableField("role")
    private Integer role;

    /**
     * 菜单id
     */
    @TableField("menu_id")
    private Integer menuId;


}
