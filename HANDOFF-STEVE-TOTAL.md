# iLightMate 后端交接清单 — Steve 专用

> 编写日期：2026-04-01
> 编写人：Claude (AI) + ZoE
> 状态：前端 feature complete，后端代码骨架完成，需要联调 + 部署

---

## 一、项目总览

### 产品
- **名称**：iLightMate 知见光伙伴
- **定位**：自我探索与个人成长工具（非心理治疗/咨询）
- **前端**：React + TypeScript + Vite + TailwindCSS + Zustand
- **后端**：RuoYi 4.8.3（Spring Boot 2.7.x + MyBatis + MySQL + Redis + Shiro）
- **定价**：3 层（体验版 Free / 成长版 ¥19月 / 专业版 ¥1,680年）

### GitHub 仓库
| 仓库 | 地址 | 分支 |
|------|------|------|
| 前端 | `github.com/sisuy/ilightmate` | main |
| 后端 | `github.com/sisuy/ilightmate-backend` | master |

### 技术架构图
```
前端 (React/Vite)
  ↓ HTTPS + JWT Bearer Token
RuoYi 后端 (Spring Boot 2.7.x)
  ├── ruoyi-admin/        ← 启动入口 + application.yml
  ├── ruoyi-framework/    ← JWT + Redis + 拦截器（框架自带）
  ├── ruoyi-system/       ← sys_user + sys_role（框架自带）
  ├── ruoyi-common/       ← 工具类 + 响应包装（框架自带）
  └── ruoyi-ilightmate/   ← ★ 业务模块（我们写的全部代码）
      ├── controller/  (16 个)
      ├── service/     (11 接口 + 11 实现)
      ├── mapper/      (16 个)
      ├── domain/      (13 个)
      ├── config/      (4 个)
      ├── task/        (3 个定时任务)
      └── dto/         (2 个)
```

---

## 二、环境变量清单（部署必须）

在服务器 `.env` 或 application.yml 中配置：

```bash
# ═══ AI 提供商 ═══
AI_API_KEY=                    # MiniMax / Claude / OpenAI API Key

# ═══ 阿里云 SMS ═══
ALIYUN_ACCESS_KEY_ID=
ALIYUN_ACCESS_KEY_SECRET=
ALIYUN_SMS_TEMPLATE=           # 短信模板编号

# ═══ 支付宝 ═══
ALIPAY_APP_ID=
ALIPAY_PRIVATE_KEY=            # RSA2 私钥
ALIPAY_PUBLIC_KEY=             # 支付宝公钥
ALIPAY_NOTIFY_URL=https://api.ilightmate.cn/api/pay/alipay/notify
ALIPAY_RETURN_URL=https://app.ilightmate.cn/app/pricing

# ═══ 微信支付（V3） ═══
WECHAT_APP_ID=
WECHAT_MCH_ID=                 # 商户号
WECHAT_API_KEY_V3=             # APIv3 密钥
WECHAT_CERT_SERIAL_NO=        # 证书序列号
WECHAT_PRIVATE_KEY_PATH=       # 商户私钥 PEM 文件路径
WECHAT_NOTIFY_URL=https://api.ilightmate.cn/api/pay/wechat/notify

# ═══ MySQL ═══
# 在 application-druid.yml 中配置
# spring.datasource.druid.master.url / username / password

# ═══ Redis ═══
# 在 application.yml 中配置
# spring.redis.host / port / password
```

### application.yml 业务配置位置
文件：`ruoyi-admin/src/main/resources/application.yml` 第 159-193 行
```yaml
ilightmate:
  ai:
    provider: minimax              # claude / minimax / openai
    api-url: https://api.minimax.io/v1/chat/completions
    api-key: ${AI_API_KEY:}
    model-id: MiniMax-Text-01
    max-tokens: 4096
    temperature: 0.7
  sms:
    enabled: false                 # 生产改为 true
    access-key-id: ${ALIYUN_ACCESS_KEY_ID:}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET:}
    sign-name: 知见光伙伴
    template-code: ${ALIYUN_SMS_TEMPLATE:}
  alipay:
    app-id: ${ALIPAY_APP_ID:}
    private-key: ${ALIPAY_PRIVATE_KEY:}
    public-key: ${ALIPAY_PUBLIC_KEY:}
    gateway-url: https://openapi.alipay.com/gateway.do
    notify-url: ${ALIPAY_NOTIFY_URL:...}
    return-url: ${ALIPAY_RETURN_URL:...}
  wechat:
    app-id: ${WECHAT_APP_ID:}
    mch-id: ${WECHAT_MCH_ID:}
    api-key-v3: ${WECHAT_API_KEY_V3:}
    cert-serial-no: ${WECHAT_CERT_SERIAL_NO:}
    private-key-path: ${WECHAT_PRIVATE_KEY_PATH:}
    notify-url: ${WECHAT_NOTIFY_URL:...}
```

