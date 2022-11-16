package com.csdtb.common.vo.examconfig;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-16
 **/
@Data
@Builder
public class SelectExamConfigVo {
    /**
     * 监控任务集合
     */
    private List<BallMonitorTaskVo> monitorTaskVoList;

    /**
     * 计算任务
     */
    private List<CalculateTaskVo> calculateTaskVoList;

    /**
     * 准备阶段
     */
    private PrepareStageVo prepareStageVo;

    @Data
    public static class BallMonitorTaskVo{
        /**
         * 主键id
         */
        private Integer id;

        /**
         * 难度(1-简单，2-一般，3-困难)
         */
        private Integer level;

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
    @Data
    public static class CalculateTaskVo{
        /**
         * 主键id
         */
        private Integer id;

        /**
         * 难度(1-简单，2-一般，3-困难)
         */
        private Integer level;

        /**
         * 难度1：一位数加减法（10%-90%）
         */
        private Integer calculateLevel1;

        /**
         * 难度2：两位数加减法（不进位不退位，答案是两位数）（10%-90%）
         */
        private Integer calculateLevel2;

        /**
         * 难度3：两位数加减法（进位退位，答案是两位数）（10%-90%）
         */
        private Integer calculateLevel3;
    }
    @Data
    public static class PrepareStageVo{
        /**
         * 主键id
         */
        private Integer id;

        /**
         * 指导语内容
         */
        private String guidingWords;

        /**
         * 指导语时长（1-30秒，2-1分钟，3-2分钟，4-3分钟）
         */
        private Integer guidingWordsDuration;

        /**
         * 模拟考核时长（1-1分钟，2-2分钟，3-3分钟）
         */
        private Integer simulateExamDuration;

        /**
         * 模拟小球数量（1，2，3，4，5）
         */
        private Integer simulateBallNumber;

        /**
         * 模拟小球速度(1-慢、2-中、3-快)
         */
        private Integer simulateBallSpeed;

        /**
         * 模拟考核计算数量（1，2，3，4，5）
         */
        private Integer simulateCalculateNumber;

        /**
         * 模拟考核计算难度(1-简单，2-一般，3-困难)
         */
        private Integer simulateCalculateLevel;
    }
}
