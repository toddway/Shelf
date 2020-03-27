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
import java.io.File
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class JvmTests {
    val key = "aKey"
    var value = Obj(1)
    val clock = ManualClock()
    val shelf = Shelf(
        FileStorage(File("/tmp")),
        KotlinxSerializer().apply { register(ThingSerializer) },
        clock
    )
    val invalidKey = "Here.Is-An\uD83D\uDC4D\uD83C\uDFFDInvalid.:%$&$^:\"File_Name!!!"

    @BeforeTest
    fun before() {
        shelf.clear()
    }

    @Test
    fun `test_with_ktor`() {
        shelf.serializer = GsonSerializer()

        runBlocking {
            shelf.clear()
            val start = Date().time
            println(KtorThingService(shelf).getThingList())
            println(KtorThingService(shelf).getThingList())
            println(Date().time-start)
        }
    }

    @Test
    fun `when_using_MoshiSerializer_then_returned_values_match_the_stored_values`() {
        shelf.serializer = MoshiSerializer()

        with(shelf.item(key)) {
            put(value)

            assertTrue(has(value))
            assertTrue(get<Obj>()?.let { has(it) } ?: false)
        }
    }

    @Test
    fun `moshi_lists`() {
        shelf.serializer = MoshiSerializer()

        with(shelf.item(key)) {
            val list = listOf(Obj(1), Obj(2))
            put(list)

            assertTrue(has(list))
            assertTrue(getList<Obj>()?.let { has(it) } ?: false)

            val list2 = listOf("ASDfadf")
            put(list2)

            assertTrue(has(list2))
            assertTrue(getList<String>()?.let { has(it) } ?: false)
        }

        shelf.item("test").put(listOf(Thing("asdfd")))
        println(shelf.item("test").get<List<Thing>>())
    }

    @Test fun gson() {
        shelf.serializer = GsonSerializer()

        with(shelf.item(key)) {
            put(value)

            assertTrue(has(value))
            assertTrue(get<Obj>()?.let { has(it) } ?: false)
        }
    }

    @Test
    fun `gson_lists`() {
        shelf.serializer = GsonSerializer()

        with(shelf.item(key)) {
            val list = listOf(Obj(1), Obj(2))
            put(list)

            assertTrue(has(list))
            println(getList<Obj>())
            assertTrue(getList<Obj>()?.let { has(it) } ?: false)

            val list2 = listOf("ASDfadf")
            put(list2)

            assertTrue(has(list2))
            assertTrue(getList<String>()?.let { has(it) } ?: false)
        }

        shelf.item("test").put(listOf(Thing("asdfd")))
        println(shelf.item("test").get<List<Thing>>())
    }

    @Test
    fun `when_item_is_called_and_the_key_has_invalid_characters_then_each_invalid_character_is_replaced_with_an_underscore`() {
        val result = File("/tmp").item(invalidKey)
        assertEquals("Here.Is-An__Invalid.________File_Name___.shelf", result.name)
    }

    @Test fun `when_item_is_put_and_the_key_has_invalid_characters_then_the_same_invalid_key_can_be_used_to_get_the_value`() {
        shelf.serializer = MoshiSerializer()
        shelf.item(invalidKey).put(value)
        assertEquals(value, shelf.item(invalidKey).get()!!)
    }
}

class KtorThingService(val shelf : Shelf) {
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
        shelf.item(request.url.buildString())
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

fun newListOfThings() : List<Thing> = listOf(Thing("..."))

fun getListOfThings(shelf: Shelf) =
    shelf.item("things")
        .apply { if (olderThan(60)) put(newListOfThings()) }
        .getList<Thing>()
