package com.example.data.remote

import android.util.Log
import com.example.data.local.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseFirestoreSync {

    // Custom extension to safely await any Android Task with Coroutines without requiring external libraries
    private suspend fun <T> Task<T>.awaitTask(): T? = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Task failed with unknown error"))
            }
        }
    }

    suspend fun pushUser(user: UserEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userMap = mapOf(
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
                "languagePreference" to user.languagePreference,
                "createdAt" to user.createdAt,
                "updatedAt" to user.updatedAt
            )
            db.collection("users").document(user.id).set(userMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed user to Firestore: ${user.fullName}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing user: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushCategory(category: CategoryEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val categoryMap = mapOf(
                "id" to category.id,
                "categoryName" to category.categoryName,
                "categoryImage" to category.categoryImage,
                "description" to category.description
            )
            db.collection("categories").document(category.id).set(categoryMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed category to Firestore: ${category.categoryName}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing category: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushQuiz(quiz: QuizEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val quizMap = mapOf(
                "id" to quiz.id,
                "categoryId" to quiz.categoryId,
                "title" to quiz.title,
                "description" to quiz.description,
                "imageUrl" to quiz.imageUrl,
                "sponsorName" to quiz.sponsorName,
                "sponsorLogo" to quiz.sponsorLogo,
                "accessCode" to quiz.accessCode,
                "timeLimitMinutes" to quiz.timeLimitMinutes,
                "startDate" to quiz.startDate,
                "endDate" to quiz.endDate,
                "totalQuestions" to quiz.totalQuestions,
                "createdBy" to quiz.createdBy,
                "sponsorId" to quiz.sponsorId,
                "maxAttempts" to quiz.maxAttempts,
                "active" to quiz.active
            )
            db.collection("quizzes").document(quiz.id).set(quizMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed quiz to Firestore: ${quiz.title}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing quiz: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushQuestion(question: QuestionEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val questionMap = mapOf(
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
            )
            db.collection("questions").document(question.id).set(questionMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed question to Firestore: ${question.id}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing question: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushLeaderboardEntry(entry: LeaderboardEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val entryMap = mapOf(
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
            )
            db.collection("leaderboard").document(entry.id).set(entryMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed leaderboard entry to Firestore: ${entry.userFullName}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing leaderboard entry: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushSponsor(sponsor: SponsorEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val sponsorMap = mapOf(
                "id" to sponsor.id,
                "name" to sponsor.name,
                "logoUrl" to sponsor.logoUrl,
                "description" to sponsor.description
            )
            db.collection("sponsors").document(sponsor.id).set(sponsorMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed sponsor to Firestore: ${sponsor.name}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing sponsor: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushAnnouncement(announcement: AnnouncementEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val map = mapOf(
                "id" to announcement.id,
                "title" to announcement.title,
                "content" to announcement.content,
                "imageUrl" to announcement.imageUrl,
                "linkUrl" to announcement.linkUrl,
                "linkLabel" to announcement.linkLabel,
                "active" to announcement.active
            )
            db.collection("announcements").document(announcement.id).set(map).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed announcement to Firestore: ${announcement.title}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing announcement: ${e.message}", e)
            throw e
        }
    }

    suspend fun pushAuditLog(log: AuditLogEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val logMap = mapOf(
                "id" to log.id,
                "adminId" to log.adminId,
                "adminName" to log.adminName,
                "action" to log.action,
                "target" to log.target,
                "timestamp" to log.timestamp
            )
            db.collection("audit_logs").document(log.id).set(logMap).awaitTask()
            Log.d("FirebaseSync", "Successfully pushed audit log to Firestore: ${log.action}")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pushing audit log: ${e.message}", e)
            throw e
        }
    }

    suspend fun performanceCompleteSync(quizAppDao: QuizAppDao) {
        try {
            val db = FirebaseFirestore.getInstance()
            Log.d("FirebaseSync", "Initializing remote sync (PULL from Firestore)...")

            // 1. Fetch categories
            val categoriesSnap = db.collection("categories").get().awaitTask()
            if (categoriesSnap != null && !categoriesSnap.isEmpty) {
                for (doc in categoriesSnap.documents) {
                    val id = doc.id
                    val categoryName = doc.getString("categoryName") ?: ""
                    val categoryImage = doc.getString("categoryImage") ?: ""
                    val description = doc.getString("description") ?: ""
                    quizAppDao.insertCategory(CategoryEntity(id, categoryName, categoryImage, description))
                }
                Log.d("FirebaseSync", "Pulled ${categoriesSnap.size()} categories from Firestore")
            }

            // 2. Fetch users
            val usersSnap = db.collection("users").get().awaitTask()
            if (usersSnap != null && !usersSnap.isEmpty) {
                for (doc in usersSnap.documents) {
                    val id = doc.id
                    val fullName = doc.getString("fullName") ?: ""
                    val phoneNumber = doc.getString("phoneNumber") ?: ""
                    val email = doc.getString("email") ?: ""
                    val region = doc.getString("region") ?: ""
                    val constituency = doc.getString("constituency") ?: ""
                    val role = doc.getString("role") ?: ""
                    val status = doc.getString("status") ?: ""
                    val profilePhoto = doc.getString("profilePhoto") ?: ""
                    val passwordHash = doc.getString("passwordHash") ?: ""
                    val languagePreference = doc.getString("languagePreference") ?: "English"
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    quizAppDao.insertUser(
                        UserEntity(
                            id = id,
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email,
                            region = region,
                            constituency = constituency,
                            role = role,
                            status = status,
                            profilePhoto = profilePhoto,
                            passwordHash = passwordHash,
                            languagePreference = languagePreference,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
                Log.d("FirebaseSync", "Pulled ${usersSnap.size()} users from Firestore")
            }

            // 3. Fetch quizzes
            val quizzesSnap = db.collection("quizzes").get().awaitTask()
            if (quizzesSnap != null && !quizzesSnap.isEmpty) {
                for (doc in quizzesSnap.documents) {
                    val id = doc.id
                    val categoryId = doc.getString("categoryId") ?: ""
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val sponsorName = doc.getString("sponsorName") ?: ""
                    val sponsorLogo = doc.getString("sponsorLogo") ?: ""
                    val accessCode = doc.getString("accessCode") ?: ""
                    val timeLimitMinutes = doc.getLong("timeLimitMinutes")?.toInt() ?: 15
                    val startDate = doc.getString("startDate") ?: ""
                    val endDate = doc.getString("endDate") ?: ""
                    val totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 10
                    val createdBy = doc.getString("createdBy") ?: ""
                    val sponsorId = doc.getString("sponsorId") ?: ""
                    val maxAttempts = doc.getLong("maxAttempts")?.toInt() ?: 3
                    val active = doc.getBoolean("active") ?: true
                    quizAppDao.insertQuiz(
                        QuizEntity(
                            id = id,
                            categoryId = categoryId,
                            title = title,
                            description = description,
                            imageUrl = imageUrl,
                            sponsorName = sponsorName,
                            sponsorLogo = sponsorLogo,
                            accessCode = accessCode,
                            timeLimitMinutes = timeLimitMinutes,
                            startDate = startDate,
                            endDate = endDate,
                            totalQuestions = totalQuestions,
                            createdBy = createdBy,
                            sponsorId = sponsorId,
                            maxAttempts = maxAttempts,
                            active = active
                        )
                    )
                }
                Log.d("FirebaseSync", "Pulled ${quizzesSnap.size()} quizzes from Firestore")
            }

            // 4. Fetch questions
            val questionsSnap = db.collection("questions").get().awaitTask()
            if (questionsSnap != null && !questionsSnap.isEmpty) {
                val qList = mutableListOf<QuestionEntity>()
                for (doc in questionsSnap.documents) {
                    val id = doc.id
                    val quizId = doc.getString("quizId") ?: ""
                    val questionText = doc.getString("questionText") ?: ""
                    val optionA = doc.getString("optionA") ?: ""
                    val optionB = doc.getString("optionB") ?: ""
                    val optionC = doc.getString("optionC") ?: ""
                    val optionD = doc.getString("optionD") ?: ""
                    val correctAnswer = doc.getString("correctAnswer") ?: ""
                    val explanation = doc.getString("explanation") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    qList.add(
                        QuestionEntity(
                            id = id,
                            quizId = quizId,
                            questionText = questionText,
                            optionA = optionA,
                            optionB = optionB,
                            optionC = optionC,
                            optionD = optionD,
                            correctAnswer = correctAnswer,
                            explanation = explanation,
                            imageUrl = imageUrl
                        )
                    )
                }
                if (qList.isNotEmpty()) {
                    quizAppDao.insertQuestions(qList)
                }
                Log.d("FirebaseSync", "Pulled ${questionsSnap.size()} questions from Firestore")
            }

            // 5. Fetch leaderboard
            val leaderboardSnap = db.collection("leaderboard").get().awaitTask()
            if (leaderboardSnap != null && !leaderboardSnap.isEmpty) {
                val lEntries = mutableListOf<LeaderboardEntity>()
                for (doc in leaderboardSnap.documents) {
                    val id = doc.id
                    val userId = doc.getString("userId") ?: ""
                    val userFullName = doc.getString("userFullName") ?: ""
                    val quizId = doc.getString("quizId")
                    val categoryId = doc.getString("categoryId")
                    val score = doc.getLong("score")?.toInt() ?: 0
                    val completionTimeSeconds = doc.getLong("completionTimeSeconds") ?: 0L
                    val ranking = doc.getLong("ranking")?.toInt() ?: 0
                    val timePeriod = doc.getString("timePeriod") ?: "Global"
                    val region = doc.getString("region") ?: ""
                    val constituency = doc.getString("constituency") ?: ""
                    lEntries.add(
                        LeaderboardEntity(
                            id = id,
                            userId = userId,
                            userFullName = userFullName,
                            quizId = quizId,
                            categoryId = categoryId,
                            score = score,
                            completionTimeSeconds = completionTimeSeconds,
                            ranking = ranking,
                            timePeriod = timePeriod,
                            region = region,
                            constituency = constituency
                        )
                    )
                }
                if (lEntries.isNotEmpty()) {
                    quizAppDao.insertLeaderboardEntries(lEntries)
                }
                Log.d("FirebaseSync", "Pulled ${leaderboardSnap.size()} leaderboard entries from Firestore")
            }

            // 6. Fetch sponsors
            val sponsorsSnap = db.collection("sponsors").get().awaitTask()
            if (sponsorsSnap != null && !sponsorsSnap.isEmpty) {
                for (doc in sponsorsSnap.documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val logoUrl = doc.getString("logoUrl") ?: ""
                    val description = doc.getString("description") ?: ""
                    quizAppDao.insertSponsor(SponsorEntity(id, name, logoUrl, description))
                }
                Log.d("FirebaseSync", "Pulled ${sponsorsSnap.size()} sponsors from Firestore")
            }

            // 7. Fetch announcements
            val announcementsSnap = db.collection("announcements").get().awaitTask()
            if (announcementsSnap != null && !announcementsSnap.isEmpty) {
                for (doc in announcementsSnap.documents) {
                    val id = doc.id
                    val title = doc.getString("title") ?: ""
                    val content = doc.getString("content") ?: ""
                    val imageUrl = doc.getString("imageUrl")
                    val linkUrl = doc.getString("linkUrl")
                    val linkLabel = doc.getString("linkLabel")
                    val active = doc.getBoolean("active") ?: true
                    quizAppDao.insertAnnouncement(
                        AnnouncementEntity(
                            id = id,
                            title = title,
                            content = content,
                            imageUrl = imageUrl,
                            linkUrl = linkUrl,
                            linkLabel = linkLabel,
                            active = active
                        )
                    )
                }
                Log.d("FirebaseSync", "Pulled ${announcementsSnap.size()} announcements from Firestore")
            }

            // 8. Fetch audit logs
            val logsSnap = db.collection("audit_logs").get().awaitTask()
            if (logsSnap != null && !logsSnap.isEmpty) {
                for (doc in logsSnap.documents) {
                    val id = doc.id
                    val adminId = doc.getString("adminId") ?: ""
                    val adminName = doc.getString("adminName") ?: ""
                    val action = doc.getString("action") ?: ""
                    val target = doc.getString("target") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    quizAppDao.insertAuditLog(AuditLogEntity(id, adminId, adminName, action, target, timestamp))
                }
                Log.d("FirebaseSync", "Pulled ${logsSnap.size()} audit logs from Firestore")
            }

            Log.d("FirebaseSync", "Remote database sync (PULL) completed successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error pulling during complete sync: ${e.message}", e)
        }
    }

    suspend fun runDiagnostics(): Result<String> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val testDocId = "test_diag_" + System.currentTimeMillis()
            val testMap = mapOf(
                "status" to "healthy",
                "timestamp" to System.currentTimeMillis(),
                "device" to android.os.Build.MODEL
            )
            
            // 1. Try to Write
            db.collection("diagnostics").document(testDocId).set(testMap).awaitTask()
            
            // 2. Try to Read
            val doc = db.collection("diagnostics").document(testDocId).get().awaitTask()
            if (doc == null || !doc.exists()) {
                throw Exception("Written test document could not be retrieved from Firestore.")
            }
            
            // 3. Try to Delete
            db.collection("diagnostics").document(testDocId).delete().awaitTask()
            
            Result.success("Success! Firebase connection is fully functional and writing to Firestore is allowed.")
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            val resolution = when {
                msg.contains("PERMISSION_DENIED", ignoreCase = true) -> 
                    "PERMISSION_DENIED: Insufficient permissions.\n\nResolution: Go to your Firebase Console -> Firestore Database -> Rules tab, and update your security rules to allow read/write access (e.g., set 'allow read, write: if true;')."
                msg.contains("UNAVAILABLE", ignoreCase = true) -> 
                    "UNAVAILABLE: Cannot connect to Firestore.\n\nResolution: Ensure your device has internet access and that you have initialized the 'Cloud Firestore' database in your Firebase Console for the project 'ndc-quiz-android-app'."
                else -> "Error: $msg\n\nResolution: Please verify that you have registered the package name 'com.aistudio.quizapp.abcdef' in your Firebase project settings and generated the correct google-services.json."
            }
            Result.failure(Exception(resolution))
        }
    }

    suspend fun forceUploadAll(quizAppDao: QuizAppDao): Result<String> {
        return try {
            val categoriesList = quizAppDao.getAllCategoriesFlow().first()
            val usersList = quizAppDao.getAllUsersFlow().first()
            val quizzesList = quizAppDao.getAllQuizzesFlow().first()
            val sponsorsList = quizAppDao.getAllSponsorsFlow().first()
            val announcementsList = quizAppDao.getAllAnnouncementsFlow().first()
            val auditLogsList = quizAppDao.getAllAuditLogsFlow().first()
            
            var catCount = 0
            var userCount = 0
            var quizCount = 0
            var qnCount = 0
            var spCount = 0
            var anCount = 0
            var logCount = 0
            
            for (cat in categoriesList) {
                pushCategory(cat)
                catCount++
            }
            for (user in usersList) {
                pushUser(user)
                userCount++
            }
            for (quiz in quizzesList) {
                pushQuiz(quiz)
                quizCount++
                val questions = quizAppDao.getQuestionsByQuiz(quiz.id)
                for (question in questions) {
                    pushQuestion(question)
                    qnCount++
                }
            }
            for (sponsor in sponsorsList) {
                pushSponsor(sponsor)
                spCount++
            }
            for (announcement in announcementsList) {
                pushAnnouncement(announcement)
                anCount++
            }
            for (log in auditLogsList) {
                pushAuditLog(log)
                logCount++
            }
            
            Result.success("Fully synchronized with Firestore!\n\nUploaded:\n• $catCount categories\n• $userCount users\n• $quizCount quizzes ($qnCount questions)\n• $spCount sponsors\n• $anCount announcements\n• $logCount audit logs.")
        } catch (e: Exception) {
            Result.failure(Exception("Sync failed: ${e.message}\n\nPlease run Firebase Diagnostics to troubleshoot connection issues."))
        }
    }

    suspend fun deleteUser(userId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted user from Firestore: $userId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting user: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("categories").document(categoryId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted category from Firestore: $categoryId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting category: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteQuiz(quizId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("quizzes").document(quizId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted quiz from Firestore: $quizId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting quiz: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteQuestion(questionId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("questions").document(questionId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted question from Firestore: $questionId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting question: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteQuestionsByQuiz(quizId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("questions").whereEqualTo("quizId", quizId).get().awaitTask()
            if (snap != null && !snap.isEmpty) {
                for (doc in snap.documents) {
                    db.collection("questions").document(doc.id).delete().awaitTask()
                }
            }
            Log.d("FirebaseSync", "Successfully deleted questions for quiz: $quizId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting questions for quiz: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteLeaderboardEntry(entryId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("leaderboard").document(entryId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted leaderboard entry from Firestore: $entryId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting leaderboard entry: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteLeaderboardByQuiz(quizId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("leaderboard").whereEqualTo("quizId", quizId).get().awaitTask()
            if (snap != null && !snap.isEmpty) {
                for (doc in snap.documents) {
                    db.collection("leaderboard").document(doc.id).delete().awaitTask()
                }
            }
            Log.d("FirebaseSync", "Successfully deleted leaderboard entries for quiz: $quizId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting leaderboard entries for quiz: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteLeaderboardByCategory(categoryId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("leaderboard").whereEqualTo("categoryId", categoryId).get().awaitTask()
            if (snap != null && !snap.isEmpty) {
                for (doc in snap.documents) {
                    db.collection("leaderboard").document(doc.id).delete().awaitTask()
                }
            }
            Log.d("FirebaseSync", "Successfully deleted leaderboard entries for category: $categoryId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting leaderboard entries for category: ${e.message}", e)
            throw e
        }
    }

    suspend fun clearAllLeaderboard() {
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("leaderboard").get().awaitTask()
            if (snap != null && !snap.isEmpty) {
                for (doc in snap.documents) {
                    db.collection("leaderboard").document(doc.id).delete().awaitTask()
                }
            }
            Log.d("FirebaseSync", "Successfully cleared all leaderboard entries from Firestore")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error clearing leaderboard: ${e.message}", e)
            throw e
        }
    }

    fun startRealtimeSync(quizAppDao: QuizAppDao) {
        val db = FirebaseFirestore.getInstance()

        // Sync Users
        db.collection("users").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                for (change in snapshot.documentChanges) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            val doc = change.document
                            val user = UserEntity(
                                id = doc.id,
                                fullName = doc.getString("fullName") ?: "",
                                phoneNumber = doc.getString("phoneNumber") ?: "",
                                email = doc.getString("email") ?: "",
                                region = doc.getString("region") ?: "",
                                constituency = doc.getString("constituency") ?: "",
                                role = doc.getString("role") ?: "",
                                status = doc.getString("status") ?: "",
                                profilePhoto = doc.getString("profilePhoto") ?: "",
                                passwordHash = doc.getString("passwordHash") ?: "",
                                languagePreference = doc.getString("languagePreference") ?: "English",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                            )
                            CoroutineScope(Dispatchers.IO).launch { quizAppDao.insertUser(user) }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            CoroutineScope(Dispatchers.IO).launch { quizAppDao.deleteUserById(change.document.id) }
                        }
                    }
                }
            }
        }
        // Sync Quizzes
        db.collection("quizzes").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                for (change in snapshot.documentChanges) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            val doc = change.document
                            val quiz = QuizEntity(
                                id = doc.id,
                                categoryId = doc.getString("categoryId") ?: "",
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                sponsorName = doc.getString("sponsorName") ?: "",
                                sponsorLogo = doc.getString("sponsorLogo") ?: "",
                                accessCode = doc.getString("accessCode") ?: "",
                                timeLimitMinutes = doc.getLong("timeLimitMinutes")?.toInt() ?: 15,
                                startDate = doc.getString("startDate") ?: "",
                                endDate = doc.getString("endDate") ?: "",
                                totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 10,
                                createdBy = doc.getString("createdBy") ?: "",
                                sponsorId = doc.getString("sponsorId") ?: "",
                                maxAttempts = doc.getLong("maxAttempts")?.toInt() ?: 3,
                                active = doc.getBoolean("active") ?: true
                            )
                            CoroutineScope(Dispatchers.IO).launch { quizAppDao.insertQuiz(quiz) }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            CoroutineScope(Dispatchers.IO).launch { quizAppDao.deleteQuizById(change.document.id) }
                        }
                    }
                }
            }
        }
    }

    suspend fun deleteSponsor(sponsorId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("sponsors").document(sponsorId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted sponsor from Firestore: $sponsorId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting sponsor: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAnnouncement(announcementId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("announcements").document(announcementId).delete().awaitTask()
            Log.d("FirebaseSync", "Successfully deleted announcement from Firestore: $announcementId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting announcement: ${e.message}", e)
            throw e
        }
    }
}
