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
 * 菜单表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_menu")
public class MenuEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单名称
     */
    @TableField("title")
    private String title;

    /**
     * 菜单路径url
     */
    @TableField("path")
    private String path;


}
