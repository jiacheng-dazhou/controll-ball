package com.csdtb.common.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhoujiacheng
 * @Date 2022-12-13
 **/
@Getter
@AllArgsConstructor
public class Frame {
    /**
     * 外边框x坐标最大边界
     */
    public static final Integer margin_x = 596;
    /**
     * 外边框y坐标最大边界
     */
    public static final Integer margin_y = 540;
    /**
     * 内边框x坐标最大边界
     */
    public static final Integer innerBorder_x = 546;
    /**
     * 内边框y坐标最大边界
     */
    public static final Integer innerBorder_y = 490;
    /**
     * 小球半径
     */
    public static final Integer ballRadius = 20;
    /**
     * 内外边框间距
     */
    public static final Integer distance = 50;
}
