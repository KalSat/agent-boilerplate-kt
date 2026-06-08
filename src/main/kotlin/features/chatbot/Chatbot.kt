package ai.inspire.features.chatbot

import ai.inspire.shared.promptExecutor
import ai.inspire.shared.smallFastLlmModel
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessageBuilder
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.runBlocking

fun testChatbot() =
    runBlocking {
        println("聊天机器人启动...")

        val messages = mutableListOf<Message>(
            MessageBuilder.system().addText(PROMPTS["concise"]!!).build(),
        )

        println("输入 'exit' 退出对话。")
        while (true) {
            print("用户: ")
            val userInput = readlnOrNull() ?: break
            if (userInput.trim().lowercase() == "exit") {
                println("聊天机器人已退出。")
                break
            }

            messages.add(MessageBuilder.user().addText(userInput).build())

            print("机器人: \n")
            var fullReply = ""

            promptExecutor
                .executeStreaming(
                    prompt("chatbot", LLMParams(temperature = 0.3)) { messages(messages) },
                    smallFastLlmModel,
                ).collect { frame ->
                    if (frame is StreamFrame.TextDelta) {
                        print(frame.text)
                        System.out.flush()
                        fullReply += frame.text
                    }
                }

            println("\n" + "-".repeat(40))

            messages.add(MessageBuilder.assistant().addText(fullReply).build())

            val tail = messages.takeLast(10)
            messages.clear()
            messages.addAll(tail)
        }
    }
