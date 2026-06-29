package com.helmsail.lightborrow.rag.service;

import com.helmsail.lightborrow.rag.pipeline.offline.RagOfflinePipeline;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RAG 知识库定时同步服务。
 *
 * <p>启动时自动扫描 docs/ 目录同步一次，之后每天凌晨 2 点重新同步。
 */
@Slf4j
public class RagScheduledService {

    private final RagOfflinePipeline ragOfflinePipeline;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public RagScheduledService(RagOfflinePipeline ragOfflinePipeline) {
        this.ragOfflinePipeline = ragOfflinePipeline;
    }

    @PostConstruct
    public void init() {
        log.info("[RAG] 定时同步服务初始化完成，首次同步将在启动后执行");
        syncKnowledgeBase();
    }

    /**
     * 每天凌晨 2 点同步知识库。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledSync() {
        log.info("[RAG] 开始定时同步知识库 {}", LocalDateTime.now());
        syncKnowledgeBase();
    }

    /**
     * 同步知识库：扫描 docs/ 目录下的 .md 文件，分块嵌入入库。
     */
    public void syncKnowledgeBase() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:docs/*.md");

            for (Resource resource : resources) {
                try {
                    String filename = resource.getFilename();
                    String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    String documentId = filename != null ? filename.replace(".md", "") : "unknown";

                    log.info("[RAG] 处理文档: {} ({} bytes)", filename, content.length());
                    int chunkCount = ragOfflinePipeline.processDocument(documentId, content).size();
                    log.info("[RAG] 文档 {} 处理完成，生成 {} 个块", filename, chunkCount);
                } catch (IOException e) {
                    log.warn("[RAG] 文档处理失败: {}", resource.getFilename(), e);
                }
            }

            initialized.set(true);
            log.info("[RAG] 知识库同步完成，共处理 {} 个文档", resources.length);
        } catch (IOException e) {
            log.error("[RAG] 知识库同步失败", e);
        }
    }

    public boolean isInitialized() {
        return initialized.get();
    }
}
