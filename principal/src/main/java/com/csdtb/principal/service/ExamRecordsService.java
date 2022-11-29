package com.csdtb.principal.service;

import com.csdtb.common.ResponseResult;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 考试记录表 服务类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
public interface ExamRecordsService {

    /**
     * 分页查询考核记录
     * @param page
     * @param pageSize
     * @param examName
     * @param userName
     * @param token
     * @return
     */
    ResponseResult selectRecordsByPage(Integer page, Integer pageSize, String examName, String userName,String token);

    /**
     * 查询考核记录详情
     * @param id
     * @param token
     * @return
     */
    ResponseResult selectRecordDetail(Integer id,String token);

    /**
     * 导出考核记录详情
     * @param id
     * @param token
     * @param response
     * @return
     */
    void exportRecordDetail(Integer id, String token, HttpServletResponse response);

    /**
     * 查看录像
     * @param id
     * @param token
     * @param response
     */
    void selectExamRecordVideo(Integer id, String token, HttpServletResponse response) throws Exception;
}
