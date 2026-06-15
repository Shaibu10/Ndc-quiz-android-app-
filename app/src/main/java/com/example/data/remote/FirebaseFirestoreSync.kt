package com.example.data.remote

import android.util.Log
import com.example.data.local.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FirebaseFirestoreSync {
    private const val TAG = "FirestoreSync"
    private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/ndc-quiz-android-app/databases/(default)/documents"
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // Helpers to robustly extract types from Firestore fields
    private fun getStr(fields: JSONObject, key: String, default: String = ""): String {
        return if (fields.has(key)) {
            val valueObj = fields.getJSONObject(key)
            if (valueObj.has("stringValue")) valueObj.getString("stringValue") else default
        } else {
            default
        }
    }

    private fun getInt(fields: JSONObject, key: String, default: Int = 0): Int {
        return if (fields.has(key)) {
            val valueObj = fields.getJSONObject(key)
            if (valueObj.has("integerValue")) {
                valueObj.getString("integerValue").toIntOrNull() ?: default
            } else if (valueObj.has("doubleValue")) {
                valueObj.getDouble("doubleValue").toInt()
            } else {
                default
            }
        } else {
            default
        }
    }

    private fun getLong(fields: JSONObject, key: String, default: Long = 0L): Long {
        return if (fields.has(key)) {
            val valueObj = fields.getJSONObject(key)
            if (valueObj.has("integerValue")) {
                valueObj.getString("integerValue").toLongOrNull() ?: default
            } else if (valueObj.has("doubleValue")) {
                valueObj.getDouble("doubleValue").toLong()
            } else {
                default
            }
        } else {
            default
        }
    }

    private fun getBool(fields: JSONObject, key: String, default: Boolean = false): Boolean {
        return if (fields.has(key)) {
            val valueObj = fields.getJSONObject(key)
            if (valueObj.has("booleanValue")) valueObj.getBoolean("booleanValue") else default
        } else {
            default
        }
    }

    // Push standard entity mapping to Firestore Fields JSON
    private fun mapToFields(map: Map<String, Any?>): JSONObject {
        val fieldsJson = JSONObject()
        map.forEach { (key, value) ->
            val valJson = JSONObject()
            when (value) {
                is String -> valJson.put("stringValue", value)
                is Boolean -> valJson.put("booleanValue", value)
                is Int -> valJson.put("integerValue", value.toString())
                is Long -> valJson.put("integerValue", value.toString())
                is Double -> valJson.put("doubleValue", value)
                null -> valJson.put("nullValue", JSONObject.NULL)
                else -> valJson.put("stringValue", value.toString())
            }
            fieldsJson.put(key, valJson)
        }
        return fieldsJson
    }

    // Generic function to push a single document to Firebase Firestore
    suspend fun pushDocument(collection: String, documentId: String, fields: JSONObject): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/$collection/$documentId"
        val requestBody = JSONObject().apply {
            put("fields", fields)
        }.toString().toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(url)
            .patch(requestBody) // Use PATCH to create or update
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully pushed document $documentId to $collection")
                    true
                } else {
                    Log.e(TAG, "Failed pushing document $documentId to $collection: ${response.code} ${response.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error pushing document $collection/$documentId: ${e.message}")
            false
        }
    }

    // Generic function to pull check list of documents from Firestore
    private suspend fun fetchCollectionDocuments(collection: String): JSONArray = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/$collection?pageSize=300"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: "{}"
                    val jsonResponse = JSONObject(bodyStr)
                    if (jsonResponse.has("documents")) {
                        jsonResponse.getJSONArray("documents")
                    } else {
                        JSONArray() // No documents found
                    }
                } else {
                    Log.e(TAG, "Failed fetching collection $collection: code ${response.code}")
                    JSONArray()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching collection $collection: ${e.message}")
            JSONArray()
        }
    }

    // Sync categories from Firestore
    suspend fun syncCategories(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("categories")
        if (docsArray.length() == 0) return
        
        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val category = CategoryEntity(
                    id = getStr(fields, "id"),
                    categoryName = getStr(fields, "categoryName"),
                    categoryImage = getStr(fields, "categoryImage"),
                    description = getStr(fields, "description"),
                    active = getBool(fields, "active"),
                    createdAt = getLong(fields, "createdAt")
                )
                quizAppDao.insertCategory(category)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Category document at $i: ${e.message}")
            }
        }
    }

    // Sync quizzes from Firestore
    suspend fun syncQuizzes(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("quizzes")
        if (docsArray.length() == 0) return

        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val quiz = QuizEntity(
                    id = getStr(fields, "id"),
                    categoryId = getStr(fields, "categoryId"),
                    title = getStr(fields, "title"),
                    description = getStr(fields, "description"),
                    imageUrl = getStr(fields, "imageUrl"),
                    sponsorName = getStr(fields, "sponsorName"),
                    sponsorLogo = getStr(fields, "sponsorLogo"),
                    accessCode = getStr(fields, "accessCode"),
                    timeLimitMinutes = getInt(fields, "timeLimitMinutes"),
                    active = getBool(fields, "active"),
                    startDate = getStr(fields, "startDate"),
                    endDate = getStr(fields, "endDate"),
                    totalQuestions = getInt(fields, "totalQuestions"),
                    createdBy = getStr(fields, "createdBy"),
                    createdAt = getLong(fields, "createdAt"),
                    sponsorId = getStr(fields, "sponsorId"),
                    maxAttempts = getInt(fields, "maxAttempts")
                )
                quizAppDao.insertQuiz(quiz)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Quiz document at $i: ${e.message}")
            }
        }
    }

    // Sync questions from Firestore
    suspend fun syncQuestions(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("questions")
        if (docsArray.length() == 0) return

        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val question = QuestionEntity(
                    id = getStr(fields, "id"),
                    quizId = getStr(fields, "quizId"),
                    questionText = getStr(fields, "questionText"),
                    optionA = getStr(fields, "optionA"),
                    optionB = getStr(fields, "optionB"),
                    optionC = getStr(fields, "optionC"),
                    optionD = getStr(fields, "optionD"),
                    correctAnswer = getStr(fields, "correctAnswer"),
                    explanation = getStr(fields, "explanation"),
                    imageUrl = getStr(fields, "imageUrl")
                )
                quizAppDao.insertQuestion(question)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Question document at $i: ${e.message}")
            }
        }
    }

    // Sync sponsors from Firestore
    suspend fun syncSponsors(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("sponsors")
        if (docsArray.length() == 0) return

        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val sponsor = SponsorEntity(
                    id = getStr(fields, "id"),
                    name = getStr(fields, "name"),
                    logoUrl = getStr(fields, "logoUrl"),
                    description = getStr(fields, "description"),
                    active = getBool(fields, "active")
                )
                quizAppDao.insertSponsor(sponsor)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Sponsor document at $i: ${e.message}")
            }
        }
    }

    // Sync announcements from Firestore
    suspend fun syncAnnouncements(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("announcements")
        if (docsArray.length() == 0) return

        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val announcement = AnnouncementEntity(
                    id = getStr(fields, "id"),
                    title = getStr(fields, "title"),
                    content = getStr(fields, "content"),
                    createdAt = getLong(fields, "createdAt"),
                    scheduledAt = getLong(fields, "scheduledAt"),
                    isPushed = getBool(fields, "isPushed"),
                    active = getBool(fields, "active"),
                    imageUrl = if (fields.has("imageUrl") && !fields.getJSONObject("imageUrl").has("nullValue")) getStr(fields, "imageUrl") else null,
                    linkUrl = if (fields.has("linkUrl") && !fields.getJSONObject("linkUrl").has("nullValue")) getStr(fields, "linkUrl") else null,
                    linkLabel = if (fields.has("linkLabel") && !fields.getJSONObject("linkLabel").has("nullValue")) getStr(fields, "linkLabel") else null
                )
                quizAppDao.insertAnnouncement(announcement)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Announcement document at $i: ${e.message}")
            }
        }
    }

    // Sync leaderboard from Firestore
    suspend fun syncLeaderboard(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("leaderboard")
        if (docsArray.length() == 0) return

        val entries = ArrayList<LeaderboardEntity>()
        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val entry = LeaderboardEntity(
                    id = getStr(fields, "id"),
                    userId = getStr(fields, "userId"),
                    userFullName = getStr(fields, "userFullName"),
                    quizId = getStr(fields, "quizId"),
                    categoryId = getStr(fields, "categoryId"),
                    score = getInt(fields, "score"),
                    completionTimeSeconds = getLong(fields, "completionTimeSeconds"),
                    ranking = getInt(fields, "ranking"),
                    timePeriod = getStr(fields, "timePeriod"),
                    region = getStr(fields, "region"),
                    constituency = getStr(fields, "constituency")
                )
                entries.add(entry)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Leaderboard document at $i: ${e.message}")
            }
        }
        if (entries.isNotEmpty()) {
            quizAppDao.insertLeaderboardEntries(entries)
        }
    }

    // Sync users from Firestore
    suspend fun syncUsers(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("users")
        if (docsArray.length() == 0) return

        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val user = UserEntity(
                    id = getStr(fields, "id"),
                    fullName = getStr(fields, "fullName"),
                    phoneNumber = getStr(fields, "phoneNumber"),
                    email = getStr(fields, "email"),
                    region = getStr(fields, "region"),
                    constituency = getStr(fields, "constituency"),
                    role = getStr(fields, "role"),
                    status = getStr(fields, "status"),
                    profilePhoto = getStr(fields, "profilePhoto"),
                    passwordHash = getStr(fields, "passwordHash"),
                    createdAt = getLong(fields, "createdAt"),
                    updatedAt = getLong(fields, "updatedAt"),
                    languagePreference = getStr(fields, "languagePreference")
                )
                quizAppDao.insertUser(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing User document at $i: ${e.message}")
            }
        }
    }

    // Sync audit logs from Firestore
    suspend fun syncAuditLogs(quizAppDao: QuizAppDao) {
        val docsArray = fetchCollectionDocuments("audit_logs")
        if (docsArray.length() == 0) return

        for (i in 0 until docsArray.length()) {
            try {
                val docObj = docsArray.getJSONObject(i)
                val fields = docObj.getJSONObject("fields")
                val log = AuditLogEntity(
                    id = getStr(fields, "id"),
                    adminId = getStr(fields, "adminId"),
                    adminName = getStr(fields, "adminName"),
                    action = getStr(fields, "action"),
                    target = getStr(fields, "target"),
                    timestamp = getLong(fields, "timestamp")
                )
                quizAppDao.insertAuditLog(log)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Audit Log document at $i: ${e.message}")
            }
        }
    }

    // Perform complete pull sync of all collections
    suspend fun performanceCompleteSync(quizAppDao: QuizAppDao) {
        Log.i(TAG, "Starting complete pull synchronization from Firestore...")
        syncCategories(quizAppDao)
        syncQuizzes(quizAppDao)
        syncQuestions(quizAppDao)
        syncSponsors(quizAppDao)
        syncAnnouncements(quizAppDao)
        syncLeaderboard(quizAppDao)
        syncUsers(quizAppDao)
        syncAuditLogs(quizAppDao)
        Log.i(TAG, "Finished complete pull synchronization.")
    }

    // Pushes supporting functions
    suspend fun pushCategory(category: CategoryEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to category.id,
            "categoryName" to category.categoryName,
            "categoryImage" to category.categoryImage,
            "description" to category.description,
            "active" to category.active,
            "createdAt" to category.createdAt
        ))
        return pushDocument("categories", category.id, fields)
    }

    suspend fun pushQuiz(quiz: QuizEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to quiz.id,
            "categoryId" to quiz.categoryId,
            "title" to quiz.title,
            "description" to quiz.description,
            "imageUrl" to quiz.imageUrl,
            "sponsorName" to quiz.sponsorName,
            "sponsorLogo" to quiz.sponsorLogo,
            "accessCode" to quiz.accessCode,
            "timeLimitMinutes" to quiz.timeLimitMinutes,
            "active" to quiz.active,
            "startDate" to quiz.startDate,
            "endDate" to quiz.endDate,
            "totalQuestions" to quiz.totalQuestions,
            "createdBy" to quiz.createdBy,
            "createdAt" to quiz.createdAt,
            "sponsorId" to quiz.sponsorId,
            "maxAttempts" to quiz.maxAttempts
        ))
        return pushDocument("quizzes", quiz.id, fields)
    }

    suspend fun pushQuestion(question: QuestionEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to question.id,
            "quizId" to question.quizId,
            "questionText" to question.questionText,
            "optionA" to question.optionA,
            "optionB" to question.optionB,
            "optionC" to question.optionC,
            "optionD" to question.optionD,
            "correctAnswer" to question.correctAnswer,
            "explanation" to question.explanation,
            "imageUrl" to question.imageUrl
        ))
        return pushDocument("questions", question.id, fields)
    }

    suspend fun pushSponsor(sponsor: SponsorEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to sponsor.id,
            "name" to sponsor.name,
            "logoUrl" to sponsor.logoUrl,
            "description" to sponsor.description,
            "active" to sponsor.active
        ))
        return pushDocument("sponsors", sponsor.id, fields)
    }

    suspend fun pushAnnouncement(announcement: AnnouncementEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to announcement.id,
            "title" to announcement.title,
            "content" to announcement.content,
            "createdAt" to announcement.createdAt,
            "scheduledAt" to announcement.scheduledAt,
            "isPushed" to announcement.isPushed,
            "active" to announcement.active,
            "imageUrl" to (announcement.imageUrl ?: ""),
            "linkUrl" to (announcement.linkUrl ?: ""),
            "linkLabel" to (announcement.linkLabel ?: "")
        ))
        return pushDocument("announcements", announcement.id, fields)
    }

    suspend fun pushLeaderboardEntry(entry: LeaderboardEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to entry.id,
            "userId" to entry.userId,
            "userFullName" to entry.userFullName,
            "quizId" to entry.quizId,
            "categoryId" to entry.categoryId,
            "score" to entry.score,
            "completionTimeSeconds" to entry.completionTimeSeconds,
            "ranking" to entry.ranking,
            "timePeriod" to entry.timePeriod,
            "region" to entry.region,
            "constituency" to entry.constituency
        ))
        return pushDocument("leaderboard", entry.id, fields)
    }

    suspend fun pushUser(user: UserEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to user.id,
            "fullName" to user.fullName,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "region" to user.region,
            "constituency" to user.constituency,
            "role" to user.role,
            "status" to user.status,
            "profilePhoto" to user.profilePhoto,
            "passwordHash" to user.passwordHash,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt,
            "languagePreference" to user.languagePreference
        ))
        return pushDocument("users", user.id, fields)
    }

    suspend fun pushAuditLog(log: AuditLogEntity): Boolean {
        val fields = mapToFields(mapOf(
            "id" to log.id,
            "adminId" to log.adminId,
            "adminName" to log.adminName,
            "action" to log.action,
            "target" to log.target,
            "timestamp" to log.timestamp
        ))
        return pushDocument("audit_logs", log.id, fields)
    }
}
