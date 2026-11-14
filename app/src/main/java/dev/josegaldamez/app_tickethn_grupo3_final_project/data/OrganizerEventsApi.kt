package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.JsonParseException
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal const val ORGANIZER_PAGE_SIZE = 20
private const val MAX_COVER_BYTES = 5 * 1024 * 1024

internal data class OrganizerEvent(
    val id: String,
    val title: String,
    val description: String?,
    val address: String?,
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("cover_image_url") val coverUrl: String?,
    @SerializedName("starts_at") val startsAt: String?,
    @SerializedName("ticket_types") val localities: List<OrganizerLocality>?
)

internal class OrganizerLocality(
    val id: String,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    val capacity: Int,
    @SerializedName("sold_count") val soldCount: Int,
    @SerializedName("is_active") val active: Boolean
)

internal class LocalityInput(
    val title: String,
    val description: String,
    val price: BigDecimal,
    val capacity: Int,
    @SerializedName("is_active") val active: Boolean
)

internal data class EventDraft(
    val title: String,
    val description: String,
    val address: String,
    val startsAt: String?,
    val latitude: Double?,
    val longitude: Double?
)

internal interface OrganizerEvents {
    suspend fun list(offset: Int): List<OrganizerEvent>
    suspend fun create(draft: EventDraft, cover: ByteArray?): OrganizerEvent
    suspend fun detail(id: String): OrganizerEvent
    suspend fun update(id: String, draft: EventDraft): OrganizerEvent
    suspend fun replaceCover(id: String, cover: ByteArray): OrganizerEvent
    suspend fun saveLocality(id: String, localityId: String?, input: LocalityInput): OrganizerLocality
    suspend fun publish(id: String)
}

internal class CoverTooLargeException : IOException()

internal suspend fun readEventCover(resolver: ContentResolver, uri: Uri): ByteArray =
    withContext(Dispatchers.IO) {
        val stream = resolver.openInputStream(uri) ?: throw IOException("Cover cannot be opened")
        stream.use {
            val buffer = ByteArray(8192)
            val output = ByteArrayOutputStream()
            while (true) {
                val count = it.read(buffer)
                if (count == -1) break
                if (output.size() + count > MAX_COVER_BYTES) throw CoverTooLargeException()
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }

internal object OrganizerEventsApi {
    private val service = Retrofit.Builder()
        .baseUrl(TICKET_HN_API_URL)
        .client(OkHttpClient.Builder().retryOnConnectionFailure(false).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OrganizerEventsService::class.java)

    suspend fun list(accessToken: String, offset: Int): List<OrganizerEvent> =
        read { service.list("Bearer $accessToken", ORGANIZER_PAGE_SIZE, offset) }

    suspend fun create(accessToken: String, draft: EventDraft, cover: ByteArray?): OrganizerEvent {
        val form = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("title", draft.title)
        if (draft.description.isNotBlank()) form.addFormDataPart("description", draft.description)
        if (draft.address.isNotBlank()) form.addFormDataPart("address", draft.address)
        draft.startsAt?.let { form.addFormDataPart("starts_at", it) }
        draft.latitude?.let { form.addFormDataPart("latitude", it.toString()) }
        draft.longitude?.let { form.addFormDataPart("longitude", it.toString()) }
        if (cover != null) form.addCover(cover)
        return read { service.create("Bearer $accessToken", form.build()) }
    }

    suspend fun detail(token: String, id: String): OrganizerEvent =
        read { service.detail("Bearer $token", id) }

    suspend fun update(token: String, id: String, draft: EventDraft): OrganizerEvent {
        val body = JsonObject().apply {
            addProperty("title", draft.title)
            addProperty("description", draft.description.ifBlank { null })
            addProperty("address", draft.address.ifBlank { null })
            addProperty("starts_at", draft.startsAt)
            addProperty("latitude", draft.latitude)
            addProperty("longitude", draft.longitude)
        }
        // Send explicit nulls so an optional field can be cleared.
        val json = RequestBody.create(MediaType.parse("application/json"), body.toString())
        return read { service.update("Bearer $token", id, json) }
    }

    suspend fun replaceCover(token: String, id: String, cover: ByteArray): OrganizerEvent {
        val form = MultipartBody.Builder().setType(MultipartBody.FORM).addCover(cover).build()
        return read { service.replaceCover("Bearer $token", id, form) }
    }

    suspend fun saveLocality(token: String, id: String, localityId: String?, input: LocalityInput): OrganizerLocality =
        read {
            if (localityId == null) service.createLocality("Bearer $token", id, input)
            else service.updateLocality("Bearer $token", id, localityId, input)
        }

    suspend fun publish(token: String, id: String) {
        read { service.publish("Bearer $token", id) }
    }

    private fun MultipartBody.Builder.addCover(cover: ByteArray): MultipartBody.Builder =
        addFormDataPart("cover", "cover", RequestBody.create(MediaType.parse("application/octet-stream"), cover))

    private suspend fun <T> read(request: suspend () -> Response<T>): T {
        val response = try {
            request()
        } catch (error: JsonParseException) {
            throw IOException("Invalid organizer response", error)
        }
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw AuthApiException(response.code(), null)
        }
        return response.body() ?: throw IOException("Missing organizer response")
    }
}

private interface OrganizerEventsService {
    @GET("organizer/events")
    suspend fun list(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<List<OrganizerEvent>>

    @POST("organizer/events")
    suspend fun create(
        @Header("Authorization") authorization: String,
        @Body form: RequestBody
    ): Response<OrganizerEvent>

    @GET("organizer/events/{id}")
    suspend fun detail(@Header("Authorization") auth: String, @Path("id") id: String): Response<OrganizerEvent>

    @PATCH("organizer/events/{id}")
    suspend fun update(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: RequestBody): Response<OrganizerEvent>

    @PUT("organizer/events/{id}/cover")
    suspend fun replaceCover(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: RequestBody): Response<OrganizerEvent>

    @POST("organizer/events/{id}/ticket-types")
    suspend fun createLocality(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: LocalityInput): Response<OrganizerLocality>

    @PATCH("organizer/events/{id}/ticket-types/{localityId}")
    suspend fun updateLocality(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Path("localityId") localityId: String,
        @Body body: LocalityInput
    ): Response<OrganizerLocality>

    @POST("organizer/events/{id}/publish")
    suspend fun publish(@Header("Authorization") auth: String, @Path("id") id: String): Response<OrganizerEvent>
}

