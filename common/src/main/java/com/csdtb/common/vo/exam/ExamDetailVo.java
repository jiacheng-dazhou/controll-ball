package com.csdtb.common.vo.exam;

import lombok.Data;


/**
 * @Author zhoujiacheng
 * @Date 2022-11-24
 **/
@Data
public class ExamDetailVo {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 考核名称
     */
    private String name;

    /**
     * 考试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String startTime;

    /**
     * 考核时长（分钟表示）
     */
    private Long examDuration;

    /**
     * 监视任务难度（1-简单，2-一般，3-困难）
     */
    private Integer monitorLevel;

    /**
     * 监视任务时长（分钟）
     */
    private Integer monitorDuration;

    /**
     * 监视休息时长（分钟）
     */
    private Integer monitorSleepDuration;

    /**
     * 计算任务难度（1-简单，2-一般，3-困难）
     */
    private Integer calculateLevel;

    /**
     * 计算固定数量
     */
    private Integer calculateNumber;

    /**
     * 计算固定频率（秒）
     */
    private Integer calculateRate;

    /**
     * 脑电测试开始时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String erpStartTime;

    /**
     * 脑电测试结束时间（格式：yyyy-MM-dd HH:mm:ss)
     */
    private String erpEndTime;

    /**
     * 考核说明
     */
    private String description;

    /**
     * 考试状态(1-未开考，2-即将开始，3-进行中，4-已完成）
     */
    private Integer status;

    /**
     * 考试题库
     */
    private String questions;

    /**
     * 监控任务难度详情
     */
    private BallMonitorTaskVo ballMonitorTaskVo;

    @Data
    public static class BallMonitorTaskVo{
        /**
         * 小球数量（1-10）
         */
        private Integer number;

        /**
         * 小球速度(1-慢、2-中、3-快)
         */
        private Integer speed;

        /**
         * 折返小球颜色(1-蓝，2-绿)
         */
        private Integer turnbackColor;

        /**
         * 小球折返率（10%-90%）
         */
        private Integer turnbackRate;

        /**
         * 出界小球颜色(1-蓝，2-绿)
         */
        private Integer boundsColor;

        /**
         * 小球出界率（10%-90%）
         */
        private Integer boundsRate;
    }
}
