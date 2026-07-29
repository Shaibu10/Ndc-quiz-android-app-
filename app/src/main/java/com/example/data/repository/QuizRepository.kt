package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.FirebaseFirestoreSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import java.util.UUID

interface QuizRepository {
    // Current active session
    val currentUserState: StateFlow<UserEntity?>
    suspend fun getSessionUser(): UserEntity?

    // Auth
    suspend fun loginWithEmail(email: String, passwordHash: String): Result<UserEntity>
    suspend fun loginWithPhone(phone: String, passwordHash: String): Result<UserEntity>
    suspend fun loginWithUserId(userId: String): Result<UserEntity>
    suspend fun registerUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        region: String,
        constituency: String,
        passwordHash: String
    ): Result<UserEntity>
    suspend fun logout()
    suspend fun updateProfile(fullName: String, email: String, region: String, constituency: String, languagePreference: String, profilePhoto: String): Result<UserEntity>
    suspend fun changePassword(oldPasswordHash: String, newPasswordHash: String): Result<Unit>

    // Categories
    val categoriesFlow: Flow<List<CategoryEntity>>
    suspend fun createCategory(category: CategoryEntity)
    suspend fun deleteCategory(categoryId: String)

    // Quizzes
    val quizzesFlow: Flow<List<QuizEntity>>
    fun getQuizzesByCategory(categoryId: String): Flow<List<QuizEntity>>
    suspend fun getQuizById(quizId: String): QuizEntity?
    suspend fun createQuiz(quiz: QuizEntity)
    suspend fun deleteQuiz(quizId: String)
    suspend fun toggleQuizActive(quizId: String): Result<Unit>

    // Questions
    fun getQuestionsForQuiz(quizId: String): Flow<List<QuestionEntity>>
    suspend fun getQuestionsForQuizList(quizId: String): List<QuestionEntity>
    suspend fun insertQuestions(questions: List<QuestionEntity>)
    suspend fun insertQuestion(question: QuestionEntity)
    suspend fun deleteQuestion(id: String)

    // Attempts and Leaderboard
    val attemptsFlow: Flow<List<QuizAttemptEntity>>
    fun getLeaderboard(quizId: String? = null, categoryId: String? = null, period: String = "Global"): Flow<List<LeaderboardEntity>>
    suspend fun submitQuizAttempt(userId: String, quizId: String, score: Int, timeUsedSeconds: Long): Result<QuizAttemptEntity>
    suspend fun getAttemptsByQuizAndUser(quizId: String, userId: String): List<QuizAttemptEntity>
    suspend fun clearLeaderboard(quizId: String?, categoryId: String?): Result<Unit>
    suspend fun deleteLeaderboardEntry(id: String): Result<Unit>

    // Sponsors
    val sponsorsFlow: Flow<List<SponsorEntity>>
    suspend fun createSponsor(sponsor: SponsorEntity)
    suspend fun deleteSponsor(id: String)

    // Announcements
    val announcementsFlow: Flow<List<AnnouncementEntity>>
    suspend fun createAnnouncement(
        title: String,
        content: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        linkLabel: String? = null
    ): Result<AnnouncementEntity>
    suspend fun deleteAnnouncement(id: String)
    suspend fun toggleAnnouncementActive(id: String): Result<Unit>

    // Sync
    suspend fun syncWithRemoteDatabase()

    // Analytics / Admin
    val allUsersFlow: Flow<List<UserEntity>>
    val auditLogsFlow: Flow<List<AuditLogEntity>>
    suspend fun updateUserStatus(userId: String, status: String): Result<Unit>
    suspend fun updateUserRole(userId: String, role: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun addAuditLog(action: String, target: String)
    suspend fun clearAllAuditLogs(): Result<Unit>
    suspend fun runFirebaseDiagnostics(): Result<String>
    suspend fun forceUploadAllToFirebase(): Result<String>
}

