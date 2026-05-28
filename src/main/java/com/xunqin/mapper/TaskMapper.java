package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface TaskMapper extends BaseMapper<Task> {
    @Delete("DELETE FROM task WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
