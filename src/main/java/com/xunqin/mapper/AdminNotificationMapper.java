package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.AdminNotification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface AdminNotificationMapper extends BaseMapper<AdminNotification> {
    @Delete("DELETE FROM admin_notification WHERE id = #{id} AND user_id = #{userId}")
    int physicalDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}