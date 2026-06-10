@file:Suppress("ktlint:standard:filename")

package ai.inspire.features.structuredoutput

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Movie")
@LLMDescription("A movie with details")
data class Movie(
    @property:LLMDescription("The title of the movie")
    val title: String,
    @property:LLMDescription("The year the movie was released")
    val year: Int,
    @property:LLMDescription("The director of the movie")
    val director: String,
    @property:LLMDescription("The movie's rating out of 10")
    val rating: Float,
)
