package com.helmsail.lightborrow.memory.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("user_memory")
public class UserMemoryEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userId;

    /** 记忆类型: entity / preference / event */
    private String memoryType;

    /** 记忆内容文本 */
    private String content;

    /** 向量嵌入，格式 '[0.1,0.2,...]' */
    private String embedding;

    private LocalDateTime createdAt;
}
