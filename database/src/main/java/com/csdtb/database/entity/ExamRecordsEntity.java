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
 * 考试记录表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_exam_records")
public class ExamRecordsEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考核id
     */
    @TableField("exam_id")
    private Integer examId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;


}
