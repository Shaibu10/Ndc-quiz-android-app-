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
import kotlinx.coroutines.flow.first
import java.util.UUID

interface QuizRepository {
    // Current active session
    val currentUserState: StateFlow<UserEntity?>
    suspend fun getSessionUser(): UserEntity?

    // Auth
    suspend fun loginWithEmail(email: String, passwordHash: String): Result<UserEntity>
    suspend fun loginWithPhone(phone: String, passwordHash: String): Result<UserEntity>
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

    // Seed Data
    suspend fun checkAndSeedDatabase()

    // Analytics / Admin
    val allUsersFlow: Flow<List<UserEntity>>
    val auditLogsFlow: Flow<List<AuditLogEntity>>
    suspend fun updateUserStatus(userId: String, status: String): Result<Unit>
    suspend fun updateUserRole(userId: String, role: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun addAuditLog(action: String, target: String)
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
    }

    // Attempts and Leaderboard
    override val attemptsFlow: Flow<List<QuizAttemptEntity>>
        get() = _currentUserState.value?.let { quizAppDao.getAttemptsByUserFlow(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())

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
            quizId != null -> quizAppDao.deleteLeaderboardByQuiz(quizId)
            categoryId != null -> quizAppDao.deleteLeaderboardByCategory(categoryId)
            else -> quizAppDao.clearAllLeaderboard()
        }
        addAuditLog("LEADERBOARD_CLEAR", "Cleared matches for Quiz: $quizId, Cat: $categoryId")
        return Result.success(Unit)
    }

    override suspend fun deleteLeaderboardEntry(id: String): Result<Unit> {
        quizAppDao.deleteLeaderboardById(id)
        addAuditLog("LEADERBOARD_ENTRY_DEL", "Deleted specific leaderboard record ID: $id")
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

    override suspend fun checkAndSeedDatabase() {
        // Try to sync with Firestore remote database first
        try {
            FirebaseFirestoreSync.performanceCompleteSync(quizAppDao)
        } catch (e: Exception) {
            Log.e("QuizRepository", "First-time remote database sync failed/offline: ${e.message}")
        }

        val existingCats = quizAppDao.getAllCategoriesFlow().first()
        if (existingCats.isNotEmpty()) {
            Log.i("QuizRepository", "Database populated successfully (size: ${existingCats.size} categories)")
            return // Seeded already (either via Firestore pull or local db cache)
        }

        Log.w("QuizRepository", "Empty database detected. Performing initial local high-fidelity seeding and Firestore uploads...")

        // 1. Categories
        val cats = listOf(
            CategoryEntity("cat1", "NDC History", "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?q=80&w=260", "The rich revolutionary foundation, philosophies and timeline of the NDC party."),
            CategoryEntity("cat2", "NDC Leadership", "https://images.unsplash.com/photo-1517048676732-d65bc937f952?q=80&w=260", "Flagbearers, Presidents, National Chairpersons, and founders of the congress."),
            CategoryEntity("cat3", "Ghana Politics", "https://images.unsplash.com/photo-1450133064473-71024230f91b?q=80&w=260", "General political milestones, administrative divisions, and democratic transitions."),
            CategoryEntity("cat4", "Election Campaign & Manifestos", "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?q=80&w=260", "Highlighting policies, presidential promises, and key party platforms."),
            CategoryEntity("cat5", "Party Constitution & Structure", "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?q=80&w=260", "The written guidelines, hierarchy, principles, and internal organs of the NDC.")
        )
        cats.forEach { quizAppDao.insertCategory(it) }

        // 2. Administrators / Users
        val adminUser = UserEntity(
            id = "admin_shaibu",
            fullName = "Shaibu Mahama",
            phoneNumber = "+233244123456",
            email = "shaibu5278@gmail.com",
            region = "Greater Accra",
            constituency = "Ayawaso West Wuogon",
            role = "Super Admin",
            status = "Active",
            profilePhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200",
            passwordHash = "47fdac4f" // first 8 characters of app ID serves as dummy hashed password
        )
        quizAppDao.insertUser(adminUser)

        val partyUser = UserEntity(
            id = "test_user_id",
            fullName = "Kwame Mensah",
            phoneNumber = "+233201112222",
            email = "kwame@ndc.com",
            region = "Ashanti",
            constituency = "Asawase",
            role = "User",
            status = "Active",
            profilePhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200",
            passwordHash = "123456"
        )
        quizAppDao.insertUser(partyUser)

        // 3. Quizzes
        val q1 = QuizEntity(
            id = "quiz1",
            categoryId = "cat1",
            title = "NDC Historical Milestone Quiz",
            description = "Test your depth on the foundation, PNDC evolution, and Jerry John Rawlings legacy.",
            imageUrl = "https://images.unsplash.com/photo-1450133064473-71024230f91b?q=80&w=400",
            sponsorName = "NDC Greater Accra Caucus",
            sponsorLogo = "https://images.unsplash.com/photo-1560179707-f14e90ef3623?q=80&w=100",
            accessCode = "",
            timeLimitMinutes = 3,
            startDate = "2026-06-01",
            endDate = "2026-12-31",
            totalQuestions = 4,
            createdBy = "admin_shaibu",
            sponsorId = "sp1",
            maxAttempts = 3
        )
        val q2 = QuizEntity(
            id = "quiz2",
            categoryId = "cat2",
            title = "Atta Mills & Mahama Leadership Quiz",
            description = "Understand the philosophies, policy programs, and social infrastructure legacy of John Evans Atta Mills and John Dramani Mahama.",
            imageUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?q=80&w=400",
            sponsorName = "John Mahama Action Group",
            sponsorLogo = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=100",
            accessCode = "NDC2026", // Requires code!
            timeLimitMinutes = 5,
            startDate = "2026-06-01",
            endDate = "2026-12-31",
            totalQuestions = 3,
            createdBy = "admin_shaibu",
            sponsorId = "sp2",
            maxAttempts = 1
        )
        quizAppDao.insertQuiz(q1)
        quizAppDao.insertQuiz(q2)

        // 4. Questions
        val questions = listOf(
            // Quiz 1 Questions (NDC History)
            QuestionEntity(
                id = "qn1_1",
                quizId = "quiz1",
                questionText = "In what year was the National Democratic Congress (NDC) officially founded?",
                optionA = "1981",
                optionB = "1992",
                optionC = "1996",
                optionD = "2000",
                correctAnswer = "B",
                explanation = "The NDC was founded in 1992 by Jerry John Rawlings, transitioning Ghana into democratic governance under the Fourth Republic.",
                imageUrl = ""
            ),
            QuestionEntity(
                id = "qn1_2",
                quizId = "quiz1",
                questionText = "Which visual icon represents unity, shelter, and security on the official NDC party logo?",
                optionA = "The Red Rooster",
                optionB = "The Umbrella with an Eagle",
                optionC = "The Golden Elephant",
                optionD = "The Rising Sun",
                correctAnswer = "B",
                explanation = "The Umbrella (Akatamanso) with an Eagle perched on top symbolises nationwide unity, protection, shelter for all, and soaring leadership.",
                imageUrl = ""
            ),
            QuestionEntity(
                id = "qn1_3",
                quizId = "quiz1",
                questionText = "What colors compile the official colors of the NDC party?",
                optionA = "Green, White, Red, and Black",
                optionB = "Blue, White, and Red",
                optionC = "Red, Gold, Green, and Black",
                optionD = "Green, Yellow, and Purple",
                correctAnswer = "A",
                explanation = "NDC's official brand colors are Green (rich land), White (peace/purity), Red (martyr sacrifice), and Black (African dignity).",
                imageUrl = ""
            ),
            QuestionEntity(
                id = "qn1_4",
                quizId = "quiz1",
                questionText = "Who was chosen as the NDC Vice-Presidential candidate alongside Jerry John Rawlings in the historic 1992 election?",
                optionA = "Kow Nkensen Arkaah",
                optionB = "John Evans Atta Mills",
                optionC = "Martin Amidu",
                optionD = "Amissah-Arthur",
                correctAnswer = "A",
                explanation = "Kow Nkensen Arkaah of the National Convention Party (NCP) was Rawlings' running mate in 1992 as part of the Progressive Alliance.",
                imageUrl = ""
            ),

            // Quiz 2 Questions (Leadership)
            QuestionEntity(
                id = "qn2_1",
                quizId = "quiz2",
                questionText = "Which former NDC President of Ghana was affectionately nicknamed the 'Asomdwee Hene' (King of Peace)?",
                optionA = "Jerry John Rawlings",
                optionB = "John Evans Atta Mills",
                optionC = "John Dramani Mahama",
                optionD = "Kwame Nkrumah",
                correctAnswer = "B",
                explanation = "Professor John Evans Atta Mills was loved for his serene demeanor, peaceful administrative values and humility, earning him the title Asomdwee Hene.",
                imageUrl = ""
            ),
            QuestionEntity(
                id = "qn2_2",
                quizId = "quiz2",
                questionText = "H.E. John Dramani Mahama served of which national position prior to becoming the President of Ghana?",
                optionA = "Speaker of Parliament",
                optionB = "Chief Justice",
                optionC = "Vice President & MP for Bole Bamboi",
                optionD = "Mayor of Accra",
                correctAnswer = "C",
                explanation = "John Dramani Mahama served as Member of Parliament for Bole Bamboi, Minister of Communication, and Vice President before becoming President.",
                imageUrl = ""
            ),
            QuestionEntity(
                id = "qn2_3",
                quizId = "quiz2",
                questionText = "Which major economic masterstroke was initiated under John Mahama's leadership to address electricity stability?",
                optionA = "The Bui Dam construction completion",
                optionB = "The introduction of the Karpowership and Ameri Power plants",
                optionC = "Akosombo expansion only",
                optionD = "Solar grid privatization",
                correctAnswer = "B",
                explanation = "President John Mahama resolved the power crisis ('Dumsor') systematically by adding emergency and thermal generation capacities via Karpowership and Ameri.",
                imageUrl = ""
            )
        )
        quizAppDao.insertQuestions(questions)

        // 5. Sponsors
        val s1 = SponsorEntity("sp1", "NDC Greater Accra Caucus", "https://images.unsplash.com/photo-1560179707-f14e90ef3623?q=80&w=100", "Supporting youth development.")
        val s2 = SponsorEntity("sp2", "John Mahama Action Group", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=100", "Building the Ghana we want together.")
        quizAppDao.insertSponsor(s1)
        quizAppDao.insertSponsor(s2)

        // 6. Announcements
        val an1 = AnnouncementEntity(
            id = "an_1",
            title = "🏆 National NDC Quiz Battle 2026 Launches!",
            content = "Participate and win laptops, tablets, and cash awards sponsored by party patriots. Quizzes refresh daily!"
        )
        val an2 = AnnouncementEntity(
            id = "an_2",
            title = "💼 Youth Employment and Tech Workshops",
            content = "Join our virtual tech and training skills session this Saturday at 4 PM GMT. Check dashboard for webinar links."
        )
        quizAppDao.insertAnnouncement(an1)
        quizAppDao.insertAnnouncement(an2)

        // 7. Simulated Leaderboard
        val testLeaders = listOf(
            LeaderboardEntity(UUID.randomUUID().toString(), "test_user_id", "Kwame Mensah", "quiz1", "cat1", 4, 45, 1, "Global", "Ashanti", "Asawase"),
            LeaderboardEntity(UUID.randomUUID().toString(), "dummy_1", "Efua Ansah", "quiz1", "cat1", 3, 55, 2, "Global", "Central", "Cape Coast South"),
            LeaderboardEntity(UUID.randomUUID().toString(), "dummy_2", "Alhassan Ibrahim", "quiz1", "cat1", 3, 62, 3, "Global", "Northern", "Tamale Central"),
            LeaderboardEntity(UUID.randomUUID().toString(), "dummy_3", "Aba Mensah", "quiz2", "cat2", 3, 90, 1, "Global", "Western", "Takoradi")
        )
        quizAppDao.insertLeaderboardEntries(testLeaders)

        // Push all this newly created seed data up to Remote Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cats.forEach { FirebaseFirestoreSync.pushCategory(it) }
                FirebaseFirestoreSync.pushUser(adminUser)
                FirebaseFirestoreSync.pushUser(partyUser)
                FirebaseFirestoreSync.pushQuiz(q1)
                FirebaseFirestoreSync.pushQuiz(q2)
                questions.forEach { FirebaseFirestoreSync.pushQuestion(it) }
                FirebaseFirestoreSync.pushSponsor(s1)
                FirebaseFirestoreSync.pushSponsor(s2)
                FirebaseFirestoreSync.pushAnnouncement(an1)
                FirebaseFirestoreSync.pushAnnouncement(an2)
                testLeaders.forEach { FirebaseFirestoreSync.pushLeaderboardEntry(it) }
                Log.i("QuizRepository", "Successfully uploaded all seed data to remote Firestore backend!")
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed uploading seed data to remote: ${e.message}")
            }
        }
    }
}
