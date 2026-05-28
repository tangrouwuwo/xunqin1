package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.VolunteerNotification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface VolunteerNotificationMapper extends BaseMapper<VolunteerNotification> {
    @Delete("DELETE FROM volunteer_notification WHERE id = #{id} AND user_id = #{userId}")
    int physicalDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}