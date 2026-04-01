# iLightMate 后端交接文档 — Steve

> 日期：2026-03-31
> 项目：ruoyi-ilightmate-backend
> 基于：RuoYi 4.8.3（https://gitee.com/y_project/RuoYi）
> 状态：**核心代码已完成，需要配置环境变量 + 微信支付 SDK 初始化 + 编译测试**

---

## 一、项目概览

```
ruoyi-ilightmate-backend/
├── ruoyi-admin/              ← 启动入口（已修改 pom.xml 引入 ilightmate 模块）
├── ruoyi-framework/          ← JWT + Redis + 拦截器（RuoYi 自带，不改）
├── ruoyi-system/             ← sys_user + sys_role（RuoYi 自带，不改）
├── ruoyi-common/             ← 工具类 + AjaxResult（RuoYi 自带，不改）
├── ruoyi-generator/          ← 代码生成器（可选）
├── ruoyi-quartz/             ← 定时任务（订阅过期检查用）
├── ruoyi-ilightmate/         ← ★ iLightMate 业务模块（我写的）
│   ├── controller/  (6)
│   ├── service/     (7 接口 + 7 实现)
│   ├── mapper/      (7)
│   ├── domain/      (4)
│   ├── dto/         (2)
│   └── config/      (4)
├── sql/
│   └── ilightmate.sql        ← ★ 19 张业务表 + 种子数据
└── pom.xml                   ← 已添加 ruoyi-ilightmate 模块
```

**总计：37 个 Java 文件，2,221 行代码，19 张 MySQL 表。**

---

## 二、启动步骤

### Step 1: 环境准备

```bash
# 要求
JDK 17+
MySQL 5.7+
Redis 6+
Maven 3.8+
```

### Step 2: 建库建表

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE ruoyi_ilm DEFAULT CHARSET utf8mb4;"

# 2. 导入 RuoYi 系统表（在 sql/ 目录下找 ry_*.sql）
mysql -u root -p ruoyi_ilm < sql/ry_20240601.sql
mysql -u root -p ruoyi_ilm < sql/quartz.sql

# 3. 导入 iLightMate 业务表
mysql -u root -p ruoyi_ilm < sql/ilightmate.sql
```

### Step 3: 配置数据库连接

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/ruoyi_ilm?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: YOUR_PASSWORD
```

### Step 4: 配置环境变量

在 `application.yml` 末尾已添加 `ilightmate.*` 配置块。以下环境变量需要设置：

| 变量 | 说明 | 示例 |
|------|------|------|
| `AI_API_KEY` | AI 提供商 API Key | `sk-xxxxxxxx` |
| `ALIYUN_ACCESS_KEY_ID` | 阿里云 SMS | `LTAI5txxxxxxx` |
| `ALIYUN_ACCESS_KEY_SECRET` | 阿里云 SMS | `xxxxxxxxxx` |
| `ALIYUN_SMS_TEMPLATE` | 短信模板编号 | `SMS_12345678` |
| `ALIPAY_APP_ID` | 支付宝应用 ID | `2021xxxxxxxx` |
| `ALIPAY_PRIVATE_KEY` | 支付宝应用私钥 | RSA2 密钥 |
| `ALIPAY_PUBLIC_KEY` | 支付宝公钥 | RSA2 公钥 |
| `WECHAT_APP_ID` | 微信支付 AppID | `wx1234567890` |
| `WECHAT_MCH_ID` | 微信商户号 | `1234567890` |
| `WECHAT_API_KEY_V3` | 微信支付 V3 密钥 | `xxxxxxxxxx` |
| `WECHAT_CERT_SERIAL_NO` | 微信证书序列号 | `xxxxxxxxxx` |
| `WECHAT_PRIVATE_KEY_PATH` | 微信私钥文件路径 | `/path/to/apiclient_key.pem` |

**开发模式可以先不配支付和 SMS。** SMS 关闭时验证码会打印到控制台日志。

### Step 5: 编译启动

```bash
cd ruoyi-ilightmate-backend
mvn clean package -DskipTests
cd ruoyi-admin
mvn spring-boot:run
```

或者用 IDE（IntelliJ IDEA）直接运行 `RuoYiApplication.java`。

### Step 6: 验证

```bash
# 健康检查
curl http://localhost/

# 获取套餐列表（不需要登录）
curl http://localhost/api/combo/query

# 应该返回 3 个套餐（体验版/成长版/专业版）
```

---

## 三、API 端点清单（13 个）

前端已经按这些路径写好了调用代码（`ilightmate/src/api/*.ts`），后端只需要返回匹配的 JSON 格式。

