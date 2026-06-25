package com.helmsail.lightborrow.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lightborrow.core")
public class CoreProperties {

    /** ReAct 循环最大步数 */
    private int maxSteps = 15;
}