class SupabaseOfflineFirstQuizRepository(
    private val quizAppDao: QuizAppDao
) : QuizRepository {

    private val _currentUserState = MutableStateFlow<UserEntity?>(null)
    override val currentUserState: StateFlow<UserEntity?> = _currentUserState.asStateFlow()

    override suspend fun getSessionUser(): UserEntity? = _currentUserState.value

    override suspend fun loginWithEmail(email: String, passwordHash: String): Result<UserEntity> {
        val user = quizAppDao.getUserByEmailAndPassword(email, passwordHash)
        return if (user != null) {
            if (user.status == "Suspended") {
                Result.failure(Exception("This account is suspended. Contact Admin."))
            } else {
                _currentUserState.value = user
                addAuditLog("USER_LOGIN", "Email Login: ${user.fullName}")
                Result.success(user)
            }
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }

    override suspend fun loginWithPhone(phone: String, passwordHash: String): Result<UserEntity> {
        val user = quizAppDao.getUserByPhoneAndPassword(phone, passwordHash)
        return if (user != null) {
            if (user.status == "Suspended") {
                Result.failure(Exception("This account is suspended. Contact Admin."))
            } else {
                _currentUserState.value = user
                addAuditLog("USER_LOGIN", "Phone Login: ${user.fullName}")
                Result.success(user)
            }
        } else {
            Result.failure(Exception("Invalid phone number or password."))
        }
    }

    override suspend fun loginWithUserId(userId: String): Result<UserEntity> {
        val user = quizAppDao.getUserById(userId)
        return if (user != null) {
            _currentUserState.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("User not found."))
        }
    }

    override suspend fun registerUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        region: String,
        constituency: String,
        passwordHash: String
    ): Result<UserEntity> {
        // Validate duplicates
        val existingEmail = quizAppDao.getUserByEmail(email)
        if (existingEmail != null) {
            return Result.failure(Exception("Email is already registered!"))
        }
        val existingPhone = quizAppDao.getUserByPhone(phoneNumber)
        if (existingPhone != null) {
            return Result.failure(Exception("Phone number is already registered!"))
        }

        // Auto-assign Admin to Shaibu (user specified email in user metadata context is shaibu5278@gmail.com)
        val role = if (email.equals("shaibu5278@gmail.com", ignoreCase = true) || email.equals("admin@ndc.com", ignoreCase = true)) "Super Admin" else "User"

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            region = region,
            constituency = constituency,
            role = role,
            status = "Active",
            profilePhoto = "",
            passwordHash = passwordHash
        )

        quizAppDao.insertUser(newUser)
        _currentUserState.value = newUser
        addAuditLog("USER_REGISTER", "Registered User: $fullName ($role)")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushUser(newUser)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed syncing registration to Firebase: ${e.message}")
            }
        }
        
        return Result.success(newUser)
    }

    override suspend fun logout() {
        val user = _currentUserState.value
        if (user != null) {
            addAuditLog("USER_LOGOUT", "Logout: ${user.fullName}")
        }
        _currentUserState.value = null
    }

    override suspend fun updateProfile(
        fullName: String,
        email: String,
        region: String,
        constituency: String,
        languagePreference: String,
        profilePhoto: String
    ): Result<UserEntity> {
        val currentUser = _currentUserState.value ?: return Result.failure(Exception("No active session."))
        val updatedUser = currentUser.copy(
            fullName = fullName,
            email = email,
            region = region,
            constituency = constituency,
            languagePreference = languagePreference,
            profilePhoto = profilePhoto,
            updatedAt = System.currentTimeMillis()
        )
        quizAppDao.insertUser(updatedUser)
        _currentUserState.value = updatedUser
        addAuditLog("PROFILE_UPDATE", "Updated details for ${fullName}")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushUser(updatedUser)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed syncing profile update to Firebase: ${e.message}")
            }
        }
        
        return Result.success(updatedUser)
    }

    override suspend fun changePassword(oldPasswordHash: String, newPasswordHash: String): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(Exception("No active session."))
        if (currentUser.passwordHash != oldPasswordHash) {
            return Result.failure(Exception("Incorrect current password."))
        }
        val updatedUser = currentUser.copy(passwordHash = newPasswordHash, updatedAt = System.currentTimeMillis())
        quizAppDao.insertUser(updatedUser)
        _currentUserState.value = updatedUser
        addAuditLog("PASSWORD_CHANGE", "Changed password")
        return Result.success(Unit)
    }

    // Categories
    override val categoriesFlow: Flow<List<CategoryEntity>> = quizAppDao.getAllCategoriesFlow()

    override suspend fun createCategory(category: CategoryEntity) {
        quizAppDao.insertCategory(category)
        addAuditLog("CATEGORY_CREATE", "Created: ${category.categoryName}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushCategory(category)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed syncing Category to Firebase: ${e.message}")
            }
        }
    }

    override suspend fun deleteCategory(categoryId: String) {
        quizAppDao.deleteCategoryById(categoryId)
        addAuditLog("CATEGORY_DELETE", "Deleted Category ID: $categoryId")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteCategory(categoryId)
                FirebaseFirestoreSync.deleteLeaderboardByCategory(categoryId)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting Category from Firebase: ${e.message}")
            }
        }
    }

    // Quizzes
    override val quizzesFlow: Flow<List<QuizEntity>> = quizAppDao.getAllQuizzesFlow()

    override fun getQuizzesByCategory(categoryId: String): Flow<List<QuizEntity>> {
        return quizAppDao.getQuizzesByCategoryFlow(categoryId)
    }

    override suspend fun getQuizById(quizId: String): QuizEntity? {
        return quizAppDao.getQuizById(quizId)
    }

    override suspend fun createQuiz(quiz: QuizEntity) {
        quizAppDao.insertQuiz(quiz)
        addAuditLog("QUIZ_CREATE", "Created Quiz: ${quiz.title}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushQuiz(quiz)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed syncing Quiz to Firebase: ${e.message}")
            }
        }
    }

    override suspend fun deleteQuiz(quizId: String) {
        quizAppDao.deleteQuizById(quizId)
        quizAppDao.deleteQuestionsByQuizId(quizId)
        addAuditLog("QUIZ_DELETE", "Deleted Quiz ID: $quizId")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteQuiz(quizId)
                FirebaseFirestoreSync.deleteQuestionsByQuiz(quizId)
                FirebaseFirestoreSync.deleteLeaderboardByQuiz(quizId)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting Quiz from Firebase: ${e.message}")
            }
        }
    }

    override suspend fun toggleQuizActive(quizId: String): Result<Unit> {
        val quiz = quizAppDao.getQuizById(quizId) ?: return Result.failure(Exception("Quiz match not found"))
        val updated = quiz.copy(active = !quiz.active)
        quizAppDao.insertQuiz(updated)
        addAuditLog("QUIZ_TOGGLE_ACTIVE", "Toggled active state of Quiz ID: $quizId to ${updated.active}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushQuiz(updated)
            } catch (e: Exception) {}
        }
        return Result.success(Unit)
    }

    // Questions
    override fun getQuestionsForQuiz(quizId: String): Flow<List<QuestionEntity>> {
        return quizAppDao.getQuestionsByQuizFlow(quizId)
    }

    override suspend fun getQuestionsForQuizList(quizId: String): List<QuestionEntity> {
        return quizAppDao.getQuestionsByQuiz(quizId)
    }

    override suspend fun insertQuestions(questions: List<QuestionEntity>) {
        quizAppDao.insertQuestions(questions)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                questions.forEach { FirebaseFirestoreSync.pushQuestion(it) }
            } catch (e: Exception) {}
        }
    }

    override suspend fun insertQuestion(question: QuestionEntity) {
        quizAppDao.insertQuestion(question)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushQuestion(question)
            } catch (e: Exception) {}
        }
    }

    override suspend fun deleteQuestion(id: String) {
        quizAppDao.deleteQuestionById(id)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteQuestion(id)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting Question from Firebase: ${e.message}")
            }
        }
    }

    // Attempts and Leaderboard
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override val attemptsFlow: Flow<List<QuizAttemptEntity>> = _currentUserState.flatMapLatest { user ->
        if (user == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            quizAppDao.getAttemptsByUserFlow(user.id)
        }
    }

    override fun getLeaderboard(quizId: String?, categoryId: String?, period: String): Flow<List<LeaderboardEntity>> {
        return when {
            quizId != null -> quizAppDao.getLeaderboardByQuizFlow(quizId)
            categoryId != null -> quizAppDao.getLeaderboardByCategoryFlow(categoryId)
            else -> quizAppDao.getGlobalLeaderboardFlow()
        }
    }

    override suspend fun submitQuizAttempt(
        userId: String,
        quizId: String,
        score: Int,
        timeUsedSeconds: Long
    ): Result<QuizAttemptEntity> {
        val quiz = quizAppDao.getQuizById(quizId) ?: return Result.failure(Exception("Quiz not found"))
        val user = quizAppDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))

        val attempt = QuizAttemptEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            quizId = quizId,
            score = score,
            completionTimeSeconds = timeUsedSeconds,
            startedAt = System.currentTimeMillis() - (timeUsedSeconds * 1000),
            submittedAt = System.currentTimeMillis()
        )
        quizAppDao.insertAttempt(attempt)

        // Add user score to leaderboard automatically
        val existingLeaderboard = quizAppDao.getGlobalLeaderboardFlow().first()
        val userRank = existingLeaderboard.size + 1

        val leaderboardEntry = LeaderboardEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            userFullName = user.fullName,
            quizId = quizId,
            categoryId = quiz.categoryId,
            score = score,
            completionTimeSeconds = timeUsedSeconds,
            ranking = userRank,
            timePeriod = "Global",
            region = user.region,
            constituency = user.constituency
        )
        quizAppDao.insertLeaderboardEntry(leaderboardEntry)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushLeaderboardEntry(leaderboardEntry)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed syncing leaderboard score to Firebase Firestore: ${e.message}")
            }
        }

        return Result.success(attempt)
    }

    override suspend fun getAttemptsByQuizAndUser(quizId: String, userId: String): List<QuizAttemptEntity> {
        return quizAppDao.getAttemptsByQuizAndUser(quizId, userId)
    }

    override suspend fun clearLeaderboard(quizId: String?, categoryId: String?): Result<Unit> {
        when {
            quizId != null -> {
                quizAppDao.deleteLeaderboardByQuiz(quizId)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FirebaseFirestoreSync.deleteLeaderboardByQuiz(quizId)
                    } catch (e: Exception) {}
                }
            }
            categoryId != null -> {
                quizAppDao.deleteLeaderboardByCategory(categoryId)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FirebaseFirestoreSync.deleteLeaderboardByCategory(categoryId)
                    } catch (e: Exception) {}
                }
            }
            else -> {
                quizAppDao.clearAllLeaderboard()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FirebaseFirestoreSync.clearAllLeaderboard()
                    } catch (e: Exception) {}
                }
            }
        }
        addAuditLog("LEADERBOARD_CLEAR", "Cleared matches for Quiz: $quizId, Cat: $categoryId")
        return Result.success(Unit)
    }

    override suspend fun deleteLeaderboardEntry(id: String): Result<Unit> {
        quizAppDao.deleteLeaderboardById(id)
        addAuditLog("LEADERBOARD_ENTRY_DEL", "Deleted specific leaderboard record ID: $id")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteLeaderboardEntry(id)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting leaderboard entry from Firebase: ${e.message}")
            }
        }
        return Result.success(Unit)
    }

    // Sponsors
    override val sponsorsFlow: Flow<List<SponsorEntity>> = quizAppDao.getAllSponsorsFlow()

    override suspend fun createSponsor(sponsor: SponsorEntity) {
        quizAppDao.insertSponsor(sponsor)
        addAuditLog("SPONSOR_CREATE", "Added Sponsor: ${sponsor.name}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushSponsor(sponsor)
            } catch (e: Exception) {}
        }
    }

    override suspend fun deleteSponsor(id: String) {
        quizAppDao.deleteSponsorById(id)
        addAuditLog("SPONSOR_DELETE", "Deleted Sponsor ID: $id")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteSponsor(id)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting Sponsor from Firebase: ${e.message}")
            }
        }
    }

    // Announcements
    override val announcementsFlow: Flow<List<AnnouncementEntity>> = quizAppDao.getAllAnnouncementsFlow()

    override suspend fun createAnnouncement(
        title: String,
        content: String,
        imageUrl: String?,
        linkUrl: String?,
        linkLabel: String?
    ): Result<AnnouncementEntity> {
        val announcement = AnnouncementEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            imageUrl = imageUrl,
            linkUrl = linkUrl,
            linkLabel = linkLabel
        )
        quizAppDao.insertAnnouncement(announcement)
        addAuditLog("ANNOUNCEMENT_CREATE", "Created Announcement: $title")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushAnnouncement(announcement)
            } catch (e: Exception) {}
        }
        
        return Result.success(announcement)
    }

    override suspend fun deleteAnnouncement(id: String) {
        quizAppDao.deleteAnnouncementById(id)
        addAuditLog("ANNOUNCEMENT_DELETE", "Deleted Announcement ID: $id")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteAnnouncement(id)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting Announcement from Firebase: ${e.message}")
            }
        }
    }

    override suspend fun toggleAnnouncementActive(id: String): Result<Unit> {
        val announcement = quizAppDao.getAnnouncementById(id) ?: return Result.failure(Exception("Announcement match not found"))
        val updated = announcement.copy(active = !announcement.active)
        quizAppDao.insertAnnouncement(updated)
        addAuditLog("ANNOUNCEMENT_TOGGLE_ACTIVE", "Toggled active state of Announcement ID: $id to ${updated.active}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushAnnouncement(updated)
            } catch (e: Exception) {}
        }
        return Result.success(Unit)
    }

    // Analytics / Admin
    override val allUsersFlow: Flow<List<UserEntity>> = quizAppDao.getAllUsersFlow()
    override val auditLogsFlow: Flow<List<AuditLogEntity>> = quizAppDao.getAllAuditLogsFlow()

    override suspend fun updateUserStatus(userId: String, status: String): Result<Unit> {
        val user = quizAppDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val updated = user.copy(status = status, updatedAt = System.currentTimeMillis())
        quizAppDao.insertUser(updated)
        addAuditLog("SUPER_ADMIN_ACTION", "Set status of ${user.fullName} to $status")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushUser(updated)
            } catch (e: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        val user = quizAppDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val updated = user.copy(role = role, updatedAt = System.currentTimeMillis())
        quizAppDao.insertUser(updated)
        addAuditLog("SUPER_ADMIN_ACTION", "Promoted/Changed ${user.fullName} to $role")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushUser(updated)
            } catch (e: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        quizAppDao.deleteUserById(userId)
        addAuditLog("SUPER_ADMIN_ACTION", "Permanently deleted user: $userId")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.deleteUser(userId)
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed deleting User from Firebase: ${e.message}")
            }
        }
        return Result.success(Unit)
    }

    override suspend fun addAuditLog(action: String, target: String) {
        val currentAdmin = _currentUserState.value
        val log = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            adminId = currentAdmin?.id ?: "SYSTEM",
            adminName = currentAdmin?.fullName ?: "Guest / System",
            action = action,
            target = target
        )
        quizAppDao.insertAuditLog(log)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestoreSync.pushAuditLog(log)
            } catch (e: Exception) {}
        }
    }

    override suspend fun clearAllAuditLogs(): Result<Unit> {
        return try {
            quizAppDao.clearAllAuditLogs()
            addAuditLog("AUDIT_CLEAR", "All historic audit logs cleared from system.")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun runFirebaseDiagnostics(): Result<String> {
        return FirebaseFirestoreSync.runDiagnostics()
    }

    override suspend fun forceUploadAllToFirebase(): Result<String> {
        return FirebaseFirestoreSync.forceUploadAll(quizAppDao)
    }

    override suspend fun syncWithRemoteDatabase() {
        // Try to sync with Firestore remote database first
        try {
            FirebaseFirestoreSync.performanceCompleteSync(quizAppDao)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Remote database sync failed: ${e.message}")
        }
    }
}
