package ai.inspire.shared

import io.github.cdimascio.dotenv.dotenv

object Config {
    private val dotenv = dotenv()

    val baseUrl: String = dotenv["BASE_URL"] ?: "https://api.siliconflow.cn/"
    val apiKey: String = dotenv["API_KEY"] ?: ""
    val model: String = dotenv["MODEL"] ?: ""
    val smallFastModel: String = dotenv["SMALL_FAST_MODEL"] ?: ""
}
