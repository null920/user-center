package com.ycr.usercenter.mapper;

import com.ycr.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author null&&
 * @description 针对表【user(用户)】的数据库操作Mapper
 * @createDate 2024-04-10 17:49:34
 * @Entity generator.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




