package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author gsk
 * @email fengge@atguigu.com
 * @date 2020-12-14 19:55:56
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategorysByPid(Long pid);
}