### 认证

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 1 | GET | `/resource/sms/code/login?phonenumber=X` | 发送短信验证码 | 无 |
| 2 | POST | `/auth/smsLogin` | 短信登录 | 无 |
| 3 | POST | `/auth/login` | 密码登录（RuoYi 自带） | 无 |
| 4 | GET | `/system/user/getInfo` | 获取用户信息（RuoYi 自带） | Bearer |
| 5 | POST | `/system/user/bind-referral-code` | 绑定推荐码 | Bearer |

### AI 对话

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 6 | POST | `/system/ai/chat` | AI 对话（SSE 流式） | Bearer |

**关键逻辑：**
- 请求前检查 token 额度（Redis `ilm:token:{userId}:{YYYY-MM}`）
- 超额返回 429
- 转发到 Claude/MiniMax（根据 `ilightmate.ai.provider` 配置）
- SSE 格式：`data: {"content":"..."}\n\n` + `data: [DONE]\n\n`
- 响应后 Redis INCRBY 计入实际 token 数
- 异步写入 MySQL `ilm_token_usage`

### 套餐

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 7 | GET | `/consultant/user/combo/{userId}` | 用户当前套餐 | Bearer |
| 8 | GET | `/api/combo/query` | 所有套餐列表 | 无 |
| 9 | GET | `/consultant/combo/{comboId}/benefits` | 套餐权益 | 无 |

### 支付

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 10 | POST | `/api/comboOrder/create-minimal` | 创建订单 | Bearer |
| 11 | POST | `/api/pay/alipay/page` | 支付宝支付 | Bearer |
| 12 | POST | `/api/pay/wechat/pc` | 微信支付 | Bearer |
| 13 | GET | `/api/pay/alipay/query` | 支付宝查询 | Bearer |
| 13b | GET | `/api/pay/wechat/query` | 微信查询 | Bearer |

**回调（不需要认证）：**
- POST `/api/pay/alipay/notify` — 支付宝异步回调
- POST `/api/pay/wechat/notify` — 微信异步回调

### 埋点

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 14 | POST | `/system/analytics/events` | 行为事件批量上报 | Bearer |

---

## 四、数据库表（19 张）

### W1 核心表（8 张，必须在上线前可用）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `ilm_token_usage` | 月度 token 计量 | user_id, month_key(YYYY-MM), used, bonus |
| `ilm_dialogue_usage` | 每日对话计数 | user_id, date_key, count |
| `ilm_combo_plans` | 套餐目录 | combo_code(0/1/2), price, monthly_token_limit |
| `ilm_user_subscriptions` | 用户当前订阅 | user_id, combo_id, start_time, end_time |
| `ilm_orders` | 订单 | order_no, pay_amount, pay_status, pay_type |
| `ilm_partners` | 推荐伙伴 | partner_type, referral_code(CH/SL/MT) |
| `ilm_user_attribution` | 用户归因 | user_id, entry_type, referral_code, locked |
| `ilm_behavioral_events` | 行为事件 | event_type, companion_id, payload(JSON) |

### W2 用户数据表（11 张，W1 建表但 API 可以后补）

| 表名 | 说明 |
|------|------|
| `ilm_explore_scores` | 七维分数 |
| `ilm_diary_entries` | 日记 |
| `ilm_theater_sessions` | 剧场记录 |
| `ilm_legacy_members` | 传承成员 |
| `ilm_emotion_tags` | 情绪标签 |
| `ilm_activities` | 活动记录 |
| `ilm_dialogue_history` | 对话摘要 |
| `ilm_coach_sessions` | 明场教练会话 |
| `ilm_user_consent` | PIPL 同意状态 |
| `ilm_user_onboarding` | 引导状态 |

### 种子数据

`ilm_combo_plans` 已包含 4 条种子数据（体验版月付 + 成长版月付 + 成长版年付 + 专业版年付）。

### sys_user 扩展

已在 SQL 中添加：
```sql
ALTER TABLE sys_user ADD COLUMN invite_code VARCHAR(20);
ALTER TABLE sys_user ADD COLUMN sys_language VARCHAR(10) DEFAULT 'zh-CN';
```

---

## 五、你需要做的 3 件事

### 1. 配置环境变量（30 分钟）

参照上面 Step 4 的表格，把 AI/SMS/支付的密钥配进去。开发阶段可以先只配 `AI_API_KEY`，SMS 设 `enabled: false`，支付先不配。

### 2. 微信支付 SDK 初始化（2-3 小时）

`IlmPayServiceImpl.java` 中有 3 个 `// TODO` 标记，需要用 `wechatpay-java` SDK 填充：

**位置 1：`createWechatNative()`** — 创建 Native 支付
```java
// 需要初始化 NativePayService，调用 prepay()，返回 codeUrl
```

