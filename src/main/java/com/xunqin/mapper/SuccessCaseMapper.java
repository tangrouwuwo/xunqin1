package com.xunqin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunqin.entity.SuccessCase;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface SuccessCaseMapper extends BaseMapper<SuccessCase> {

    @Delete("DELETE FROM success_case WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}