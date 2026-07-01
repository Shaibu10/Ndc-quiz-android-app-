package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val region: String,
    val constituency: String,
    val role: String,
    val status: String,
    val profilePhoto: String,
    val passwordHash: String,
    val languagePreference: String = "English",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val categoryName: String,
    val categoryImage: String,
    val description: String
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val sponsorName: String,
    val sponsorLogo: String,
    val accessCode: String,
    val timeLimitMinutes: Int,
    val startDate: String,
    val endDate: String,
    val totalQuestions: Int,
    val createdBy: String,
    val sponsorId: String,
    val maxAttempts: Int,
    val active: Boolean = true
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val explanation: String,
    val imageUrl: String
)

@Entity(tableName = "attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val quizId: String,
    val score: Int,
    val completionTimeSeconds: Long,
    val startedAt: Long,
    val submittedAt: Long
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userFullName: String,
    val quizId: String? = null,
    val categoryId: String? = null,
    val score: Int,
    val completionTimeSeconds: Long,
    val ranking: Int,
    val timePeriod: String = "Global",
    val region: String,
    val constituency: String
)

@Entity(tableName = "sponsors")
data class SponsorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logoUrl: String,
    val description: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val linkUrl: String? = null,
    val linkLabel: String? = null,
    val active: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val adminId: String,
    val adminName: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis()
)
