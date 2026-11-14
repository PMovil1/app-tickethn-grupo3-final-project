package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

internal object EventCoverCache {
    private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    private val memory = object : LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }
    private val downloadLocks = Array(16) { Mutex() }
    private var client: OkHttpClient? = null

    fun get(url: String): Bitmap? = memory.get(url)

    suspend fun load(context: Context, rawUrl: String): Bitmap? {
        val url = HttpUrl.parse(rawUrl)?.takeIf { it.isHttps } ?: return null
        get(rawUrl)?.let { return it }
        val lock = downloadLocks[(rawUrl.hashCode() and Int.MAX_VALUE) % downloadLocks.size]
        return lock.withLock {
            get(rawUrl) ?: withContext(Dispatchers.IO) {
                try {
                    download(client(context), url)?.also { memory.put(rawUrl, it) }
                } catch (_: IOException) {
                    null
                } catch (_: SecurityException) {
                    null
                }
            }
        }
    }

    @Synchronized
    private fun client(context: Context): OkHttpClient {
        return client ?: OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "event-covers"), 40L * 1024 * 1024))
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .followSslRedirects(false)
            .build()
            .also { client = it }
    }

    private fun download(client: OkHttpClient, url: HttpUrl): Bitmap? {
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body() ?: return null
            if (body.contentLength() > MAX_IMAGE_BYTES) return null
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            val stream = body.byteStream()
            while (true) {
                val count = stream.read(buffer)
                if (count == -1) break
                if (bytes.size() + count > MAX_IMAGE_BYTES) return null
                bytes.write(buffer, 0, count)
            }

            val image = bytes.toByteArray()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(image, 0, image.size, bounds)
            if (bounds.outWidth !in 1..4096 || bounds.outHeight !in 1..4096) return null
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1024) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeByteArray(image, 0, image.size, options)
        }
    }
}

