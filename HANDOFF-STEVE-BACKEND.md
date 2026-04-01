# iLightMate 后端交接文档 — Steve

> 日期：2026-03-31（v2 更新）
> 项目：ruoyi-ilightmate-backend
> GitHub：https://github.com/sisuy/ilightmate-backend
> 基于：RuoYi 4.8.3（https://gitee.com/y_project/RuoYi）
> 状态：**业务逻辑全部完成（45 个 Java 文件 / 3,065 行），需要配环境变量 + 微信 SDK 填充 + 编译测试**

---

## 一、项目概览

```
ruoyi-ilightmate-backend/
├── ruoyi-admin/              ← 启动入口（已修改 pom + application.yml）
├── ruoyi-framework/          ← JWT + Redis + 拦截器（RuoYi 自带）
├── ruoyi-system/             ← sys_user + sys_role（RuoYi 自带）
├── ruoyi-common/             ← AjaxResult + 工具类（RuoYi 自带）
├── ruoyi-quartz/             ← 定时任务（用于订阅过期 + 续费提醒 + 自动续费）
├── ruoyi-ilightmate/         ← ★ iLightMate 业务模块
│   ├── controller/   (7)     ← API 端点
│   ├── service/      (9 接口 + 9 实现)  ← 业务逻辑
│   ├── mapper/       (7)     ← MyBatis 数据访问
│   ├── domain/       (4)     ← 实体类
│   ├── dto/          (2)     ← 请求 DTO
│   ├── config/       (4)     ← AI/支付/SMS 配置
│   └── task/         (3)     ← 定时任务
├── sql/
│   └── ilightmate.sql        ← 19 张表 + 种子数据
└── HANDOFF-STEVE-BACKEND.md  ← 本文件
```

---

## 二、启动步骤

### Step 1: 环境要求

```
JDK 17+  |  MySQL 5.7+  |  Redis 6+  |  Maven 3.8+
```

### Step 2: 建库建表

```bash
mysql -u root -p -e "CREATE DATABASE ruoyi_ilm DEFAULT CHARSET utf8mb4;"
mysql -u root -p ruoyi_ilm < sql/ry_20240601.sql    # RuoYi 系统表
mysql -u root -p ruoyi_ilm < sql/quartz.sql          # 定时任务表
mysql -u root -p ruoyi_ilm < sql/ilightmate.sql      # ★ iLightMate 19 张业务表
```

### Step 3: 配置数据库

`ruoyi-admin/src/main/resources/application-druid.yml`：
```yaml
spring.datasource.druid.master:
  url: jdbc:mysql://localhost:3306/ruoyi_ilm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
  username: root
  password: YOUR_PASSWORD
```

### Step 4: 配置环境变量

| 变量 | 说明 | 必填 | 开发模式 |
|------|------|------|---------|
| `AI_API_KEY` | AI API Key（MiniMax/Claude） | 是 | 需要 |
| `ALIYUN_ACCESS_KEY_ID` | 阿里云 SMS | 否 | 关闭 SMS 时不需要 |
| `ALIYUN_ACCESS_KEY_SECRET` | 阿里云 SMS | 否 | 同上 |
| `ALIYUN_SMS_TEMPLATE` | 短信模板编号 | 否 | 同上 |
| `ALIPAY_APP_ID` | 支付宝 | 否 | 不测支付时不需要 |
| `ALIPAY_PRIVATE_KEY` | 支付宝 | 否 | 同上 |
| `ALIPAY_PUBLIC_KEY` | 支付宝 | 否 | 同上 |
| `WECHAT_APP_ID` | 微信支付 | 否 | 同上 |
| `WECHAT_MCH_ID` | 微信商户号 | 否 | 同上 |
| `WECHAT_API_KEY_V3` | 微信 V3 密钥 | 否 | 同上 |
| `WECHAT_CERT_SERIAL_NO` | 微信证书序列号 | 否 | 同上 |
| `WECHAT_PRIVATE_KEY_PATH` | 微信私钥路径 | 否 | 同上 |

**开发模式最少只需要 `AI_API_KEY`。** SMS 关闭时验证码打印到控制台。

### Step 5: 编译启动

```bash
mvn clean package -DskipTests
cd ruoyi-admin && mvn spring-boot:run
```

### Step 6: 验证

```bash
curl http://localhost/api/combo/query
# 应返回 4 条套餐（体验版月付 + 成长版月付 + 成长版年付 + 专业版年付）
```

---

## 三、完整 API 清单（16 个端点）

