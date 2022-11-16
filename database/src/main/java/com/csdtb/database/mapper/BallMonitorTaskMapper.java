package com.csdtb.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csdtb.database.entity.BallMonitorTaskEntity;

import java.util.List;

/**
 * <p>
 * 小球监控任务表 Dao 接口
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
public interface BallMonitorTaskMapper extends BaseMapper<BallMonitorTaskEntity> {
    /**
     * 批量更新
     * @param list
     */
    void updateListById(List<BallMonitorTaskEntity> list);
}
