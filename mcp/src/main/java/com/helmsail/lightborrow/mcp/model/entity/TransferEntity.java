package com.helmsail.lightborrow.mcp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("transfer")
public class TransferEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String fromUserId;

    private Integer borrowId;

    private String toUserId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
