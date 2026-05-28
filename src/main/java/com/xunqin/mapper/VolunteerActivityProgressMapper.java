package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.VolunteerActivityProgress;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VolunteerActivityProgressMapper extends BaseMapper<VolunteerActivityProgress> {

    @Select("SELECT * FROM volunteer_activity_progress WHERE activity_id = #{activityId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<VolunteerActivityProgress> selectByActivityId(@Param("activityId") Long activityId);

    @Delete("DELETE FROM volunteer_activity_progress WHERE activity_id = #{activityId}")
    int deletePhysicalByActivityId(@Param("activityId") Long activityId);
}
