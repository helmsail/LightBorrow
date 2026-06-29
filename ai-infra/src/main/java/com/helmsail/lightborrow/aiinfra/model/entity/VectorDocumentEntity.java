package com.helmsail.lightborrow.aiinfra.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("vector_documents")
public class VectorDocumentEntity {

    @TableId
    private String id;

    /** pgvector 向量，以 '[0.1,0.2,...]' 字符串形式存储 */
    private String embedding;

    /** 元数据 JSON 字符串 */
    private String metadata;

    /** 余弦相似度距离，仅查询时填充 */
    @TableField(exist = false)
    private Double distance;

    private LocalDateTime createdAt;
}
