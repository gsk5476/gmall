package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author gsk
 * @email gsk@atguigu.com
 * @date 2020-12-14 21:20:46
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
