package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.IOException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal class AuthSession(
    val email: String,
    val fullName: String?,
    val accessToken: String,
    val refreshToken: String
)

internal class AuthApiException(val status: Int, val code: String?) : Exception()

internal object AuthApi {
    private val gson = Gson()
    private val service = Retrofit.Builder()
        .baseUrl("https://api-tickethn-grupo3-final-project-337965348211.us-central1.run.app/api/auth/")
        .client(OkHttpClient.Builder().retryOnConnectionFailure(false).build())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(AuthService::class.java)

    suspend fun register(name: String, email: String, password: String) {
        service.register(RegisterRequest(name, email, password)).requireSuccess()
    }

    suspend fun verifyEmail(email: String, code: String) {
        service.verifyEmail(VerifyEmailRequest(email, code)).requireSuccess()
    }

    suspend fun resendVerification(email: String) {
        service.resendVerification(EmailRequest(email)).requireSuccess()
    }

    suspend fun login(email: String, password: String): AuthSession =
        readSession { service.login(LoginRequest(email, password)) }

    suspend fun refresh(refreshToken: String): AuthSession =
        readSession { service.refresh(RefreshRequest(refreshToken)) }

    suspend fun logout(session: AuthSession) {
        service.logout("Bearer ${session.accessToken}", RefreshRequest(session.refreshToken)).requireSuccess()
    }

    private suspend fun readSession(call: suspend () -> Response<SessionResponse>): AuthSession {
        val response = try {
            call()
        } catch (error: JsonParseException) {
            throw IOException("Invalid authentication response", error)
        }
        response.requireSuccess()
        val body = response.body()
        val email = body?.user?.email
        val fullName = body?.user?.fullName?.trim()?.takeIf { it.isNotEmpty() }
        val accessToken = body?.accessToken
        val refreshToken = body?.refreshToken
        if (email.isNullOrBlank() || accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            throw IOException("Incomplete authentication response")
        }
        return AuthSession(email, fullName, accessToken, refreshToken)
    }

    private fun Response<*>.requireSuccess() {
        if (isSuccessful) return
        val errorCode = try {
            errorBody()?.charStream()?.use { gson.fromJson(it, ErrorResponse::class.java)?.error?.code }
        } catch (_: JsonParseException) {
            null
        }
        throw AuthApiException(code(), errorCode)
    }
}

private interface AuthService {
    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<SessionResponse>

    @POST("verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<Unit>

    @POST("resend-verification")
    suspend fun resendVerification(@Body body: EmailRequest): Response<Unit>

    @POST("refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<SessionResponse>

    @POST("logout")
    suspend fun logout(@Header("Authorization") authorization: String, @Body body: RefreshRequest): Response<Unit>
}

private class RegisterRequest(@SerializedName("full_name") val name: String, val email: String, val password: String)
private class LoginRequest(val email: String, val password: String)
private class VerifyEmailRequest(val email: String, val code: String)
private class EmailRequest(val email: String)
private class RefreshRequest(@SerializedName("refresh_token") val refreshToken: String)
private class SessionResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    val user: SessionUser?
)
private class SessionUser(
    val email: String?,
    @SerializedName("full_name") val fullName: String?
)
private class ErrorResponse(val error: ErrorDetail?)
private class ErrorDetail(val code: String?)
