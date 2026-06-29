package com.helmsail.lightborrow.memory.service;

import com.helmsail.lightborrow.memory.mapper.BehaviorMapper;
import com.helmsail.lightborrow.memory.model.entity.BehaviorEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户反馈服务。记录用户对 Agent 回答的点赞/点踩。
 */
@Slf4j
public class FeedbackService {

    private final BehaviorMapper behaviorMapper;

    public FeedbackService(BehaviorMapper behaviorMapper) {
        this.behaviorMapper = behaviorMapper;
    }

    public void record(String userId, String sessionId, String rating, String comment) {
        try {
            BehaviorEntity entity = new BehaviorEntity();
            entity.setUserId(userId);
            entity.setAction("feedback_" + ("like".equals(rating) ? "like" : "dislike"));
            entity.setTargetType("session");
            entity.setTargetId(sessionId);
            behaviorMapper.insert(entity);
            log.info("[Feedback] userId={}, rating={}, comment={}", userId, rating, comment);
        } catch (Exception e) {
            log.warn("[Feedback] 记录失败 userId={}", userId, e);
        }
    }
}
