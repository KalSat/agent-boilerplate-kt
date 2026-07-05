# agent-boilerplate-kt

一个基于 JetBrains [Koog Agents](https://github.com/JetBrains/koog) 的 Kotlin AI Agent
样板项目，演示了几种常见能力：

- **聊天机器人**：支持流式输出和上下文窗口维护
- **结构化输出**：使用 JSON Schema 约束模型返回类型安全结果
- **计算器 Agent**：通过工具调用执行数学计算
- **中国象棋 Agent**：通过工具调用与人机对弈

## 环境要求

- JDK 17
- Kotlin / Gradle
- 可用的 OpenAI 兼容模型接口

## 配置

先复制环境变量文件：

```bash
cp .env.example .env
```

然后修改 `.env`：

```env
BASE_URL=your-llm-api-url-here
API_KEY=your-llm-api-key-here
MODEL=THUDM/GLM-Z1-9B-0414
SMALL_FAST_MODEL=Qwen/Qwen3-8B
```

默认会使用 `https://api.siliconflow.cn/v1/` 作为接口地址。

## 运行

启动交互菜单：

```bash
./gradlew run
```

启动后可选择功能：

1. 聊天机器人
2. 电影推荐（结构化输出）
3. 计算器
4. 中国象棋

## 构建与测试

```bash
./gradlew build
./gradlew test
```

## 项目结构

```text
src/main/kotlin/
  Main.kt                  # 交互式入口
  shared/                  # 通用配置、HTTP 客户端、LLM 初始化
  features/chatbot/        # 流式聊天机器人
  features/structuredoutput/ # 结构化输出示例
  features/calculator/     # 工具调用示例
  features/chess/          # 中国象棋对弈示例
```

## 说明

- 项目使用 OpenAI 兼容接口，方便切换不同模型提供商
- 聊天机器人默认保留最近 10 条消息作为上下文
- 结构化输出示例通过 `executeStructured<T>()` 返回类型安全数据
- 中国象棋与计算器示例展示了 Koog 的工具调用能力
