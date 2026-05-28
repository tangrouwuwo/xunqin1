package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.VolunteerActivity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VolunteerActivityMapper extends BaseMapper<VolunteerActivity> {

    @Update("UPDATE volunteer_activity SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE volunteer_activity SET current_participants = current_participants + 1 WHERE id = #{id}")
    void incrementParticipantCount(@Param("id") Long id);

    @Update("UPDATE volunteer_activity SET current_participants = current_participants - 1 WHERE id = #{id}")
    void decrementParticipantCount(@Param("id") Long id);

    @Delete("DELETE FROM volunteer_activity WHERE id = #{id}")
    int deleteByIdPhysical(@Param("id") Long id);
}
