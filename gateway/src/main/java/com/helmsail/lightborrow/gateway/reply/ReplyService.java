package com.helmsail.lightborrow.gateway.reply;

import com.helmsail.lightborrow.gateway.adapter.ChannelAdapter;
import com.helmsail.lightborrow.gateway.model.ReplyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回复服务。根据渠道选择合适的 Adapter 格式化回复。
 */
@Slf4j
@Component
public class ReplyService {

    private final Map<String, ChannelAdapter> adapterMap;

    public ReplyService(List<ChannelAdapter> adapters) {
        this.adapterMap = adapters.stream()
                .collect(Collectors.toMap(ChannelAdapter::getChannel, a -> a));
    }

    /**
     * 格式化回复消息。
     *
     * @param reply 回复消息
     * @return 格式化后的内容
     */
    public String formatReply(ReplyMessage reply) {
        ChannelAdapter adapter = adapterMap.get(reply.getChannel());
        if (adapter == null) {
            log.warn("[Gateway] 不支持的渠道: {}", reply.getChannel());
            return reply.getContent();
        }
        return adapter.formatReply(reply);
    }
}