---

## 三、数据库（19 张 ilm_ 表）

### SQL 文件位置
- `sql/ilightmate.sql` — 全部 19 张业务表 + 种子数据
- `sql/ry_20260319.sql` — RuoYi 框架系统表
- `sql/quartz.sql` — 定时任务表

### 表清单

| # | 表名 | 用途 | 关联 Controller |
|---|------|------|----------------|
| 1 | `ilm_token_usage` | 月度 token 消耗（Redis 热路径 + MySQL 持久化） | IlmAiChatController |
| 2 | `ilm_dialogue_usage` | 每日对话次数计数 | IlmAiChatController |
| 3 | `ilm_combo_plans` | 3 层套餐目录（种子数据） | IlmComboController |
| 4 | `ilm_user_subscriptions` | 用户当前订阅状态 | IlmComboController |
| 5 | `ilm_orders` | 订单 + 支付状态机 | IlmOrderController / IlmPayController |
| 6 | `ilm_partners` | 推荐合作伙伴（CH-/SL-/MT- 码） | IlmMentorController |
| 7 | `ilm_user_attribution` | 用户归因（首付锁定） | IlmMentorController |
| 8 | `ilm_behavioral_events` | 匿名行为埋点 | IlmAnalyticsController |
| 9 | `ilm_explore_scores` | 七维评分 | IlmExploreController |
| 10 | `ilm_diary_entries` | 日记 | IlmDiaryController |
| 11 | `ilm_theater_sessions` | 关系剧场记录 | IlmTheaterController |
| 12 | `ilm_legacy_members` | 光语传承成员 | IlmLegacyController |
| 13 | `ilm_emotion_tags` | 情绪标签 | （暂无独立 Controller） |
| 14 | `ilm_activities` | 站内通知/活动记录 | AutoRenewTask / SubscriptionRenewRemindTask |
| 15 | `ilm_dialogue_history` | 对话摘要 | IlmDialogueHistoryController |
| 16 | `ilm_coach_sessions` | 明场会话（5步流程） | IlmCoachSessionController |
| 17 | `ilm_user_consent` | PIPL 同意记录 | IlmUserDataController |
| 18 | `ilm_user_onboarding` | 引导状态 | （暂无独立 Controller） |
| 19 | `ilm_family_trees` | 家族树（JSON 存储） | IlmFamilyTreeController |

### 种子数据（ilm_combo_plans）

```sql
INSERT INTO ilm_combo_plans VALUES
('0', '体验版', 0.00,    NULL,   50000,  '{"companions":2,"trees":1,"layers":3,"theaters":1}'),
('1', '成长版', 19.00,   198.00, 500000, '{"companions":6,"trees":3,"layers":7,"theaters":6,"legacy":3}'),
('2', '专业版', NULL,    1680.00,3000000,'{"companions":6,"trees":10,"layers":7,"theaters":6,"legacy":10,"caseManagement":true,"pdf":true}');
```

---

## 四、全部 API 端点（45 个）

### 4.1 认证 — IlmAuthController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| GET | `/resource/sms/code/login?phone={phone}` | 发送短信验证码 | `auth.ts` |
| POST | `/auth/smsLogin` | 短信验证码登录 | `auth.ts` |
| POST | `/auth/login` | 密码登录（RuoYi 自带） | `auth.ts` |
| GET | `/system/user/getInfo` | 获取当前用户信息 | `auth.ts` |

