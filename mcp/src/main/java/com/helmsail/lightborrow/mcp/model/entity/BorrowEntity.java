package com.helmsail.lightborrow.mcp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("borrow")
public class BorrowEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userId;

    private Integer assetId;

    private String reason;

    private LocalDate expectedReturnAt;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
