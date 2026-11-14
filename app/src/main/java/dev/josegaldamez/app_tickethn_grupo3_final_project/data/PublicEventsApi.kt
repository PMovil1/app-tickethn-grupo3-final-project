package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.IOException
import java.math.BigDecimal
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

internal class PublicEvent(
    val id: String,
    val title: String,
    val description: String?,
    val address: String?,
    @SerializedName("starts_at") val startsAt: String?,
    @SerializedName("cover_image_url") val coverUrl: String?,
    @SerializedName("ticket_types") val ticketTypes: List<PublicTicketType>?
)

internal class PublicTicketType(
    val id: String,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    @SerializedName("is_active") val active: Boolean,
    val available: Int
)

internal object PublicEventsApi {
    private const val pageSize = 100
    private val service = Retrofit.Builder()
        .baseUrl(TICKET_HN_API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PublicEventsService::class.java)

    suspend fun list(): List<PublicEvent> {
        val events = linkedMapOf<String, PublicEvent>()
        var offset = 0
        do {
            val response = try {
                service.list(pageSize, offset)
            } catch (error: JsonParseException) {
                throw IOException("Invalid public events response", error)
            }
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                throw IOException("Public events request failed: HTTP ${response.code()}")
            }
            val page = response.body() ?: throw IOException("Missing public events response")
            page.forEach { events[it.id] = it }
            offset += page.size
        } while (page.size == pageSize)
        return events.values.toList()
    }
}

private interface PublicEventsService {
    @GET("events")
    suspend fun list(@Query("limit") limit: Int, @Query("offset") offset: Int): Response<List<PublicEvent>>
}

