package com.helmsail.lightborrow.memory.pipeline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.memory.exception.MemoryException;
import com.helmsail.lightborrow.memory.mapper.BehaviorMapper;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import com.helmsail.lightborrow.memory.model.entity.BehaviorEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用户画像阶段。从 behavior 表加载用户最近行为。
 */
@Slf4j
public class ProfileStage implements MemoryStage {

    private final BehaviorMapper behaviorMapper;

    public ProfileStage(BehaviorMapper behaviorMapper) {
        this.behaviorMapper = behaviorMapper;
    }

    @Override
    public void load(MemoryContext ctx) {
        try {
            IPage<BehaviorEntity> behaviorPage = behaviorMapper.selectPage(
                    new Page<>(1, 5),
                    Wrappers.<BehaviorEntity>lambdaQuery()
                            .eq(BehaviorEntity::getUserId, ctx.getUserId())
                            .orderByDesc(BehaviorEntity::getCreatedAt));
            List<BehaviorEntity> behaviors = behaviorPage.getRecords();

            if (behaviors.isEmpty()) {
                ctx.setProfileSummary("新用户");
            } else {
                StringBuilder sb = new StringBuilder("最近行为：");
                for (BehaviorEntity b : behaviors) {
                    sb.append(b.getAction()).append(" ")
                            .append(b.getTargetType()).append(" ")
                            .append(b.getTargetId()).append("; ");
                }
                ctx.setProfileSummary(sb.toString());
            }
            log.debug("[Memory] 画像加载 userId={}", ctx.getUserId());
        } catch (Exception e) {
            log.error("[Memory] 画像加载失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_PROFILE_FAILED, e, ctx.getUserId());
        }
    }
}
