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
    val serializers = listOf(
        KotlinxSerializer().apply { register(ThingSerializer) },
        MoshiSerializer(),
        GsonSerializer()
    )
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
    fun `when_using_any_serializer_then_any_returned_values_match_the_stored_values`() {
        serializers.forEach { serializer ->
            shelf.clear()
            shelf.serializer = serializer.also { println("\n" + serializer::class.simpleName) }

            listOf(1, true, "adsf", Thing("ADf")).forEach { value ->
                shelf.item(key).apply {
                    assertEquals(value, put(value).run { get(value::class) }.also { println("  $value") })
                    assertTrue(has(value))
                }

                listOf(
                    listOf(value, value, value),
                    listOf()
                ).forEach { list ->
                    shelf.item(key).apply {
                        assertEquals(list, put(list).run { getList(value::class) }.also { println("  $list") })
                        assertTrue(has(list))
                    }
                }
            }



            Unit
        }
    }


    @Test
    fun `test_with_ktor`() {
        serializers.forEach {
            runBlocking {
                shelf.clear()
                shelf.serializer = it
                val start = Date().time
                println(KtorThingService(shelf).getThingList())
                println(KtorThingService(shelf).getThingList())
                println(Date().time-start)
            }
        }
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
            .apply { if (olderThan(60)) { putRaw(newThingList2()) } }
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
