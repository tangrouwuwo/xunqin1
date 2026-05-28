package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.VolunteerActivityReport;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VolunteerActivityReportMapper extends BaseMapper<VolunteerActivityReport> {

    @Select("SELECT * FROM volunteer_activity_report WHERE participant_id = #{participantId} AND is_deleted = 0")
    VolunteerActivityReport selectByParticipantId(@Param("participantId") Long participantId);

    @Select("SELECT COUNT(*) FROM volunteer_activity_report WHERE activity_id = #{activityId} AND status = 0 AND is_deleted = 0")
    int countPendingReports(@Param("activityId") Long activityId);

    @Delete("DELETE FROM volunteer_activity_report WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
