package com.helmsail.lightborrow.rag.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "lightborrow.rag")
public class RagProperties {

    /** 分块大小（字符数） */
    @Min(value = 50, message = "分块大小必须 >= 50")
    private int chunkSize = 500;

    /** 重叠字符数 */
    @Min(value = 0, message = "重叠字符数不能为负")
    private int overlap = 50;

    /** 检索返回 topK 条 */
    @Min(value = 1, message = "topK 必须 >= 1")
    private int topK = 5;
}
