package com.csdtb.common.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-11
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {
    /**
     * 请求token
     */
    private String token;
    /**
     * 菜单信息
     */
    private List<MenuVo> menuVoList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuVo{
        private Integer id;

        /**
         * 菜单名称
         */
        private String title;

        /**
         * 菜单路径url
         */
        private String path;
    }
}
