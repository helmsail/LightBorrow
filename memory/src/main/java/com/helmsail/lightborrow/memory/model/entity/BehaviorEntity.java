package com.helmsail.lightborrow.memory.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("behavior")
public class BehaviorEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userId;

    private String action;

    private String targetType;

    private String targetId;

    private LocalDateTime createdAt;
}