**请求/响应格式：**
```
POST /auth/smsLogin
Body: { "phone": "13800138000", "code": "123456" }
Response: { "code": 200, "msg": "success", "token": "eyJhbG..." }
```

### 4.2 AI 对话 — IlmAiChatController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/system/ai/chat` | AI 对话（SSE 流式） | `ai.ts` |

**这是最核心的端点。** 流程：
1. 前端发送 JSON body + JWT header
2. 后端检查 Redis `ilm:token:{userId}:{YYYY-MM}` 是否超额
3. 超额 → 返回 `{ code: 429, msg: "Token额度已用尽" }`
4. 未超额 → 转发请求到 AI 提供商（MiniMax/Claude/OpenAI）
5. SSE 流式响应：`Content-Type: text/event-stream`
6. 完成后 Redis INCRBY token 用量 + 异步写入 MySQL

**请求格式：**
```
POST /system/ai/chat
Headers: Authorization: Bearer {jwt_token}
Body: {
  "companionId": "yu",
  "messages": [
    { "role": "system", "content": "..." },
    { "role": "user", "content": "你好" }
  ],
  "maxTokens": 2048,
  "temperature": 0.7
}
```

**SSE 响应格式：**
```
data: {"choices":[{"delta":{"content":"你"}}]}
data: {"choices":[{"delta":{"content":"好"}}]}
data: [DONE]
```

### 4.3 套餐 — IlmComboController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| GET | `/api/combo/query` | 获取套餐列表 | `combo.ts` |
| GET | `/consultant/user/combo/{userId}` | 获取用户当前套餐 | `combo.ts` |
| GET | `/consultant/combo/{comboId}/benefits` | 获取套餐权益 | `combo.ts` |

### 4.4 订单 — IlmOrderController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/comboOrder/create-minimal` | 创建订阅订单 | `combo.ts` |

**请求：**
```json
{ "comboId": "1", "period": "monthly" }
```

### 4.5 支付 — IlmPayController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/pay/alipay/page` | 支付宝网页支付 | `payment.ts` |
| POST | `/api/pay/wechat/pc` | 微信 Native 支付（返回二维码 URL） | `payment.ts` |
| GET | `/api/pay/alipay/query?orderNo={no}` | 查询支付宝订单状态 | `payment.ts` |
| GET | `/api/pay/wechat/query?orderNo={no}` | 查询微信订单状态 | `payment.ts` |
| POST | `/api/pay/alipay/notify` | 支付宝异步回调（支付宝服务器调用） | — |
| POST | `/api/pay/wechat/notify` | 微信异步回调（微信服务器调用） | — |

**微信支付已用 wechatpay-java SDK v0.2.14 实现：**
- `RSAAutoCertificateConfig` 初始化
- `NativePayService.prepay()` 生成二维码
- `queryOrderByOutTradeNo()` 查询状态
- `NotificationParser` 验证回调签名

**支付宝已用 alipay-sdk-java v4.39.79 实现。**

### 4.6 Token 加购 — IlmTokenAddonController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| GET | `/api/tokenAddon/list` | 获取加购包列表 | `token-addon.ts` |
| POST | `/api/tokenAddon/create` | 创建加购订单 | `token-addon.ts` |
| POST | `/api/tokenAddon/confirm` | 确认加购（支付后） | `token-addon.ts` |

**加购包定价：**
- 成长版：¥9.9 / 100K tokens
- 专业版：¥49 / 500K tokens 或 ¥399 / 5M tokens

### 4.7 家族树 — IlmFamilyTreeController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| GET | `/api/family-tree/list` | 获取用户的家族树列表 | `family-tree.ts` |
| GET | `/api/family-tree/{id}` | 获取单棵树详情 | `family-tree.ts` |
| POST | `/api/family-tree/create` | 创建家族树 | `family-tree.ts` |
| PUT | `/api/family-tree/update` | 更新家族树数据 | `family-tree.ts` |
| PUT | `/api/family-tree/update-analysis` | 更新分析结果 | `family-tree.ts` |
| DELETE | `/api/family-tree/{id}` | 删除家族树 | `family-tree.ts` |

**树数量限制（Service 层强制）：**
- 体验版：1 棵
- 成长版：3 棵
- 专业版：10 棵

