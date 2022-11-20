package com.csdtb.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csdtb.database.entity.ExamRecordsEntity;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 考试记录表 Dao 接口
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
public interface ExamRecordsMapper extends BaseMapper<ExamRecordsEntity> {

    /**
     * 新增并返回id
     * @param examRecordsEntity
     */
    void insertAndReturnId(@Param("entity") ExamRecordsEntity examRecordsEntity);
}
