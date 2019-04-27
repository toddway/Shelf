package com.toddway.shelf

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import java.util.*
import kotlin.test.*

class JvmTests {
    val key = "aKey"
    var value = Obj(1)
    val clock = ManualClock()

    @BeforeTest
    fun before() {
        Shelf.storage = DiskStorage()
        Shelf.serializer = KotlinxSerializer().apply {
            register(ThingSerializer)
        }
        Shelf.clock = clock
        Shelf.clear()
    }


    @Test
    fun `test_with_ktor`() {
        Shelf.serializer = MoshiSerializer()

        runBlocking {
            Shelf.clear()
            val start = Date().time
            println(KtorThingService().getThingList())
            println(KtorThingService().getThingList())
            println(Date().time-start)
        }
    }

    @Test
    fun `when_using_MoshiSerializer_then_returned_values_match_the_stored_values`() {
        Shelf.serializer = MoshiSerializer()

        with(Shelf.item(key)) {
            put(value)

            assertTrue(has(value))
            assertTrue(get<Obj>()?.let { has(it) } ?: false)
        }
    }

    @Test
    fun `moshi_lists`() {
        Shelf.serializer = MoshiSerializer()

        with(Shelf.item(key)) {
            val list = listOf(Obj(1), Obj(2))
            put(list)

            assertTrue(has(list))
            assertTrue(getList<Obj>()?.let { has(it) } ?: false)

            val list2 = listOf("ASDfadf")
            put(list2)

            assertTrue(has(list2))
            assertTrue(getList<String>()?.let { has(it) } ?: false)
        }

//        Shelf.item("test").put(listOf(Thing("asdfd")))
//        println(Shelf.item("test").get<List<Thing>>())
    }
}

class KtorThingService {
    private val client = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
    }

    val request = HttpRequestBuilder().apply {
        url(BuildKonfig.url)
        header(HttpHeaders.Authorization, BuildKonfig.token)
        header(HttpHeaders.CacheControl, "no-cache")
    }

    suspend fun newThingList() = Json.nonstrict.parse(ThingSerializer.list, newThingList2())
    suspend fun newThingList2() = client.response(request).readText()
    suspend fun getThingList() =
        Shelf.item(request.url.buildString())
            .apply { if (olderThan(60)) put(newThingList2()) }
            .getList<Thing>()
}

suspend fun HttpClient.response(requestBuilder: HttpRequestBuilder): HttpResponse {
    val response = call(requestBuilder).response
    if (response.status.value >= 300)
        throw BadResponseStatusException(response.status, response)
    return response
}


data class Thing(val url : String)

@Serializer(forClass = Thing::class)
object ThingSerializer
