package com.csdtb.common.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-02
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> {

    /**
     * 分页信息
     */
    private PageVo pageVo;

    /**
     * 返回数据
     */
    private Collection<T> list;

    @Data
    @Builder
    public static class PageVo{
        /**
         * 当前页
         */
        private long page;
        /**
         * 分页大小
         */
        private long pageSize;
        /**
         * 总记录数
         */
        private long total;
        /**
         * 总页数
         */
        private long totalPage;
    }

    public static PageData initPageVo(Page page, Collection list){
        PageVo pageVo = PageVo.builder()
                .page(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .totalPage(page.getPages())
                .build();
        return PageData.builder()
                .pageVo(pageVo)
                .list(list)
                .build();
    }

    public static PageData initPageVo(Page page){
        PageVo pageVo = PageVo.builder()
                .page(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .totalPage(page.getPages())
                .build();
        return PageData.builder()
                .pageVo(pageVo)
                .build();
    }
}
