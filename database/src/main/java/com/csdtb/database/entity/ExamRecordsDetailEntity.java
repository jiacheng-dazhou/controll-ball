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
 * 考试记录详情表
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_exam_records_detail")
public class ExamRecordsDetailEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考核id
     */
    @TableField("exam_records_id")
    private Integer examRecordsId;

    /**
     * 记录类型(1-折返,2-出界,3-碰壁,4-计算,5-脑电测试)
     */
    @TableField("type")
    private Integer type;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 出现时间
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 是否正确(0-错误，1-正确)
     */
    @TableField("is_correct")
    private Boolean isCorrect;

    /**
     * 判断反应时间(秒）
     */
    @TableField("reaction_time")
    private String reactionTime;

    /**
     * 判断效率
     */
    @TableField("judgment_efficiency")
    private String judgmentEfficiency;

    /**
     * 答案（类型：4-计算对应字段）
     */
    @TableField("answer")
    private String answer;

    /**
     * 是否提前反应(0-错误，1-正确)
     */
    @TableField("is_react_in_advance")
    private Boolean isReactInAdvance;


}
