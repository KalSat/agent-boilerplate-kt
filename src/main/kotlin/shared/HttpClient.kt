@file:Suppress("UnstableApiUsage")

package ai.inspire.shared

import ai.koog.http.client.ktor.KtorKoogHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.nio.charset.StandardCharsets

private val koogClientFactory = KtorKoogHttpClient.Factory(
    baseClient = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    },
)

fun createHttpClient(
    baseUrl: String,
    apiKey: String,
): KtorKoogHttpClient =
    koogClientFactory
        .create(
            clientName = "KtorHttpClient",
            baseUrl = baseUrl,
            headers = mapOf("Authorization" to "Bearer $apiKey"),
        ).also {
            // 拦截 Ktor 的渲染阶段（此时对象已被 ContentNegotiation 转为 JSON 文本）
            it.ktorClient.requestPipeline.intercept(HttpRequestPipeline.Render) { payload ->
                // 仅拦截声明了 application/json 且内容为 TextContent 的请求
                if (payload !is OutgoingContent ||
                    payload.contentType?.match(ContentType.Application.Json) != true
                ) {
                    proceedWith(payload)
                    return@intercept
                }

                try {
                    val originalJsonText = when (payload) {
                        is TextContent -> {
                            payload.text
                        }

                        is OutgoingContent.ByteArrayContent -> {
                            String(payload.bytes(), StandardCharsets.UTF_8)
                        }

                        else -> {
                            proceedWith(payload)
                            return@intercept
                        }
                    }

                    val jsonElement =
                        Json.parseToJsonElement(originalJsonText).jsonObject.toMutableMap()

                    // extra_body 注入
                    jsonElement["extra_body"] = buildJsonObject {
                        put("enable_thinking", false)
                        put("thinking_budget", 0)
                    }

                    // 检查并移除空的 tools 数组，防止触发网关严格校验
                    val toolsElement = jsonElement["tools"]
                    if (toolsElement is JsonArray && toolsElement.isEmpty()) {
                        jsonElement.remove("tools")
                    }

                    val modifiedJsonText =
                        Json.encodeToString(JsonObject.serializer(), JsonObject(jsonElement))
                    val modifiedPayload = TextContent(
                        text = modifiedJsonText,
                        contentType = payload.contentType ?: ContentType.Application.Json,
                        status = payload.status,
                    )

                    proceedWith(modifiedPayload)
                    return@intercept
                } catch (_: Exception) {
                    proceedWith(payload)
                }
            }
        }