### 认证（5 个）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/resource/sms/code/login?phonenumber=X` | 发短信验证码 | 无 |
| POST | `/auth/smsLogin` | 短信登录 | 无 |
| POST | `/auth/login` | 密码登录（RuoYi 自带） | 无 |
| GET | `/system/user/getInfo` | 获取用户信息 | Bearer |
| POST | `/system/user/bind-referral-code` | 绑定推荐码 | Bearer |

### AI 对话（1 个）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/system/ai/chat` | SSE 流式对话 + 服务端 token 计量 | Bearer |

### 套餐（3 个）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/consultant/user/combo/{userId}` | 用户当前套餐 | Bearer |
| GET | `/api/combo/query` | 套餐列表 | 无 |
| GET | `/consultant/combo/{comboId}/benefits` | 套餐权益 | 无 |

### 支付（5 个）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/comboOrder/create-minimal` | 创建订单 | Bearer |
| POST | `/api/pay/alipay/page` | 支付宝支付 | Bearer |
| POST | `/api/pay/wechat/pc` | 微信支付 | Bearer |
| GET | `/api/pay/alipay/query` | 支付宝查询 | Bearer |
| GET | `/api/pay/wechat/query` | 微信查询 | Bearer |

### Token 加购（2 个）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/tokenAddon/list` | 可用加购包列表 | Bearer |
| POST | `/api/tokenAddon/create` | 创建加购包订单 | Bearer |

### 回调（不需要认证，2 个）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/pay/alipay/notify` | 支付宝异步回调 |
| POST | `/api/pay/wechat/notify` | 微信异步回调 |

### 埋点（1 个）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/system/analytics/events` | 行为事件上报 | Bearer |

---

## 四、完整业务逻辑清单

### 订阅生命周期

```
用户注册
  → 体验版（默认，免费，50K tokens/月）
  → 可获得试用（成长版 7 天 / 专业版 10 天 / 邀请奖励 7 天）

购买成长版月付（首月 ¥9.9，之后 ¥19/月）
  → createOrder() 自动检测首次购买 → 应用首月特惠
  → 支付成功 → markPaid()
    → activateSubscription()：月付 +1 月
    → 首付锁定归因
    → 如果是导师邀请客户 → 导师 +200K token

购买成长版年付（¥198/年）
  → activateSubscription()：年付 +1 年

升级专业版（¥1,680/年）
  → activateSubscription()：
    → 旧订阅立即过期
    → 新订阅从当前时间开始 +1 年

续费（同级）
  → activateSubscription()：从 end_time 延长（不是从今天）

到期
  → SubscriptionExpireTask（每天 00:05）自动标记过期
  → 用户降为体验版

续费提醒
  → SubscriptionRenewRemindTask（每天 10:00）
    → 3 天前提醒
    → 当天提醒
    → 过期 1 天后最后挽回

自动续费（月付代扣）
  → AutoRenewTask（每天 08:30）
    → 查找今天到期的月付 + 有代扣签约的用户
    → 调用代扣 → 成功则自动续费
```

### 支付后完整流程

```
支付回调 → markPaid(orderNo, payType, transactionNo)
  │
  ├─ 加购包订单？（remark 以 TOKEN_ADDON: 开头）
  │   → tokenService.addBonusTokens() → 完成
  │
  ├─ 自动续费订单？（remark 以 AUTO_RENEW: 开头）
  │   → comboService.activateSubscription() → 完成
  │
  └─ 普通订阅订单
      → comboService.activateSubscription()
      → 首次付费？
          → 锁定归因（attributionMapper.lockAttribution）
          → 检查是否导师邀请的客户
              → 是：给导师 +200K token
      → 完成
```

### Token 计量

```
AI 对话请求 → IlmAiChatController
  → 检查月度 token 额度（Redis 热路径）
    → 超额 → 429 "Token额度已用尽"
  → 检查每日对话次数
    → 超额 → 429 "今日对话次数已用完"
  → 转发 AI（Claude/MiniMax SSE 流式）
  → 响应后 Redis INCRBY 计入实际 token
  → 异步写入 MySQL
```

| 套餐 | 月 Token | 日对话 | 加购 |
|------|---------|--------|------|
| 体验版 | 50K | 5 次 | 不支持 |
| 成长版 | 500K | 无限 | ¥9.9/100K |
| 专业版 | 3M | 无限 | ¥49/500K 或 ¥399/5M |

### 退款

```
refund(orderNo, reason, amount)
  → 调支付宝/微信退款 API
  → 订单状态 → REFUNDED
  → 用户所有活跃订阅 → 过期
  → 用户降为体验版
```

---

## 五、你需要做的事

### 必须（上线前）

