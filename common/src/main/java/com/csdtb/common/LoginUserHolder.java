package com.csdtb.common;

import com.csdtb.common.dto.user.UserDTO;

/**
 * @author zhoujiacheng
 * @date 2023-03-07
 */
public class LoginUserHolder {
    private static ThreadLocal<UserDTO> userHolder = new ThreadLocal();

    public static UserDTO getUser() {
        return userHolder.get();
    }

    public static void putUser(UserDTO user) {
        userHolder.set(user);
    }

    public static void removeUser(){
        userHolder.remove();
    }
}
