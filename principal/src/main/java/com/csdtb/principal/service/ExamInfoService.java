package com.csdtb.principal.service;

import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.exam.AddExamDTO;
import com.csdtb.common.dto.exam.UpdateExamDTO;

/**
 * <p>
 * 考试信息表 服务类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-17
 */
public interface ExamInfoService {

    /**
     * 新增考核
     * @param dto
     * @return
     */
    ResponseResult addExam(AddExamDTO dto);

    /**
     * 分页查询考试信息
     * @param page
     * @param pageSize
     * @param name
     * @param startTime
     * @return
     */
    ResponseResult selectExamByPage(Integer page, Integer pageSize, String name, String startTime);

    /**
     * 更新考试信息
     * @param dto
     * @return
     */
    ResponseResult updateExam(UpdateExamDTO dto);

    /**
     * 删除考试信息
     * @param id
     * @return
     */
    ResponseResult deleteExam(Integer id);

    /**
     * 进入考核指导语
     * @return
     */
    ResponseResult guidelines();

    /**
     * 查询考试详情
     * @param id
     * @return
     */
    ResponseResult selectExamDetail(String id);
}
