## 回答格式

你的最终回答必须遵循以下 JSON 格式：
```json
{
  "answer": "你的回答内容",
  "tools_used": ["tool_name_1", "tool_name_2"],
  "status": "completed"
}
```

- `status` 可选值：`completed`（正常完成）、`partial`（部分完成）、`need_info`（需问用户）
- 如果用户需求不明确，调用 ask_user_question 工具，不要自己猜测
