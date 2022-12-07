package com.csdtb.common.dto.websocket;

import lombok.Data;

@Data
public class BallData {
    private Integer horizontalCoordinate;//横坐标
    private Integer verticalCoordinate;//纵坐标
    private Integer horizontalSpeed;//横坐标速度
    private Integer verticalSpeed;//纵坐标速度
}
