package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.VolunteerActivityParticipant;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VolunteerActivityParticipantMapper extends BaseMapper<VolunteerActivityParticipant> {

    @Select("SELECT * FROM volunteer_activity_participant WHERE activity_id = #{activityId} AND volunteer_id = #{volunteerId} AND is_deleted = 0")
    VolunteerActivityParticipant selectByActivityAndVolunteer(@Param("activityId") Long activityId, @Param("volunteerId") Long volunteerId);

    @Update("UPDATE volunteer_activity_participant SET is_reported = 1 WHERE id = #{id}")
    void markAsReported(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM volunteer_activity_participant WHERE activity_id = #{activityId} AND status = 1 AND is_deleted = 0")
    int countApprovedParticipants(@Param("activityId") Long activityId);

    @Delete("DELETE FROM volunteer_activity_participant WHERE activity_id = #{activityId}")
    int deletePhysicalByActivityId(@Param("activityId") Long activityId);
}
