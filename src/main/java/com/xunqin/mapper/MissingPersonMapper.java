package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.MissingPerson;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MissingPersonMapper extends BaseMapper<MissingPerson> {

    @Delete("DELETE FROM missing_person WHERE id = #{id}")
    int deleteByIdPhysical(@Param("id") Long id);
}
