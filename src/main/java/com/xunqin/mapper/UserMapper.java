package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除用户（绕过逻辑删除）
     */
    @Update("DELETE FROM user WHERE id = #{userId}")
    int deleteByIdPhysical(@Param("userId") Long userId);
}
