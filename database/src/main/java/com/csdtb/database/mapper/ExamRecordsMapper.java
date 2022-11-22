package com.csdtb.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csdtb.database.entity.ExamRecordPageEntity;
import com.csdtb.database.entity.ExamRecordsEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 根据条件分页查询考试记录
     * @param page
     * @param userId
     * @param examName
     * @param userName
     * @return
     */
    Page<ExamRecordPageEntity> selectPageByCondition(Page page, Long userId, String examName, String userName);
}
