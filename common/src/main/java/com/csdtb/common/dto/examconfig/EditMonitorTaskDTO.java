package com.csdtb.common.dto.examconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


/**
 * @Author zhoujiacheng
 * @Date 2022-11-16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditMonitorTaskDTO {
    /**
     * 主键id
     */
    private Integer id;
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
