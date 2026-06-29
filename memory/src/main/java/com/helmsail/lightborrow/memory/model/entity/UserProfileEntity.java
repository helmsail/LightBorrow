package com.helmsail.lightborrow.memory.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("user_profile")
public class UserProfileEntity {

    @TableId
    private String userId;

    private String profileData;

    private LocalDateTime updatedAt;
}
