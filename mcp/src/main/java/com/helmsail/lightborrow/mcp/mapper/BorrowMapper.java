package com.helmsail.lightborrow.mcp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helmsail.lightborrow.mcp.model.entity.BorrowEntity;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface BorrowMapper extends BaseMapper<BorrowEntity> {

    @Select("""
            SELECT b.*, a.name AS asset_name, a.code AS asset_code
            FROM borrow b
            JOIN asset a ON b.asset_id = a.id
            WHERE b.user_id = #{userId}
            ORDER BY b.created_at DESC
            """)
    IPage<Map<String, Object>> selectMyBorrows(Page<?> page, String userId);
}
