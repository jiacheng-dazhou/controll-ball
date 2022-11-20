package com.csdtb.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csdtb.database.entity.ExamInfoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 考试信息表 Dao 接口
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-17
 */
public interface ExamInfoMapper extends BaseMapper<ExamInfoEntity> {

    /**
     * 批量更新考核状态
     * @param ids
     * @param status
     */
    void updateStatusByIds(@Param("ids") List<Integer> ids, Integer status);

    /**
     * 批量更新考试题库和状态
     * @param examList
     */
    void updateQuestionsByIds(@Param("examList") List<ExamInfoEntity> examList);
}
