package com.csdtb.principal.controller;

import com.csdtb.common.ResponseResult;
import com.csdtb.common.dto.user.AddUserDTO;
import com.csdtb.common.dto.user.UpdateUserDTO;
import com.csdtb.principal.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 账号管理表模块
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-09
 */
@Slf4j
@RestController
@RequestMapping("/user-login")
public class UserLoginController {

    @Resource
    private UserLoginService userLoginService;

    @PostMapping("/addUser")
    public ResponseResult addUser(@RequestBody AddUserDTO dto){
        return userLoginService.addUser(dto);
    }

    @GetMapping("/selectUserByPage")
    public ResponseResult selectUserByPage(
            @RequestParam(value = "page",defaultValue = "1",required = false)Integer page,
            @RequestParam(value = "pageSize",defaultValue = "10",required = false)Integer pageSize,
            @RequestParam(value = "username",required = false)String username,
            @RequestParam(value = "account",required = false)Long account
    ){
        return userLoginService.selectUserByPage(page,pageSize,username,account);
    }

    @PutMapping("/updateUser")
    public ResponseResult updateUser(@RequestBody UpdateUserDTO dto){
        return userLoginService.updateUser(dto);
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseResult deleteUser(@PathVariable("id")Long id){
        return userLoginService.deleteUser(id);
    }
}
