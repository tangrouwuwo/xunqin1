package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.VolunteerApplication;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VolunteerApplicationMapper extends BaseMapper<VolunteerApplication> {

    @Select("SELECT * FROM volunteer_application WHERE id = #{id}")
    VolunteerApplication selectByIdIgnoreLogicDelete(@Param("id") Long id);

    @Delete("DELETE FROM volunteer_application WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
