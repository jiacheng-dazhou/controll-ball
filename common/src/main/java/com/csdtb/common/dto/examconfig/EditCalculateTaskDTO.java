package com.csdtb.common.dto.examconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditCalculateTaskDTO {
    /**
     * 主键id
     */
    private Integer id;

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
