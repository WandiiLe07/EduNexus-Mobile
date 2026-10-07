package com.dut.edunexusmobile.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.dut.edunexusmobile.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class ResourceRepository {

    companion object {

        // =====================================================
        // SUPABASE
        // =====================================================

        private const val SUPABASE_URL =
            "https://hvnvnunslkkfcznbubls.supabase.co"

        private const val SUPABASE_PUBLISHABLE_KEY =
            "sb_publishable_795a6aWmmMM8K0OQVZnAnA_OLIiumNo"

        private const val BUCKET_NAME = "resources"

        private const val RESOURCE_TABLE = "resources"
    }


    // =========================================================
    // LOAD RESOURCES FROM SUPABASE POSTGRESQL
    // =========================================================

    fun observeResourcesByModule(
        moduleCode: String
    ): Flow<List<Resource>> = flow {

        while (true) {

            try {

                emit(
                    getResourcesFromSupabase(moduleCode)
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }

            delay(5_000)
        }
    }


    // =========================================================
    // GET RESOURCES
    // =========================================================

    private suspend fun getResourcesFromSupabase(
        moduleCode: String
    ): List<Resource> = withContext(Dispatchers.IO) {

        val encodedModule =
            URLEncoder.encode(moduleCode, "UTF-8")

        /*
         * Supabase PostgREST query:
         *
         * SELECT *
         * FROM resources
         * WHERE module_code = moduleCode
         * ORDER BY uploaded_at DESC
         */

        val url =
            "$SUPABASE_URL/rest/v1/$RESOURCE_TABLE" +
                    "?module_code=eq.$encodedModule" +
                    "&select=*" +
                    "&order=uploaded_at.desc"

        val connection =
            URL(url).openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "GET"

            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            connection.setRequestProperty(
                "apikey",
                SUPABASE_PUBLISHABLE_KEY
            )

            connection.setRequestProperty(
                "Authorization",
                "Bearer $SUPABASE_PUBLISHABLE_KEY"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            val responseCode =
                connection.responseCode

            val response = if (
                responseCode in 200..299
            ) {

                connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

            } else {

                connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: ""
            }

            if (responseCode !in 200..299) {

                throw Exception(
                    "Could not load resources " +
                            "($responseCode): $response"
                )
            }

            val array =
                JSONArray(response)

            val resources =
                mutableListOf<Resource>()

            for (i in 0 until array.length()) {

                val item =
                    array.getJSONObject(i)

                resources.add(
                    Resource(
                        id =
                            item.optString("id"),

                        title =
                            item.optString("title"),

                        moduleCode =
                            item.optString("module_code"),

                        fileName =
                            item.optString("file_name"),

                        fileUrl =
                            item.optString("file_url"),

                        fileSizeBytes =
                            item.optLong("file_size_bytes"),

                        uploadedByUid =
                            item.optString(
                                "uploaded_by_uid"
                            ),

                        uploadedByName =
                            item.optString(
                                "uploaded_by_name"
                            )
                    )
                )
            }

            resources

        } finally {

            connection.disconnect()
        }
    }


    // =========================================================
    // UPLOAD RESOURCE
    // =========================================================

    suspend fun uploadResource(
        contentResolver: ContentResolver,
        fileUri: Uri,
        fileName: String,
        fileSizeBytes: Long,
        moduleCode: String,
        title: String,
        uploaderUid: String,
        uploaderName: String
    ): Result<Unit> {

        return try {

            // =================================================
            // STEP 1
            // Create safe filename
            // =================================================

            val safeFileName =
                fileName
                    .replace("\\", "_")
                    .replace("/", "_")
                    .replace(" ", "_")


            // =================================================
            // STEP 2
            // Create unique Supabase Storage path
            // =================================================

            val storagePath =
                "$moduleCode/" +
                        "${UUID.randomUUID()}_" +
                        safeFileName

            val encodedStoragePath =
                encodeStoragePath(storagePath)


            // =================================================
            // STEP 3
            // Upload actual document to Supabase Storage
            // =================================================

            uploadToSupabaseStorage(
                contentResolver = contentResolver,
                fileUri = fileUri,
                encodedPath = encodedStoragePath
            )


            // =================================================
            // STEP 4
            // Build public URL
            // =================================================

            val publicUrl =
                "$SUPABASE_URL/" +
                        "storage/v1/object/public/" +
                        "$BUCKET_NAME/" +
                        encodedStoragePath


            // =================================================
            // STEP 5
            // Save metadata directly into Supabase PostgreSQL
            // =================================================

            saveResourceToSupabaseDatabase(
                title = title,
                moduleCode = moduleCode,
                fileName = fileName,
                fileUrl = publicUrl,
                fileSizeBytes = fileSizeBytes,
                uploaderUid = uploaderUid,
                uploaderName = uploaderName
            )


            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // =========================================================
    // UPLOAD FILE TO SUPABASE STORAGE
    // =========================================================

    private suspend fun uploadToSupabaseStorage(
        contentResolver: ContentResolver,
        fileUri: Uri,
        encodedPath: String
    ) = withContext(Dispatchers.IO) {

        val uploadUrl =
            "$SUPABASE_URL/storage/v1/object/" +
                    "$BUCKET_NAME/$encodedPath"

        val mimeType =
            contentResolver.getType(fileUri)
                ?: "application/octet-stream"

        val connection =
            URL(uploadUrl)
                .openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "POST"
            connection.doOutput = true

            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000


            // -------------------------------------------------
            // Supabase authentication headers
            // -------------------------------------------------

            connection.setRequestProperty(
                "Authorization",
                "Bearer $SUPABASE_PUBLISHABLE_KEY"
            )

            connection.setRequestProperty(
                "apikey",
                SUPABASE_PUBLISHABLE_KEY
            )


            connection.setRequestProperty(
                "Content-Type",
                mimeType
            )

            connection.setRequestProperty(
                "x-upsert",
                "false"
            )


            // -------------------------------------------------
            // Read selected Android file
            // -------------------------------------------------

            val inputStream =
                contentResolver.openInputStream(
                    fileUri
                )
                    ?: throw Exception(
                        "Could not open selected file."
                    )


            // -------------------------------------------------
            // Send file to Supabase
            // -------------------------------------------------

            inputStream.use { input ->

                connection.outputStream.use { output ->

                    val buffer =
                        ByteArray(8192)

                    while (true) {

                        val bytesRead =
                            input.read(buffer)

                        if (bytesRead == -1) {
                            break
                        }

                        output.write(
                            buffer,
                            0,
                            bytesRead
                        )
                    }

                    output.flush()
                }
            }


            // -------------------------------------------------
            // Check Supabase response
            // -------------------------------------------------

            val responseCode =
                connection.responseCode

            if (responseCode !in 200..299) {

                val error = try {

                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }

                } catch (_: Exception) {

                    null
                }

                throw Exception(
                    "File upload failed " +
                            "($responseCode): " +
                            (error ?: "Unknown error")
                )
            }

        } finally {

            connection.disconnect()
        }
    }


    // =========================================================
    // SAVE RESOURCE METADATA TO SUPABASE POSTGRESQL
    // =========================================================

    private suspend fun saveResourceToSupabaseDatabase(
        title: String,
        moduleCode: String,
        fileName: String,
        fileUrl: String,
        fileSizeBytes: Long,
        uploaderUid: String,
        uploaderName: String
    ) = withContext(Dispatchers.IO) {

        val url =
            "$SUPABASE_URL/rest/v1/$RESOURCE_TABLE"

        val json =
            JSONObject().apply {

                put(
                    "title",
                    title
                )

                put(
                    "module_code",
                    moduleCode
                )

                put(
                    "file_name",
                    fileName
                )

                put(
                    "file_url",
                    fileUrl
                )

                put(
                    "file_size_bytes",
                    fileSizeBytes
                )

                put(
                    "uploaded_by_uid",
                    uploaderUid
                )

                put(
                    "uploaded_by_name",
                    uploaderName
                )
            }

        val bytes =
            json.toString()
                .toByteArray(Charsets.UTF_8)


        val connection =
            URL(url)
                .openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "POST"
            connection.doOutput = true

            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000


            // -------------------------------------------------
            // Supabase headers
            // -------------------------------------------------

            connection.setRequestProperty(
                "apikey",
                SUPABASE_PUBLISHABLE_KEY
            )

            connection.setRequestProperty(
                "Authorization",
                "Bearer $SUPABASE_PUBLISHABLE_KEY"
            )

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.setRequestProperty(
                "Prefer",
                "return=representation"
            )


            // -------------------------------------------------
            // Send JSON
            // -------------------------------------------------

            connection.outputStream.use { output ->

                output.write(bytes)

                output.flush()
            }


            // -------------------------------------------------
            // Check result
            // -------------------------------------------------

            val responseCode =
                connection.responseCode

            val response =
                if (responseCode in 200..299) {

                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                } else {

                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: ""
                }


            if (responseCode !in 200..299) {

                throw Exception(
                    "Could not save resource " +
                            "($responseCode): $response"
                )
            }

        } finally {

            connection.disconnect()
        }
    }


    // =========================================================
    // DELETE RESOURCE METADATA FROM SUPABASE
    //
    // IMPORTANT:
    // This deletes ONLY the database row.
    // The actual document remains in Supabase Storage.
    // =========================================================

    suspend fun deleteResource(
        resource: Resource
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {

            // -------------------------------------------------
            // Get Firebase user
            // -------------------------------------------------

            val user =
                com.google.firebase.auth.FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?: throw Exception(
                        "Please sign in again."
                    )


            // -------------------------------------------------
            // Safety check inside Android
            // -------------------------------------------------

            if (
                resource.uploadedByUid != user.uid
            ) {

                throw Exception(
                    "You can only delete " +
                            "resources that you uploaded."
                )
            }


            // -------------------------------------------------
            // Build Supabase DELETE request
            // -------------------------------------------------

            val encodedId =
                URLEncoder.encode(
                    resource.id,
                    "UTF-8"
                )

            val url =
                "$SUPABASE_URL/rest/v1/" +
                        "$RESOURCE_TABLE?id=eq.$encodedId"


            val connection =
                URL(url)
                    .openConnection() as HttpURLConnection

            try {

                connection.requestMethod =
                    "DELETE"

                connection.connectTimeout =
                    15_000

                connection.readTimeout =
                    15_000


                // ---------------------------------------------
                // Supabase headers
                // ---------------------------------------------

                connection.setRequestProperty(
                    "apikey",
                    SUPABASE_PUBLISHABLE_KEY
                )

                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $SUPABASE_PUBLISHABLE_KEY"
                )

                connection.setRequestProperty(
                    "Prefer",
                    "return=representation"
                )


                // ---------------------------------------------
                // Execute DELETE
                // ---------------------------------------------

                val responseCode =
                    connection.responseCode

                val response =
                    if (
                        responseCode in 200..299
                    ) {

                        connection.inputStream
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: ""

                    } else {

                        connection.errorStream
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: ""
                    }


                if (
                    responseCode !in 200..299
                ) {

                    throw Exception(
                        "Delete failed " +
                                "($responseCode): " +
                                response
                    )
                }


                Result.success(Unit)

            } finally {

                connection.disconnect()
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // =========================================================
    // ENCODE SUPABASE STORAGE PATH
    // =========================================================

    private fun encodeStoragePath(
        storagePath: String
    ): String {

        return storagePath
            .split("/")
            .joinToString("/") { part ->

                URLEncoder
                    .encode(
                        part,
                        "UTF-8"
                    )
                    .replace(
                        "+",
                        "%20"
                    )
            }
    }
}