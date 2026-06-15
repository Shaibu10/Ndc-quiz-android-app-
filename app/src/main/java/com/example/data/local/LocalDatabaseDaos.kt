package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAppDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun getUserByEmailAndPassword(email: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone AND passwordHash = :passwordHash")
    suspend fun getUserByPhoneAndPassword(phone: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE fullName LIKE :query OR email LIKE :query OR phoneNumber LIKE :query")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)


    // --- Categories ---
    @Query("SELECT * FROM categories WHERE active = 1")
    fun getActiveCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)


    // --- Quizzes ---
    @Query("SELECT * FROM quizzes WHERE active = 1")
    fun getActiveQuizzesFlow(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes")
    fun getAllQuizzesFlow(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE categoryId = :categoryId AND active = 1")
    fun getQuizzesByCategoryFlow(categoryId: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: String): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuizById(quizId: String)


    // --- Questions ---
    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    fun getQuestionsByQuizFlow(quizId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsByQuiz(quizId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsByQuizId(quizId: String)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: String)


    // --- Quiz Attempts ---
    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId")
    fun getAttemptsByUserFlow(userId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId")
    suspend fun getAttemptsByQuiz(quizId: String): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId AND userId = :userId")
    suspend fun getAttemptsByQuizAndUser(quizId: String, userId: String): List<QuizAttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)


    // --- Leaderboard ---
    @Query("SELECT * FROM leaderboard WHERE quizId = :quizId ORDER BY score DESC, completionTimeSeconds ASC")
    fun getLeaderboardByQuizFlow(quizId: String): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard WHERE categoryId = :catId ORDER BY score DESC, completionTimeSeconds ASC")
    fun getLeaderboardByCategoryFlow(catId: String): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard ORDER BY score DESC, completionTimeSeconds ASC")
    fun getGlobalLeaderboardFlow(): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntity>)

    @Query("DELETE FROM leaderboard WHERE quizId = :quizId")
    suspend fun deleteLeaderboardByQuiz(quizId: String)

    @Query("DELETE FROM leaderboard WHERE categoryId = :categoryId")
    suspend fun deleteLeaderboardByCategory(categoryId: String)

    @Query("DELETE FROM leaderboard")
    suspend fun clearAllLeaderboard()

    @Query("DELETE FROM leaderboard WHERE id = :id")
    suspend fun deleteLeaderboardById(id: String)


    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)


    // --- Sponsors ---
    @Query("SELECT * FROM sponsors WHERE active = 1")
    fun getActiveSponsorsFlow(): Flow<List<SponsorEntity>>

    @Query("SELECT * FROM sponsors")
    fun getAllSponsorsFlow(): Flow<List<SponsorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsor(sponsor: SponsorEntity)

    @Query("DELETE FROM sponsors WHERE id = :id")
    suspend fun deleteSponsorById(id: String)


    // --- Announcements ---
    @Query("SELECT * FROM announcements WHERE active = 1 ORDER BY createdAt DESC")
    fun getActiveAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("SELECT * FROM announcements WHERE id = :id")
    suspend fun getAnnouncementById(id: String): AnnouncementEntity?

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: String)
}