**位置 2：`queryWechat()`** — 查询微信订单
```java
// 需要调用 queryOrderByOutTradeNo()
```

**位置 3：`handleWechatNotify()`** — 处理异步回调
```java
// 需要验签 + 解密 + 解析 Transaction
```

参考文档：https://github.com/wechatpay-apiv3/wechatpay-java

### 3. RuoYi 安全配置调整（1 小时）

需要在 RuoYi 的 Shiro/Security 配置中放行以下路径（不需要登录的接口）：

```java
// ShiroConfig.java 或 SecurityConfig.java 中添加：
filterChainDefinitionMap.put("/api/combo/query", "anon");
filterChainDefinitionMap.put("/resource/sms/code/login", "anon");
filterChainDefinitionMap.put("/auth/smsLogin", "anon");
filterChainDefinitionMap.put("/api/pay/alipay/notify", "anon");
filterChainDefinitionMap.put("/api/pay/wechat/notify", "anon");
```

同时确保 `/system/ai/chat` 需要登录（默认就需要）。

---

## 六、Token 计量机制详解

这是最核心的服务端逻辑，直接影响收入。

### 流程

```
前端 POST /system/ai/chat
  ↓
IlmAiChatController.chat()
  ↓
1. tokenService.isMonthlyLimitReached(userId)
   → Redis GET ilm:token:{userId}:2026-03
   → 如果 >= monthlyTokenLimit → 返回 429
  ↓
2. tokenService.isDailyDialogueLimitReached(userId)
   → Redis GET ilm:dialogue:{userId}:2026-03-31
   → 如果 >= dailyDialogueLimit → 返回 429
  ↓
3. aiProxyService.streamChat(req, writer, userId)
   → OkHttp 调 Claude/MiniMax API
   → SSE 逐行转发给前端
   → 返回 tokensUsed（从 AI response.usage 提取）
  ↓
4. tokenService.incrementTokenUsage(userId, tokensUsed)
   → Redis INCRBY ilm:token:{userId}:2026-03 {tokensUsed}
   → @Async 写入 MySQL ilm_token_usage
  ↓
5. tokenService.incrementDialogueCount(userId)
   → Redis INCRBY ilm:dialogue:{userId}:2026-03-31 1
   → @Async 写入 MySQL ilm_dialogue_usage
```

### 额度上限

| 套餐 | 月 Token | 日对话 |
|------|---------|--------|
| 体验版 | 50,000 | 5 |
| 成长版 | 500,000 | -1（无限） |
| 专业版 | 3,000,000 | -1（无限） |

### 加购包

当用户购买 token 加购包时，调用 `tokenService.addBonusTokens(userId, tokens)`，在 MySQL 中增加 bonus 字段。

---

## 七、AI 对话代理详解

### 支持的 AI 提供商

| 提供商 | application.yml 中 provider 值 | API 格式 |
|--------|------------------------------|----------|
| MiniMax | `minimax` | OpenAI 兼容 |
| Claude | `claude` | Anthropic 格式 |
| OpenAI/兼容 | `openai` | 标准 OpenAI |

### SSE 流式格式

前端期望的格式：
```
data: {"content":"你"}
data: {"content":"好"}
data: {"content":"，我是若曦"}
data: [DONE]
```

后端从 AI 提供商收到的原始格式会被转换为上述统一格式。

### 切换 AI 提供商

修改 `application.yml`：
```yaml
ilightmate:
  ai:
    provider: claude
    api-url: https://api.anthropic.com/v1/messages
    api-key: sk-ant-api03-xxxxxxx
    model-id: claude-sonnet-4-20250514
```

---

## 八、SMS 登录流程

```
1. 前端 GET /resource/sms/code/login?phonenumber=13800000000
   ↓
2. 后端生成 6 位随机码，存入 Redis（5 分钟过期）
   key: ilm:sms:13800000000 → "123456"
   ↓
3. 通过阿里云 SMS 发送（生产）/ 打印日志（开发）
   ↓
4. 前端 POST /auth/smsLogin { phonenumber, smsCode }
   ↓
5. 后端从 Redis 取验证码，比对
   ↓
6. 查 sys_user：存在 → 登录；不存在 → 自动注册
   ↓
7. 通过 RuoYi TokenService 签发 JWT
   ↓
8. 返回 { access_token, userInfo }
```

**开发模式：** `ilightmate.sms.enabled: false` 时，验证码不发短信，直接打印到控制台：
```
[DEV MODE] SMS code for 13800000000: 123456
```

---

## 九、支付流程

### 支付宝（已完整实现）

