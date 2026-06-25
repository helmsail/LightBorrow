package com.helmsail.lightborrow.gateway.controller;

import com.helmsail.lightborrow.framework.model.Result;
import com.helmsail.lightborrow.gateway.adapter.ChannelAdapter;
import com.helmsail.lightborrow.gateway.model.InternalMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.GATEWAY_CHANNEL_ERROR;

/**
 * 接收 IM 平台 HTTP Webhook 回调的统一入口。
 *
 * <p>路径路由：{@code /webhook/{channel}}（channel = feishu / dingtalk / wechat）。
 * <br>请求验签和限流由 {@code WebhookFilter} 统一处理，Controller 仅负责消息解析与投递。
 */
@Slf4j
@RestController
public class WebhookController {

    private final Map<String, ChannelAdapter> adapterMap;

    public WebhookController(List<ChannelAdapter> adapters) {
        this.adapterMap = adapters.stream()
                .collect(Collectors.toMap(ChannelAdapter::getChannel, a -> a));
        log.info("[Gateway] 已注册渠道适配器: {}", adapterMap.keySet());
    }

    /**
     * 接收 IM 平台 Webhook 消息。
     *
     * @param channel 渠道名（路径变量），如 feishu / dingtalk / wechat
     * @param body    原始请求体
     * @return 统一返回结果
     */
    @PostMapping("/webhook/{channel}")
    public Result<String> receiveMessage(@PathVariable("channel") String channel,
                                         @RequestBody String body) {
        ChannelAdapter adapter = adapterMap.get(channel);
        if (adapter == null) {
            log.warn("[Gateway] 不支持的渠道: {}, 可用渠道: {}", channel, adapterMap.keySet());
            return Result.error(GATEWAY_CHANNEL_ERROR.getCode(), "不支持的渠道: " + channel);
        }

        try {
            InternalMessage msg = adapter.parseRequest(channel, body);
            log.info("[Gateway] 收到消息 channel={}, userId={}, chatId={}, content={}",
                    channel, msg.getUserId(), msg.getChatId(), msg.getContent());
            return Result.success("ok");
        } catch (Exception e) {
            log.error("[Gateway] 消息解析失败 channel={}", channel, e);
            return Result.error(GATEWAY_CHANNEL_ERROR.getCode(), "消息解析失败: " + e.getMessage());
        }
    }
}
