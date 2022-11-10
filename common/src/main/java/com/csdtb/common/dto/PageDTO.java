package com.csdtb.common.dto;

import lombok.Data;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-02
 **/
@Data
public class PageDTO {
    /**
     * 当前页
     */
    private Integer page = 1;
    /**
     * 分页大小
     */
    private Integer pageSize = 10;
}