```
创建订单 → 调支付宝 SDK 生成支付页面 → 前端跳转 → 用户支付
→ 支付宝异步回调 /api/pay/alipay/notify → 验签 → markPaid → 激活订阅
→ 前端轮询 /api/pay/alipay/query 确认状态
```

### 微信（框架完整，3 个 TODO 需填充）

```
创建订单 → 调微信 SDK 生成二维码 URL → 前端展示 → 用户扫码
→ 微信异步回调 /api/pay/wechat/notify → 验签解密 → markPaid → 激活订阅
→ 前端轮询 /api/pay/wechat/query 确认状态
```

### 支付成功后的订阅激活

`IlmOrderServiceImpl.markPaid()` → `IlmComboServiceImpl.activateSubscription()`：
1. 更新订单状态为 PAID
2. 创建 `ilm_user_subscriptions` 记录
3. 根据 billingCycle 计算 end_time（月付 +1 月，年付 +1 年）

---

## 十、前端 API 对应关系

前端代码在 `ilightmate/src/api/` 下，每个文件对应的后端 Controller：

| 前端文件 | 后端 Controller | 说明 |
|---------|----------------|------|
| `auth.ts` | IlmAuthController + RuoYi 自带 | 登录注册 |
| `ai.ts` | IlmAiChatController | AI 对话 |
| `combo.ts` | IlmComboController | 套餐查询 |
| `payment.ts` | IlmOrderController + IlmPayController | 支付 |

前端的 `src/lib/request.ts` 配置了：
- Base URL: `VITE_API_BASE_URL + "/api"`（但实际路径不全在 /api 下）
- Auth: 自动注入 `Authorization: Bearer {token}`
- 响应格式: `{ code: 200, msg: "success", data: T }`
- 401 → 跳转登录页

**响应格式必须是 RuoYi 标准的 `AjaxResult`：**
```java
AjaxResult.success(data);     // { code: 0, msg: "操作成功", data: ... }
AjaxResult.error("message");  // { code: 500, msg: "message" }
```

---

## 十一、注意事项

### CORS 配置

前端和后端不同域时需要配置跨域。在 RuoYi 的 `ResourcesConfig.java` 或 `ShiroConfig.java` 中：

```java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:5173", "https://app.ilightmate.cn")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true);
```

### Redis 键命名规范

| 键模式 | 用途 | TTL |
|--------|------|-----|
| `ilm:token:{userId}:{YYYY-MM}` | 月度 token 计量 | 35 天 |
| `ilm:dialogue:{userId}:{YYYY-MM-DD}` | 每日对话计数 | 48 小时 |
| `ilm:sms:{phone}` | SMS 验证码 | 5 分钟 |

### 异步任务

`IlmTokenServiceImpl` 和 `IlmAnalyticsServiceImpl` 使用 `@Async`。确保 RuoYi 启动类或配置中启用了 `@EnableAsync`。

### MyBatis Mapper 扫描

所有 Mapper 使用了 `@Mapper` 注解。如果 RuoYi 的 `@MapperScan` 不覆盖 `com.ruoyi.ilightmate.mapper`，需要在启动类添加：

```java
@MapperScan({"com.ruoyi.**.mapper"})
```

RuoYi 默认已经是 `com.ruoyi.**.mapper`，所以应该不需要改。

---

## 十二、后续开发优先级

| 优先级 | 任务 | 说明 |
|--------|------|------|
| **P0** | 配置环境变量 + 编译启动 | 确认项目能跑起来 |
| **P0** | 测试 SMS 登录流程 | 开发模式测试（验证码打印到日志） |
| **P0** | 测试 AI 对话 SSE | 用 curl 或 Postman 测试流式响应 |
| **P1** | 微信支付 SDK 填充 | 3 个 TODO 位置 |
| **P1** | 支付宝回调测试 | 用支付宝沙箱环境 |
| **P2** | W2 用户数据 CRUD API | 11 张表的增删改查（替代前端 localStorage） |
| **P2** | 订阅过期定时任务 | RuoYi quartz 模块，每天检查 end_time |
| **P3** | 后台管理 API | 替换前端 mock 数据（按需） |

---

## 附件位置

| 文件 | 说明 |
|------|------|
| `sql/ilightmate.sql` | 19 张表 DDL + 种子数据 |
| `ruoyi-ilightmate/pom.xml` | 模块依赖（OkHttp + Alipay SDK + WechatPay SDK + Aliyun SMS） |
| `ruoyi-admin/src/main/resources/application.yml` | ilightmate.* 配置块 |
| `ilightmate/HANDOFF-STEVE.md` | 前端交接文档（API 契约 + Store 迁移指南） |

---

> **核心信息：后端代码 37 个 Java 文件已写好，你只需要配密钥 + 填微信 SDK 3 个 TODO + 编译启动。**
