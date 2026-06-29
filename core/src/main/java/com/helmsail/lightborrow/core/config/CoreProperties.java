package com.helmsail.lightborrow.core.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "lightborrow.core")
public class CoreProperties {

    /** ReAct 循环最大步数 */
    @Min(value = 1, message = "最大步数必须 >= 1")
    private int maxSteps = 15;

    /** 是否启用输入重写管线 */
    private boolean enableRewrite = true;
}
