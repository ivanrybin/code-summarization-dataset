package reposanalyzer.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reposanalyzer.utils.BadMethodUniquenessConfigParameter

enum class IdentityParameters(val label: String) {
    FILE("file"),
    NAME("name"),
    FULL_NAME("full_name"),
    ARGS_TYPES("args_types"),
    RETURN_TYPE("return_type")
}

class IdentityConfig(val parameters: List<IdentityParameters>) {
    companion object {
        fun createFromJson(jsonNode: JsonNode): IdentityConfig {
            val params = jsonNode.map { it.asText() }
            params.forEach { param ->
                if (!IdentityParameters.values().map { it.label }.contains(param)) {
                    throw BadMethodUniquenessConfigParameter("unknown parameter $param in config list")
                }
            }
            return IdentityConfig(
                IdentityParameters.values().filter { params.contains(it.label) }.toList()
            )
        }
    }

    val isNoIdentity: Boolean
        get() = parameters.isEmpty()
}