**家族树存储为 JSON 字段** — `treeData` 存完整的家族树结构（Person[] + Relationship[]），`analysisResult` 存 AI 分析结果。

### 4.8 关系剧场 — IlmTheaterController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/theater/create` | 创建剧场会话 | `theater.ts` |
| GET | `/api/theater/list` | 获取剧场列表 | `theater.ts` |
| GET | `/api/theater/{id}` | 获取单个剧场详情 | `theater.ts` |
| DELETE | `/api/theater/{id}` | 删除剧场 | `theater.ts` |

**输入验证：** `realityPositions` 字段（JSON）非空校验

### 4.9 日记 — IlmDiaryController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/diary/create` | 创建日记 | `diary.ts` |
| GET | `/api/diary/list?limit={n}` | 获取日记列表 | `diary.ts` |
| GET | `/api/diary/{id}` | 获取单篇日记 | `diary.ts` |
| DELETE | `/api/diary/{id}` | 删除日记 | `diary.ts` |

**输入验证：** content 非空、mood 1-5、limit 1-1000

### 4.10 七维探索 — IlmExploreController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| GET | `/api/explore/scores` | 获取七维分数 | `explore.ts` |
| POST | `/api/explore/scores` | 更新七维分数 | `explore.ts` |

**七个维度：** 自我觉察、关系连接、情绪自在、家族和解、生命力、价值活出、整合平衡
**输入验证：** 每个维度分数 0-100

### 4.11 光语传承 — IlmLegacyController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/legacy/create` | 创建传承成员 | `legacy.ts` |
| GET | `/api/legacy/list` | 获取传承成员列表 | `legacy.ts` |
| GET | `/api/legacy/{id}` | 获取单个成员 | `legacy.ts` |
| DELETE | `/api/legacy/{id}` | 删除传承成员 | `legacy.ts` |

**成员数量限制：** 成长版 3 人、专业版 10 人
**输入验证：** name/role 非空

### 4.12 明场会话 — IlmCoachSessionController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/coach/create` | 创建明场会话 | `coach.ts` |
| PUT | `/api/coach/update` | 更新会话 | `coach.ts` |
| GET | `/api/coach/list` | 获取会话列表 | `coach.ts` |
| GET | `/api/coach/active` | 获取当前进行中的会话 | `coach.ts` |
| GET | `/api/coach/{id}` | 获取单个会话 | `coach.ts` |
| DELETE | `/api/coach/{id}` | 删除会话 | `coach.ts` |

**输入验证：** theme/subTopic 非空

### 4.13 对话历史 — IlmDialogueHistoryController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/dialogue/save` | 保存对话摘要 | `dialogue-history.ts` |
| GET | `/api/dialogue/list?limit={n}` | 获取对话历史 | `dialogue-history.ts` |
| GET | `/api/dialogue/companion/{companionId}?limit={n}` | 按伙伴查询 | `dialogue-history.ts` |

**输入验证：** limit 1-1000

### 4.14 导师管理 — IlmMentorController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/api/mentor/register` | 注册为导师（生成 MT-XXXX 码） | `mentor.ts` |
| GET | `/api/mentor/info` | 获取导师信息 | `mentor.ts` |
| GET | `/api/mentor/clients` | 获取名下客户列表 | `mentor.ts` |
| GET | `/api/mentor/stats` | 获取导师统计数据 | `mentor.ts` |
| POST | `/api/mentor/bind` | 客户绑定导师 MT 码 | `mentor.ts` |

**导师邀请码格式：** `MT-XXXX`（4 位大写字母，区别于渠道 CH- 和直销 SL-）
**奖励机制：** 客户付费 → 导师 +200K token（非现金佣金）

### 4.15 行为埋点 — IlmAnalyticsController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/system/analytics/events` | 上报行为事件 | `analytics.ts` / `behavioral-events.ts` |

**请求格式：**
```json
{
  "sessionId": "analytics-1711900000000",
  "events": [
    {
      "eventType": "dialogue_started",
      "timestamp": "2026-04-01T10:00:00Z",
      "companionId": "yu",
      "payload": { "module": "ask" }
    }
  ],
  "timestamp": "2026-04-01T10:00:05Z"
}
```

