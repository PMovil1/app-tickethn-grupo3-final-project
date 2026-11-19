package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.IOException
import java.io.Serializable
import java.math.BigDecimal
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal class PublicEvent(
    val id: String,
    val title: String,
    val description: String?,
    val address: String?,
    @SerializedName("starts_at") val startsAt: String?,
    @SerializedName("cover_image_url") val coverUrl: String?,
    @SerializedName("ticket_types") val ticketTypes: List<PublicTicketType>?,
    val latitude: Double?,
    val longitude: Double?
)

internal class PublicTicketType(
    val id: String,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    @SerializedName("is_active") val active: Boolean,
    val available: Int
)

internal class OrderItem(
    @SerializedName("ticket_type_id") val ticketTypeId: String,
    val quantity: Int
)

internal class TicketOrder(val total: BigDecimal, val tickets: List<OrderTicket>) : Serializable

internal class OrderTicket(val code: String) : Serializable

internal data class BuyerTicket(
    val id: String,
    @SerializedName("event_id") val eventId: String,
    @SerializedName("ticket_type_id") val ticketTypeId: String,
    val code: String,
    @SerializedName("unit_price") val unitPrice: BigDecimal,
    @SerializedName("used_at") val usedAt: String?,
    @SerializedName("event_title") val eventTitle: String?,
    @SerializedName("event_address") val eventAddress: String?,
    @SerializedName("event_starts_at") val eventStartsAt: String?,
    @SerializedName("event_cover_image_url") val eventCoverUrl: String?,
    @SerializedName("event_status") val eventStatus: String?,
    @SerializedName("ticket_type_title") val ticketTypeTitle: String?,
    @SerializedName("order_status") val orderStatus: String?,
    @SerializedName("qr_payload") val qrPayload: String?
) : Serializable

private class OrderRequest(
    @SerializedName("event_id") val eventId: String,
    val items: List<OrderItem>
)

internal class ScannedTicket(
    @SerializedName("buyer_name") val buyerName: String,
    @SerializedName("purchased_at") val purchasedAt: String,
    @SerializedName("event_title") val eventTitle: String,
    @SerializedName("ticket_type_title") val ticketTypeTitle: String,
    @SerializedName("code") val code: String,
    @SerializedName("used_at") val usedAt: String?,
    @SerializedName("can_check_in") val canCheckIn: Boolean
)

private class TicketQRRequest(@SerializedName("qr_payload") val qrPayload: String)

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

    suspend fun detail(id: String): PublicEvent = read { service.detail(id) }

    suspend fun buy(token: String, eventId: String, items: List<OrderItem>): TicketOrder =
        read { service.buy("Bearer $token", OrderRequest(eventId, items)) }

    suspend fun tickets(token: String): List<BuyerTicket> {
        val tickets = linkedMapOf<String, BuyerTicket>()
        var offset = 0
        do {
            val page = read { service.tickets("Bearer $token", pageSize, offset) }
            page.forEach { tickets[it.id] = it }
            offset += page.size
        } while (page.size == pageSize)
        return tickets.values.toList()
    }

    suspend fun ticket(token: String, id: String): BuyerTicket =
        read { service.ticket("Bearer $token", id) }

    suspend fun lookupTicket(token: String, qrPayload: String): ScannedTicket {
        val response = try {
            service.lookupTicket("Bearer $token", TicketQRRequest(qrPayload))
        } catch (error: JsonParseException) {
            throw IOException("Invalid ticket lookup response", error)
        }
        response.requireSuccess()
        return response.body() ?: throw IOException("Missing ticket lookup response")
    }

    suspend fun checkIn(token: String, qrPayload: String) {
        service.checkIn("Bearer $token", TicketQRRequest(qrPayload)).requireSuccess()
    }

    private suspend fun <T> read(request: suspend () -> Response<T>): T {
        val response = try {
            request()
        } catch (error: JsonParseException) {
            throw IOException("Invalid event or order response", error)
        }
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw AuthApiException(response.code(), null)
        }
        return response.body() ?: throw IOException("Missing event or order response")
    }
}

private interface PublicEventsService {
    @GET("events")
    suspend fun list(@Query("limit") limit: Int, @Query("offset") offset: Int): Response<List<PublicEvent>>

    @GET("events/{id}")
    suspend fun detail(@Path("id") id: String): Response<PublicEvent>

    @POST("orders")
    suspend fun buy(@Header("Authorization") authorization: String, @Body body: OrderRequest): Response<TicketOrder>

    @GET("tickets")
    suspend fun tickets(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<List<BuyerTicket>>

    @GET("tickets/{id}")
    suspend fun ticket(@Header("Authorization") authorization: String, @Path("id") id: String): Response<BuyerTicket>

    @POST("tickets/lookup")
    suspend fun lookupTicket(
        @Header("Authorization") authorization: String,
        @Body body: TicketQRRequest
    ): Response<ScannedTicket>

    @POST("tickets/check-in")
    suspend fun checkIn(@Header("Authorization") authorization: String, @Body body: TicketQRRequest): Response<Unit>
}

