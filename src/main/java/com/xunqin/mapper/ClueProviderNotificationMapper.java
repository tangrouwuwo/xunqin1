package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.ClueProviderNotification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface ClueProviderNotificationMapper extends BaseMapper<ClueProviderNotification> {
    @Delete("DELETE FROM clue_provider_notification WHERE id = #{id} AND user_id = #{userId}")
    int physicalDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}