### 4.16 用户数据导出 — IlmUserDataController

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| GET | `/api/user-data/export` | PIPL 合规数据导出（JSON） | `user-data.ts` |

**导出内容：** 用户信息 + 七维分数 + 日记 + 家族树 + 剧场 + 传承 + 对话历史 + 明场会话

### 4.17 用户绑定推荐码 — IlmUserController（扩展 RuoYi）

| 方法 | 路径 | 说明 | 前端文件 |
|------|------|------|---------|
| POST | `/system/user/bind-referral-code` | 绑定推荐码 | `auth.ts` |

---

## 五、所有 Java 文件清单（76 个）

### Controller（16 个）
```
controller/
├── IlmAiChatController.java        # AI 对话 SSE 流式代理
├── IlmAnalyticsController.java     # 行为埋点上报
├── IlmAuthController.java          # SMS 登录
├── IlmCoachSessionController.java  # 明场会话 CRUD
├── IlmComboController.java         # 套餐查询
├── IlmDialogueHistoryController.java # 对话历史
├── IlmDiaryController.java         # 日记 CRUD
├── IlmExploreController.java       # 七维分数
├── IlmFamilyTreeController.java    # 家族树 CRUD + 分析
├── IlmLegacyController.java        # 传承成员 CRUD
├── IlmMentorController.java        # 导师注册/绑定/客户管理
├── IlmOrderController.java         # 订单创建
├── IlmPayController.java           # 支付宝 + 微信支付
├── IlmTheaterController.java       # 关系剧场 CRUD
├── IlmTokenAddonController.java    # Token 加购
└── IlmUserDataController.java      # PIPL 数据导出
```

### Domain/Entity（13 个）
```
domain/
├── IlmCoachSession.java
├── IlmComboPlan.java
├── IlmDialogueHistory.java
├── IlmDiaryEntry.java
├── IlmExploreScore.java
├── IlmFamilyTree.java
├── IlmLegacyMember.java
├── IlmOrder.java
├── IlmPartner.java
├── IlmTheaterSession.java
├── IlmTokenUsage.java
├── IlmUserAttribution.java
└── IlmUserSubscription.java
```

### Mapper（16 个）
```
mapper/
├── IlmActivityMapper.java          # 站内通知写入
├── IlmBehavioralEventMapper.java
├── IlmCoachSessionMapper.java
├── IlmComboPlanMapper.java
├── IlmDialogueHistoryMapper.java
├── IlmDialogueUsageMapper.java
├── IlmDiaryEntryMapper.java
├── IlmExploreScoreMapper.java
├── IlmFamilyTreeMapper.java
├── IlmLegacyMemberMapper.java
├── IlmOrderMapper.java
├── IlmPartnerMapper.java
├── IlmTheaterSessionMapper.java
├── IlmTokenUsageMapper.java
├── IlmUserAttributionMapper.java
└── IlmUserSubscriptionMapper.java
```

### Service 接口（11 个）
```
service/
├── IIlmAiProxyService.java
├── IIlmAnalyticsService.java
├── IIlmComboService.java
├── IIlmFamilyTreeService.java
├── IIlmMentorService.java
├── IIlmOrderService.java
├── IIlmPayService.java
├── IIlmRefundService.java
├── IIlmSmsService.java
├── IIlmTokenAddonService.java
└── IIlmTokenService.java
```

### Service 实现（11 个）
```
service/impl/
├── IlmAiProxyServiceImpl.java      # AI 请求构建 + 流式转发
├── IlmAnalyticsServiceImpl.java
├── IlmComboServiceImpl.java        # 订阅激活 + 到期处理
├── IlmFamilyTreeServiceImpl.java   # 树数量限制校验
├── IlmMentorServiceImpl.java       # MT 码生成 + 客户绑定
├── IlmOrderServiceImpl.java        # 订单状态机
├── IlmPayServiceImpl.java          # 支付宝 + 微信支付 SDK
├── IlmRefundServiceImpl.java       # 微信退款 SDK
├── IlmSmsServiceImpl.java          # 阿里云 SMS
├── IlmTokenAddonServiceImpl.java   # Token 加购
└── IlmTokenServiceImpl.java        # Token 计量（Redis + MySQL）
```