| # | 任务 | 时间 | 说明 |
|---|------|------|------|
| 1 | 配环境变量 + 编译启动 | 30 分钟 | 至少配 AI_API_KEY |
| 2 | RuoYi 安全配置放行 | 1 小时 | 见下方 |
| 3 | 微信支付 SDK 填充 | 2-3 小时 | IlmPayServiceImpl 4 个 TODO |
| 4 | 测试 SMS 登录 | 30 分钟 | 开发模式验证码在控制台 |
| 5 | 测试 AI 对话 SSE | 30 分钟 | curl 测试 |
| 6 | 后台配置 3 个定时任务 | 15 分钟 | 见下方 |

### 后续（W2+）

| # | 任务 | 时间 | 说明 |
|---|------|------|------|
| 7 | 用户数据 CRUD API（11 张表） | 5-7 天 | 替代前端 localStorage |
| 8 | 微信代扣签约 | 2-3 天 | AutoRenewTask 里的 TODO |
| 9 | 推送服务对接 | 1-2 天 | 续费提醒的 sendRemind() |
| 10 | 后台管理 API | 按需 | 替换前端 mock |

### RuoYi 安全配置放行

在 ShiroConfig.java 或 SecurityConfig.java 中添加：

```java
// 不需要登录的接口
filterChainDefinitionMap.put("/api/combo/query", "anon");
filterChainDefinitionMap.put("/consultant/combo/**", "anon");
filterChainDefinitionMap.put("/resource/sms/code/login", "anon");
filterChainDefinitionMap.put("/auth/smsLogin", "anon");
filterChainDefinitionMap.put("/api/pay/alipay/notify", "anon");
filterChainDefinitionMap.put("/api/pay/wechat/notify", "anon");
```

XSS 过滤也需要排除支付回调：

```yaml
# application.yml
xss:
  excludes: /system/notice/*,/api/pay/alipay/notify,/api/pay/wechat/notify
```

### 定时任务配置

在 RuoYi 后台「系统监控 → 定时任务」中添加 3 个任务：

| 任务名称 | 调用目标 | Cron | 说明 |
|---------|---------|------|------|
| 订阅过期检查 | `ilmSubscriptionExpireTask.execute()` | `0 5 0 * * ?` | 每天 00:05 |
| 续费提醒 | `ilmSubscriptionRenewRemindTask.execute()` | `0 0 10 * * ?` | 每天 10:00 |
| 自动续费 | `ilmAutoRenewTask.execute()` | `0 30 8 * * ?` | 每天 08:30 |

### 微信支付 TODO 位置（4 个）

文件：`IlmPayServiceImpl.java`

| 位置 | 方法 | 需要做什么 |
|------|------|---------|
| 1 | `createWechatNative()` | 用 NativePayService.prepay() 生成二维码 URL |
| 2 | `queryWechat()` | 用 queryOrderByOutTradeNo() 查询订单状态 |
| 3 | `handleWechatNotify()` | 验签 + 解密 + 解析 Transaction |

文件：`IlmRefundServiceImpl.java`

| 位置 | 方法 | 需要做什么 |
|------|------|---------|
| 4 | `refundWechat()` | 用 RefundService.create() 发起退款 |

SDK 文档：https://github.com/wechatpay-apiv3/wechatpay-java

---

## 六、文件清单（45 个 Java 文件）

### Controller（7 个）

| 文件 | 端点 |
|------|------|
| `IlmAuthController.java` | SMS 登录 + 推荐码绑定 |
| `IlmAiChatController.java` | AI 对话 SSE + token 检查 |
| `IlmComboController.java` | 套餐查询 |
| `IlmOrderController.java` | 订单创建 + 查询 |
| `IlmPayController.java` | 支付宝 + 微信支付 + 回调 |
| `IlmTokenAddonController.java` | Token 加购包 |
| `IlmAnalyticsController.java` | 行为埋点上报 |

### Service（9 接口 + 9 实现 = 18 个）

| 接口 | 实现 | 职责 |
|------|------|------|
| `IIlmSmsService` | `IlmSmsServiceImpl` | 阿里云 SMS + Redis 验证码 + JWT |
| `IIlmAiProxyService` | `IlmAiProxyServiceImpl` | AI SSE 流式代理（Claude/MiniMax） |
| `IIlmTokenService` | `IlmTokenServiceImpl` | Redis 热路径 token 计量 |
| `IIlmComboService` | `IlmComboServiceImpl` | 订阅管理（新购/续费/升级/试用） |
| `IIlmOrderService` | `IlmOrderServiceImpl` | 订单（首月特惠/归因锁定/导师奖励） |
| `IIlmPayService` | `IlmPayServiceImpl` | 支付宝 SDK + 微信 V3 |
| `IIlmTokenAddonService` | `IlmTokenAddonServiceImpl` | Token 加购包 |
| `IIlmRefundService` | `IlmRefundServiceImpl` | 退款 + 撤销订阅 |
| `IIlmAnalyticsService` | `IlmAnalyticsServiceImpl` | 行为事件批量存储 |

