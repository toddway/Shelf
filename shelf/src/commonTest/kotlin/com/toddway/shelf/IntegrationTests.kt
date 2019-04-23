package com.toddway.shelf

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.*
import io.ktor.client.response.readText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlin.test.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.list

class IntegrationTests {

    fun test() = runBlocked {
        Shelf.serializer = KotlinxJsonSerializer().apply {
            register(ThingSerializer)
        }

        println(StoredThingService().getThingList())
    }
}

class StoredThingService {
    val policies = with(Shelf.item("thingList")) {
        StorePolicies<List<Thing>>(
            { getList() },
            { put(it) },
            { KtorThingService().getThingList() },
            { age().isLessThan(60) }
        )
    }

    suspend fun getThingList() : List<Thing>? = policies.storedOrNew()
}

class KtorThingService {
    private val client = HttpClient {}

    private val request = HttpRequestBuilder().apply {
        url("https://api.github.com/issues?filter=all&status=open&direction=desc")
        header(HttpHeaders.Authorization, "Basic dG9kZHdheTphZjgyMWI2MDI5YzUwZDU4ZTFhMWFiMjI4YWY4Y2I5MGY1NjE3NTA1")
        header(HttpHeaders.CacheControl, "no-cache")
    }

    suspend fun getThingList() = client.request(request, ThingSerializer.list)
}

suspend fun <T : Any> HttpClient.request(builder: HttpRequestBuilder, serializer: KSerializer<T>) : T {
    val response = call(builder).response
    if (response.status.value < 300)
        return response.readText().let { Json.nonstrict.parse(serializer, it) }
    else
        throw BadResponseStatusException(response.status, response)
}

data class Thing(val url : String)

@Serializer(forClass = Thing::class)
object ThingSerializer

class StorePolicies<T : Any>(
    val getStored : () -> T?,
    val putStored : (T) -> Unit,
    val getNew : suspend () -> T?,
    val isStoredValid : () -> Boolean
) {
    suspend fun storedOrNew() : T? =
        if (!isStoredValid())
            getNew()?.also(putStored)
        else
            getStored()

    suspend fun storedOrNew(result: (T?) -> Unit) = result(storedOrNew())

    suspend fun storedThenNew(result : (T?) -> Unit) {
        result(getStored())

        if (!isStoredValid())
            result(getNew()?.also(putStored))
    }
}