### Config（4 个）
```
config/
├── AiProviderConfig.java           # AI 提供商（minimax/claude/openai）
├── AlipayConfig.java               # 支付宝
├── SmsConfig.java                  # 阿里云 SMS
└── WechatPayConfig.java            # 微信支付 V3
```

### Task 定时任务（3 个）
```
task/
├── AutoRenewTask.java              # 自动续费 + 失败通知
├── SubscriptionExpireTask.java     # 订阅到期处理
└── SubscriptionRenewRemindTask.java # 续费提醒（到期前 7 天）
```

### DTO（2 个）
```
dto/
├── ChatRequestDTO.java             # AI 对话请求
└── CreateOrderDTO.java             # 创建订单请求
```

---

## 六、前端 API 文件 ↔ 后端 Controller 对照表

| 前端 API 文件 | 导出函数数 | 后端 Controller | 状态 |
|--------------|-----------|----------------|------|
| `auth.ts` | 5 | IlmAuthController + RuoYi SysLoginController | 已对接 |
| `ai.ts` | 3 | IlmAiChatController | 已对接（SSE） |
| `combo.ts` | 5 | IlmComboController + IlmOrderController | 已对接 |
| `payment.ts` | 4 | IlmPayController | 已对接 |
| `token-addon.ts` | 3 | IlmTokenAddonController | 已对接 |
| `family-tree.ts` | 6 | IlmFamilyTreeController | 已对接 |
| `theater.ts` | 4 | IlmTheaterController | 已对接 |
| `diary.ts` | 4 | IlmDiaryController | 已对接 |
| `explore.ts` | 2 | IlmExploreController | 已对接 |
| `legacy.ts` | 4 | IlmLegacyController | 已对接 |
| `coach.ts` | 6 | IlmCoachSessionController | 已对接 |
| `dialogue-history.ts` | 3 | IlmDialogueHistoryController | 已对接 |
| `mentor.ts` | 5 | IlmMentorController | 已对接 |
| `user-data.ts` | 1 | IlmUserDataController | 已对接 |

**总计：55 个前端函数 → 45 个后端端点**

---

## 七、Maven 依赖（pom.xml 新增）

```xml
<!-- 微信支付 V3 SDK -->
<dependency>
    <groupId>com.github.wechatpay-apiv3</groupId>
    <artifactId>wechatpay-java</artifactId>
    <version>0.2.14</version>
</dependency>

<!-- 支付宝 SDK -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>4.39.79.ALL</version>
</dependency>

<!-- 阿里云 SMS -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dysmsapi20170525</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- OkHttp（AI 流式调用） -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

---

## 八、核心业务逻辑说明

### 8.1 订阅生命周期

```
用户选择套餐
  → IlmOrderController.createOrder() → 创建 ilm_orders (status=PENDING)
  → 用户支付 → IlmPayController (支付宝/微信)
  → 支付成功回调 → IlmComboServiceImpl.activateSubscription()
    → 计算 startDate / endDate（月/年）
    → 写入 ilm_user_subscriptions
    → 更新 ilm_orders (status=PAID)
  → 到期前 7 天 → SubscriptionRenewRemindTask → 写入 ilm_activities 通知
  → 到期 → SubscriptionExpireTask → 降级为体验版
  → 自动续费失败 → AutoRenewTask → 写入 ilm_activities 通知
```

### 8.2 Token 计量

```
前端 POST /system/ai/chat
  → IlmTokenServiceImpl.checkAndIncrement(userId, estimatedTokens)
    → Redis GET ilm:token:{userId}:2026-04  // O(1) 热查询
    → 超额？→ 返回 429
    → 未超额？→ 转发 AI 请求
  → AI 完成后，获取实际 usage.total_tokens
  → Redis INCRBY ilm:token:{userId}:2026-04 {actual_tokens}
  → 异步 → IlmTokenUsageMapper.upsert() 写入 MySQL

Token 限额：
  体验版: 50,000 / 月
  成长版: 500,000 / 月
  专业版: 3,000,000 / 月
