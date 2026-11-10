package com.aaqibhussain.admin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AdminApiClient(
    var baseUrl: String = "https://aaqibhussain.com"
) {
    var sessionToken: String? = null

    private fun cleanBase(): String = baseUrl.trim().trimEnd('/')

    private fun executeRequest(
        method: String,
        endpoint: String,
        bodyJson: JSONObject? = null
    ): Pair<Int, JSONObject> {
        val url = URL("${cleanBase()}$endpoint")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("Accept", "application/json")
            if (bodyJson != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            sessionToken?.let { token ->
                setRequestProperty("Authorization", "Bearer $token")
            }
        }

        if (bodyJson != null) {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(bodyJson.toString())
                writer.flush()
            }
        }

        val status = conn.responseCode
        val stream = if (status in 200..299) conn.inputStream else conn.errorStream
        val responseText = if (stream != null) {
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        } else {
            "{}"
        }
        val parsed = try {
            JSONObject(responseText)
        } catch (_: Exception) {
            JSONObject().put("error", responseText)
        }
        return Pair(status, parsed)
    }

    suspend fun login(username: String, password: String): Result<Pair<String, SiteData>> =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }
                val (code, json) = executeRequest("POST", "/api/admin/login", payload)
                if (code in 200..299 && json.optBoolean("ok", false)) {
                    val token = json.optString("token", "")
                    sessionToken = token
                    val siteObj = json.optJSONObject("siteData") ?: JSONObject()
                    Result.success(Pair(json.optString("username", username), SiteDataSerializer.parse(siteObj)))
                } else {
                    Result.failure(Exception(json.optString("error", "Authentication failed ($code)")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchSiteData(): Result<SiteData> = withContext(Dispatchers.IO) {
        try {
            val (code, json) = executeRequest("GET", "/api/site-data")
            if (code in 200..299) {
                val siteObj = json.optJSONObject("siteData") ?: JSONObject()
                Result.success(SiteDataSerializer.parse(siteObj))
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load site data")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishSiteData(siteData: SiteData): Result<SiteData> =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("siteData", SiteDataSerializer.toJson(siteData))
                }
                val (code, json) = executeRequest("PUT", "/api/admin/site-data", payload)
                if (code in 200..299 && json.optBoolean("ok", false)) {
                    val updatedObj = json.optJSONObject("siteData") ?: SiteDataSerializer.toJson(siteData)
                    Result.success(SiteDataSerializer.parse(updatedObj))
                } else {
                    Result.failure(Exception(json.optString("error", "Failed to publish site data ($code)")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun uploadAttachment(
        fileName: String,
        mimeType: String,
        base64Data: String
    ): Result<Pair<AttachmentItem, SiteData>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("name", fileName)
                put("mimeType", mimeType)
                put("dataBase64", base64Data)
            }
            val (code, json) = executeRequest("POST", "/api/admin/attachments", payload)
            if (code in 200..299 && json.optBoolean("ok", false)) {
                val attObj = json.optJSONObject("attachment") ?: JSONObject()
                val att = AttachmentItem(
                    id = attObj.optString("id", ""),
                    name = attObj.optString("name", fileName),
                    mimeType = attObj.optString("mimeType", mimeType),
                    url = attObj.optString("url", ""),
                    size = attObj.optLong("size", 0L),
                    createdAt = attObj.optString("createdAt", "")
                )
                val siteObj = json.optJSONObject("siteData") ?: JSONObject()
                Result.success(Pair(att, SiteDataSerializer.parse(siteObj)))
            } else {
                Result.failure(Exception(json.optString("error", "Attachment upload failed ($code)")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAttachment(id: String): Result<SiteData> = withContext(Dispatchers.IO) {
        try {
            val (code, json) = executeRequest("DELETE", "/api/admin/attachments/$id")
            if (code in 200..299 && json.optBoolean("ok", false)) {
                val siteObj = json.optJSONObject("siteData") ?: JSONObject()
                Result.success(SiteDataSerializer.parse(siteObj))
            } else {
                Result.failure(Exception(json.optString("error", "Delete attachment failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCredentials(
        currentPassword: String,
        newUsername: String,
        newPassword: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("currentPassword", currentPassword)
                put("username", newUsername)
                put("newPassword", newPassword)
            }
            val (code, json) = executeRequest("PUT", "/api/admin/credentials", payload)
            if (code in 200..299 && json.optBoolean("ok", false)) {
                Result.success(json.optString("username", newUsername))
            } else {
                Result.failure(Exception(json.optString("error", "Failed to update credentials")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetToDefault(): Result<SiteData> = withContext(Dispatchers.IO) {
        try {
            val (code, json) = executeRequest("POST", "/api/admin/reset", JSONObject())
            if (code in 200..299 && json.optBoolean("ok", false)) {
                val siteObj = json.optJSONObject("siteData") ?: JSONObject()
                Result.success(SiteDataSerializer.parse(siteObj))
            } else {
                Result.failure(Exception(json.optString("error", "Failed to reset database")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
