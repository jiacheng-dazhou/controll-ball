package com.csdtb.principal.service;

import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.user.AddUserDTO;
import com.csdtb.common.dto.user.LoginDTO;
import com.csdtb.common.dto.user.UpdateUserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 账号管理表 服务类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-09
 */
public interface UserLoginService {

    /**
     * 新增账户
     * @param dto
     * @return
     */
    ResponseResult addUser(AddUserDTO dto);

    /**
     * 分页查询用户信息
     * @param page
     * @param pageSize
     * @param username
     * @param controlUnit
     * @return
     */
    ResponseResult selectUserByPage(Integer page, Integer pageSize, String username, String controlUnit);

    /**
     * 修改用户信息
     * @param dto
     * @return
     */
    ResponseResult updateUser(UpdateUserDTO dto);

    /**
     * 删除用户
     * @param id
     * @return
     */
    ResponseResult deleteUser(Long id);

    /**
     * 登录
     * @param dto
     * @return
     */
    ResponseResult toLogin(LoginDTO dto);

    /**
     * 退出登录
     * @param request
     * @return
     */
    ResponseResult loginOut(HttpServletRequest request);
}