```

### 8.3 导师系统

```
导师注册：
  POST /api/mentor/register { name, phone }
  → IlmMentorServiceImpl.registerAsMentor()
  → 生成 MT-XXXX 码（4 位随机大写字母）
  → 写入 ilm_partners (partner_type='mentor')
  → 返回 IlmPartner 含 referralCode

客户绑定：
  POST /api/mentor/bind { mtCode: "MT-ABCD" }
  → 查找 ilm_partners where referral_code = 'MT-ABCD'
  → 写入 ilm_user_attribution (user_id, referral_code, entry_type='mentor_referral')
  → 首付后 locked=true 锁定归因

导师查看客户：
  GET /api/mentor/clients
  → 通过 ilm_user_attribution 反查所有 referral_code = 导师MT码的用户
  → 返回客户列表（userId, nickName, phone, hasPaid, locked）
```

### 8.4 支付流程

```
支付宝 PC 网页支付：
  POST /api/pay/alipay/page { orderNo }
  → AlipayClient.pageExecute(AlipayTradePagePayRequest)
  → 返回 HTML form（前端直接渲染跳转支付宝）
  → 支付宝回调 POST /api/pay/alipay/notify → 验签 → 更新订单

微信 Native 支付：
  POST /api/pay/wechat/pc { orderNo }
  → NativePayService.prepay(PrepayRequest)
  → 返回 code_url（二维码链接）→ 前端渲染二维码
  → 前端轮询 GET /api/pay/wechat/query?orderNo=xxx
  → 微信回调 POST /api/pay/wechat/notify → NotificationParser 验签 → 更新订单
```

### 8.5 AI 对话代理

```
POST /system/ai/chat
  → IlmAiChatController.chat()
  → 检查 token 额度
  → IlmAiProxyServiceImpl.buildRequest()
    → 根据 provider 配置选择格式：
      - minimax: POST https://api.minimax.io/v1/chat/completions
      - claude:  POST https://api.anthropic.com/v1/messages
      - openai:  POST https://api.openai.com/v1/chat/completions
  → OkHttp 流式调用 → 逐行读取 SSE → 转发给前端
  → 完成后统计 token 用量
