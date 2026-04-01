package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.dto.ChatRequestDTO;
import java.io.PrintWriter;

/**
 * AI 代理服务接口
 *
 * 将前端请求转发到 Claude / MiniMax API
 * 支持 SSE 流式和同步两种模式
 */
public interface IIlmAiProxyService {

    /**
     * SSE 流式对话
     * @return 实际消耗的 token 数
     */
    int streamChat(ChatRequestDTO req, PrintWriter writer, Long userId) throws Exception;

    /**
     * 同步对话（非流式）
     * @return AI 回复内容
     */
    String syncChat(ChatRequestDTO req, Long userId) throws Exception;

    /**
     * 估算文本的 token 数（简单按字符数估算）
     */
    int estimateTokens(String text);
}
