package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.Clue;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface ClueMapper extends BaseMapper<Clue> {
    @Delete("DELETE FROM clue WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