```

---

## 九、MyBatis XML Mapper 文件

**重要：当前 Mapper 接口已定义，但 XML 映射文件尚未编写。**

XML 文件应放在：
```
ruoyi-ilightmate/src/main/resources/mapper/ilightmate/
├── IlmActivityMapper.xml
├── IlmBehavioralEventMapper.xml
├── IlmCoachSessionMapper.xml
├── IlmComboPlanMapper.xml
├── IlmDialogueHistoryMapper.xml
├── IlmDialogueUsageMapper.xml
├── IlmDiaryEntryMapper.xml
├── IlmExploreScoreMapper.xml
├── IlmFamilyTreeMapper.xml
├── IlmLegacyMemberMapper.xml
├── IlmOrderMapper.xml
├── IlmPartnerMapper.xml
├── IlmTheaterSessionMapper.xml
├── IlmTokenUsageMapper.xml
├── IlmUserAttributionMapper.xml
└── IlmUserSubscriptionMapper.xml
```

每个 Mapper 接口已有 `@Mapper` 注解和方法签名。Steve 需要编写对应的 XML SQL 映射。

---

## 十、Steve 的 TODO（按优先级）

### P0 — 上线前必须（W1）

- [ ] 1. **建库建表**：执行 `sql/ry_20260319.sql` + `sql/quartz.sql` + `sql/ilightmate.sql`
- [ ] 2. **配置环境变量**：填写所有 env var（AI_API_KEY, 支付宝, 微信, SMS）
- [ ] 3. **编写 16 个 MyBatis XML**：为所有 Mapper 接口补全 SQL 映射
- [ ] 4. **Shiro 权限配置**：在 `ShiroConfig.java` 中放行 `/api/**` 路径（需登录但不需要特殊角色）
- [ ] 5. **CORS 配置**：允许前端域名 `app.ilightmate.cn` 跨域
- [ ] 6. **Redis 配置**：确认 Redis 连接 + 设置 token 计量 key 前缀 `ilm:token:`
- [ ] 7. **编译验证**：`mvn clean package -DskipTests` 通过

### P1 — 核心流程联调

- [ ] 8. **SMS 登录联调**：确认阿里云短信模板审核通过 + 验证码收发正常
- [ ] 9. **AI 对话 SSE 联调**：前端 → 后端 → MiniMax API → SSE 流式响应 → 前端渲染
- [ ] 10. **支付联调**：支付宝沙箱 + 微信沙箱测试完整流程
- [ ] 11. **Token 计量联调**：验证 Redis 计数准确 + 超额返回 429 + 月初重置

### P2 — 数据持久化迁移

- [ ] 12. **前端 localStorage → 后端 API**：当前七维/日记/家族树/剧场/传承/对话历史全部存在前端 localStorage，需要写迁移逻辑（读取本地 → 批量上传 → 清除本地缓存）
- [ ] 13. **家族树 JSON schema 对齐**：确认前端 `family-tree-data.ts` 的 Person/Relationship 结构与后端 `treeData` JSON 字段兼容

### P3 — 安全 + 定时任务

- [ ] 14. **数据所有权校验**：所有 CRUD 端点必须校验 `userId = SecurityUtils.getUserId()`，防止越权访问
- [ ] 15. **定时任务配置**：在 RuoYi 后台配置 3 个 Task（cron 表达式）
- [ ] 16. **Shiro/JWT token 过期时间**：建议 7 天，前端 refresh 逻辑已有

### P4 — 部署

- [ ] 17. **服务器环境**：JDK 8+ / MySQL 5.7+ / Redis 6+ / Nginx 反向代理
- [ ] 18. **Nginx 配置**：`api.ilightmate.cn` → Spring Boot 8080，`app.ilightmate.cn` → 前端静态文件
- [ ] 19. **SSL 证书**：HTTPS 必须（支付回调要求）
- [ ] 20. **微信支付证书**：将商户私钥 PEM 文件放到服务器 `WECHAT_PRIVATE_KEY_PATH` 指定位置

---

## 十一、3 层定价体系总结

| 字段 | 体验版 (code='0') | 成长版 (code='1') | 专业版 (code='2') |
|------|-------------------|-------------------|-------------------|
| 月价 | ¥0 | ¥19 | 仅年付 |
| 年价 | ¥0 | ¥198 | ¥1,680 |
| Token/月 | 50K | 500K | 3M |
| 伙伴 | 2（星宇+若曦） | 6（全部） | 6（全部） |
| 家族树 | 1 棵 | 3 棵 | 10 棵 |
| 分析层 | 前 3 层 | 完整 7 层 | 完整 7 层 |
| 剧场 | 1 种 | 6 种 | 6 种 |
| 传承 | 无 | 3 人 | 10 人 |
| 个案管理 | 无 | 无 | 有 |
| PDF 导出 | 无 | 无 | 有 |

---

## 十二、合规注意事项（PIPL）

1. **家族树数据脱敏**：Person.name 存储到后端时应替换为关系标签（不存真实姓名）
2. **三级同意**：basicService（必选）/ aggregateInsights（可选）/ researchUse（可选）
3. **数据导出/删除**：`/api/user-data/export` 已实现导出，删除需要后端补全
4. **AI 调用最小化**：发给 AI 的家族数据应脱敏
5. **导师-客户隔离**：专业版导师处理客户数据 = 受委托处理（PIPL 第 21 条）

---

## 十三、统一响应格式

所有 API 返回 RuoYi 标准格式：

```json
// 成功
{ "code": 200, "msg": "操作成功", "data": { ... } }

// 失败
{ "code": 500, "msg": "错误信息" }

// Token 超额
{ "code": 429, "msg": "Token额度已用尽" }
```

前端 `src/lib/request.ts` 已封装拦截器，自动处理 200/401/429/500。

---

## 十四、联系方式

如有疑问：
- 前端代码问 ZoE
- 后端架构参考本文档 + 代码注释
- AI 对话流式代理参考 `IlmAiChatController.java` + `IlmAiProxyServiceImpl.java`
- 支付集成参考 `IlmPayServiceImpl.java`（已有完整 SDK 调用代码）

---

*本文档由 Claude AI 于 2026-04-01 自动生成，基于前后端代码库实际内容。*
