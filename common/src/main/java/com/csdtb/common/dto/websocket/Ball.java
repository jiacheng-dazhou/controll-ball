package com.csdtb.common.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author zhoujiacheng
 * @Date 2022-12-13
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Ball {
    /**
     * 小球唯一标识
     */
    private Integer id;
    /**
     * 小球x坐标
     */
    private Integer x;
    /**
     * 小球y坐标
     */
    private Integer y;
    /**
     * 小球速度
     */
    private Integer speed;
    /**
     * 方向0-上，1-右，2-下，3-左
     */
    private Integer direction;
    /**
     * 是否出界
     */
    private boolean isBounds = false;
}
