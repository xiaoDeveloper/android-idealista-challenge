package com.xiao.idealistachallenge.data.remote

import kotlinx.serialization.json.Json

/** Shared JSON policy for the externally controlled Idealista payloads. */
object RemoteJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = false
    }
}
