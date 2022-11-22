package com.csdtb.common.vo.records;

import lombok.Data;

import java.util.List;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-21
 **/
@Data
public class ExamRecordDetailVo {
    /**
     * 姓名
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
     * 考核时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;
    /**
     * 考核总时长（格式：HH:mm:ss)
     */
    private String totalTime;
    /**
     * 考核时长（格式：HH:mm:ss)
     */
    private String nowTime;
    /**
     * 休息次数
     */
    private Integer breakNumber;
    /**
     * 休息总时长（格式：HH:mm:ss)
     */
    private String breakTotalTime;
    /**
     * 监控任务
     */
    private MonitorVo monitorVo;
    /**
     * 计算任务
     */
    private CalculateVo calculateVo;

    @Data
    public static class CalculateVo{
        /**
         * 题目数量
         */
        private Integer titleCount;
        /**
         * 难度(1-简单，2-一般，3-困难)
         */
        private Integer level;
        /**
         * 正确率
         */
        private String calculateRightRate;
        /**
         * 计算详情(考核员、管理员特有字段)
         */
        private List<CalculateDetailVo> calculateDetailVoList;

        @Data
        public static class CalculateDetailVo{
            /**
             * 名称
             */
            private String name;
            /**
             * 出现时间(格式：HH:mm:ss)
             */
            private String startTime;
            /**
             * 是否正确(0-错误，1-正确)
             */
            private Boolean isCorrect;
            /**
             * 答案
             */
            private String answer;
            /**
             * 判断反应时间(秒）
             */
            private String reactionTime;
            /**
             * 判断效率
             */
            private String judgmentEfficiency;
        }
    }

    @Data
    public static class MonitorVo{
        /**
         * 监控时长（格式：HH:mm:ss）
         */
        private String  monitorTime;
        /**
         * 难度(1-简单，2-一般，3-困难)
         */
        private Integer level;
        /**
         * 出界总数
         */
        private Integer boundsTotal;
        /**
         * 碰撞次数
         */
        private Integer crashTotal;
        /**
         * 折返总数
         */
        private Integer turnbackTotal;
        /**
         * 出界正确率
         */
        private String boundsRightRate;
        /**
         * 折返正确率
         */
        private String turnbackRightRate;
        /**
         * 碰撞正确率
         */
        private String crashRightRate;
        /**
         * 小球监控详情(考核员、管理员特有字段)
         */
        private List<MonitorDetailVo> monitorDetailVoList;

        @Data
        public static class MonitorDetailVo{
            /**
             * 名称
             */
            private String name;
            /**
             * 出现时间(格式：HH:mm:ss)
             */
            private String startTime;
            /**
             * 是否正确(0-错误，1-正确)
             */
            private Boolean isCorrect;
            /**
             * 是否提前反应(0-错误，1-正确)
             */
            private Boolean isReactInAdvance;
            /**
             * 判断反应时间(秒）
             */
            private String reactionTime;
            /**
             * 判断效率
             */
            private String judgmentEfficiency;
        }
    }
}
