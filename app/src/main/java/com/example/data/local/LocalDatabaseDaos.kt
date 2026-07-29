package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAppDao {

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByPhoneAndPassword(phone: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM categories")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("SELECT * FROM quizzes")
    fun getAllQuizzesFlow(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE categoryId = :categoryId")
    fun getQuizzesByCategoryFlow(categoryId: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: String): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuizById(quizId: String)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsByQuizId(quizId: String)

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    fun getQuestionsByQuizFlow(quizId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsByQuiz(quizId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: String)

    @Query("SELECT * FROM attempts WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getAttemptsByUserFlow(userId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM leaderboard WHERE quizId = :quizId ORDER BY score DESC, completionTimeSeconds ASC")
    fun getLeaderboardByQuizFlow(quizId: String): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard WHERE categoryId = :categoryId ORDER BY score DESC, completionTimeSeconds ASC")
    fun getLeaderboardByCategoryFlow(categoryId: String): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard ORDER BY score DESC, completionTimeSeconds ASC")
    fun getGlobalLeaderboardFlow(): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntity)

    @Query("SELECT * FROM attempts WHERE quizId = :quizId AND userId = :userId ORDER BY submittedAt DESC")
    suspend fun getAttemptsByQuizAndUser(quizId: String, userId: String): List<QuizAttemptEntity>

    @Query("DELETE FROM leaderboard WHERE quizId = :quizId")
    suspend fun deleteLeaderboardByQuiz(quizId: String)

    @Query("DELETE FROM leaderboard WHERE categoryId = :categoryId")
    suspend fun deleteLeaderboardByCategory(categoryId: String)

    @Query("DELETE FROM leaderboard")
    suspend fun clearAllLeaderboard()

    @Query("DELETE FROM leaderboard WHERE id = :id")
    suspend fun deleteLeaderboardById(id: String)

    @Query("SELECT * FROM sponsors")
    fun getAllSponsorsFlow(): Flow<List<SponsorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsor(sponsor: SponsorEntity)

    @Query("DELETE FROM sponsors WHERE id = :id")
    suspend fun deleteSponsorById(id: String)

    @Query("SELECT * FROM announcements")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: String)

    @Query("SELECT * FROM announcements WHERE id = :id LIMIT 1")
    suspend fun getAnnouncementById(id: String): AnnouncementEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAllAuditLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntity>)
}
