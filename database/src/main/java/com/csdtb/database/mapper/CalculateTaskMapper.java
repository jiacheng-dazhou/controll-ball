package com.csdtb.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csdtb.database.entity.CalculateTaskEntity;

import java.util.List;

/**
 * <p>
 * 计算任务表 Dao 接口
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-16
 */
public interface CalculateTaskMapper extends BaseMapper<CalculateTaskEntity> {

    /**
     * 批量更新
     * @param list
     */
    void updateListById(List<CalculateTaskEntity> list);
}
