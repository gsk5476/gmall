package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
 * 商品属性
 * 
 * @author gsk
 * @email fengge@atguigu.com
 * @date 2020-12-14 19:55:55
 */
@Mapper
@Service
public interface AttrMapper extends BaseMapper<AttrEntity> {
	
}