### Mapper（7 个）

| 文件 | 表 |
|------|---|
| `IlmTokenUsageMapper` | `ilm_token_usage` |
| `IlmDialogueUsageMapper` | `ilm_dialogue_usage` |
| `IlmComboPlanMapper` | `ilm_combo_plans` |
| `IlmUserSubscriptionMapper` | `ilm_user_subscriptions` |
| `IlmOrderMapper` | `ilm_orders` |
| `IlmUserAttributionMapper` | `ilm_user_attribution` |
| `IlmBehavioralEventMapper` | `ilm_behavioral_events` |

### Domain + DTO（6 个）

| 文件 | 说明 |
|------|------|
| `IlmTokenUsage` | 月度 token |
| `IlmComboPlan` | 套餐定义 |
| `IlmUserSubscription` | 用户订阅 |
| `IlmOrder` | 订单 |
| `ChatRequestDTO` | AI 对话请求 |
| `CreateOrderDTO` | 创建订单请求 |

### Config（4 个）

| 文件 | 配置前缀 |
|------|---------|
| `AiProviderConfig` | `ilightmate.ai.*` |
| `AlipayConfig` | `ilightmate.alipay.*` |
| `WechatPayConfig` | `ilightmate.wechat.*` |
| `SmsConfig` | `ilightmate.sms.*` |

### 定时任务（3 个）

| 文件 | Bean 名称 | Cron |
|------|----------|------|
| `SubscriptionExpireTask` | `ilmSubscriptionExpireTask` | `0 5 0 * * ?` |
| `SubscriptionRenewRemindTask` | `ilmSubscriptionRenewRemindTask` | `0 0 10 * * ?` |
| `AutoRenewTask` | `ilmAutoRenewTask` | `0 30 8 * * ?` |

---

## 七、数据库表（19 张）

| 表名 | 说明 | W1 必须 |
|------|------|--------|
| `ilm_token_usage` | 月度 token 计量 | 是 |
| `ilm_dialogue_usage` | 每日对话计数 | 是 |
| `ilm_combo_plans` | 套餐目录（含种子数据） | 是 |
| `ilm_user_subscriptions` | 用户订阅 | 是 |
| `ilm_orders` | 订单 | 是 |
| `ilm_partners` | 推荐伙伴 | 是 |
| `ilm_user_attribution` | 用户归因 | 是 |
| `ilm_behavioral_events` | 行为事件 | 是 |
| `ilm_explore_scores` | 七维分数 | W2 |
| `ilm_diary_entries` | 日记 | W2 |
| `ilm_theater_sessions` | 剧场记录 | W2 |
| `ilm_legacy_members` | 传承成员 | W2 |
| `ilm_emotion_tags` | 情绪标签 | W2 |
| `ilm_activities` | 活动记录 | W2 |
| `ilm_dialogue_history` | 对话摘要 | W2 |
| `ilm_coach_sessions` | 明场教练会话 | W2 |
| `ilm_user_consent` | PIPL 同意 | W2 |
| `ilm_user_onboarding` | 引导状态 | W2 |

另外 `sys_user` 表新增了 `invite_code` 和 `sys_language` 两个字段。

---

## 八、Redis 键规范

| 键 | 用途 | TTL |
|----|------|-----|
| `ilm:token:{userId}:{YYYY-MM}` | 月度 token 计量 | 35 天 |
| `ilm:dialogue:{userId}:{YYYY-MM-DD}` | 每日对话计数 | 48 小时 |
| `ilm:sms:{phone}` | SMS 验证码 | 5 分钟 |

---

## 九、CORS 配置

```java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:5173", "https://app.ilightmate.cn")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true);
```

---

## 十、关联文档

| 文件 | 位置 | 说明 |
|------|------|------|
| 前端交接文档 | `ilightmate/HANDOFF-STEVE.md` | API 契约 + Store 迁移 |
| 上线计划 | `ilightmate/LAUNCH-PLAN.md` | 12 周路线图 + 团队分工 |
| 定价共识 | `ilightmate/src/config/pricing-consensus.md` | 3 层定价详细说明 |
| Sahil 策略 | `ilightmate/SAHIL-STRATEGY.md` | 定价 + 营销 + 增长策略 |

---

> **核心信息：45 个 Java 文件全部写好，业务逻辑完整。你只需要配密钥 + 填微信 SDK 4 个 TODO + 配 3 个定时任务 + 编译启动。**
