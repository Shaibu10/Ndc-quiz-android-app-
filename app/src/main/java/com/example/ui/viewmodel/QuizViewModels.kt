package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(private val repository: QuizRepository) : ViewModel() {

    // Auth states
    val currentUser = repository.currentUserState

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted = _onboardingCompleted.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Registration UI states
    var regFullName = MutableStateFlow("")
    var regEmail = MutableStateFlow("")
    var regPhone = MutableStateFlow("")
    var regRegion = MutableStateFlow("")
    var regConstituency = MutableStateFlow("")
    var regPassword = MutableStateFlow("")

    val regionsList = listOf(
        "Greater Accra", "Ashanti", "Eastern", "Central", "Western", 
        "Western North", "Volta", "Oti", "Northern", "Savannah", 
        "North East", "Upper East", "Upper West", "Bono", "Bono East", "Ahafo"
    )

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    fun completeOnboarding() {
        _onboardingCompleted.value = true
    }

    fun login(emailOrPhone: String, passwordHash: String, onMainSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            val trimmedEmailOrPhone = emailOrPhone.trim()
            val trimmedPassword = passwordHash.trim()
            val isEmail = trimmedEmailOrPhone.contains("@")
            val result = if (isEmail) {
                repository.loginWithEmail(trimmedEmailOrPhone, trimmedPassword)
            } else {
                repository.loginWithPhone(trimmedEmailOrPhone, trimmedPassword)
            }

            result.fold(
                onSuccess = { user ->
                    _isLoading.value = false
                    onMainSuccess(user)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _authError.value = error.message ?: "Authentication failed."
                }
            )
        }
    }

    fun register(onMainSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null

            val name = regFullName.value.trim()
            val email = regEmail.value.trim()
            val phone = regPhone.value.trim()
            val region = regRegion.value.trim()
            val constituency = regConstituency.value.trim()
            val pass = regPassword.value.trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || region.isEmpty() || constituency.isEmpty() || pass.isEmpty()) {
                _isLoading.value = false
                _authError.value = "All registration fields are required."
                return@launch
            }

            val result = repository.registerUser(
                fullName = name,
                email = email,
                phoneNumber = phone,
                region = region,
                constituency = constituency,
                passwordHash = pass
            )

            result.fold(
                onSuccess = { user ->
                    _isLoading.value = false
                    clearInputs()
                    onMainSuccess(user)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _authError.value = error.message ?: "Registration failed."
                }
            )
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }

    fun updateProfile(fullName: String, email: String, region: String, constituency: String, language: String, photo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateProfile(fullName, email, region, constituency, language, photo)
            _isLoading.value = false
        }
    }

    fun changePassword(oldPass: String, newPass: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.changePassword(oldPass, newPass)
            onDone(res)
        }
    }

    private fun clearInputs() {
        regFullName.value = ""
        regEmail.value = ""
        regPhone.value = ""
        regRegion.value = ""
        regConstituency.value = ""
        regPassword.value = ""
    }
}

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {

    // Lists
    val categories = repository.categoriesFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val quizzes = repository.quizzesFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val sponsors = repository.sponsorsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val announcements = repository.announcementsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allUsers = repository.allUsersFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val auditLogs = repository.auditLogsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val attemptsFlow = repository.attemptsFlow

    // Active quiz session states
    private val _activeQuiz = MutableStateFlow<QuizEntity?>(null)
    val activeQuiz = _activeQuiz.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val quizQuestions = _quizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<String, String>>(emptyMap()) // qnId -> Answer ("A", "B", etc)
    val selectedAnswers = _selectedAnswers.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(0)
    val timeLeftSeconds = _timeLeftSeconds.asStateFlow()

    private val _cheatAttemptCount = MutableStateFlow(0)
    val cheatAttemptCount = _cheatAttemptCount.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted = _isQuizCompleted.asStateFlow()

    private val _quizResult = MutableStateFlow<QuizAttemptEntity?>(null)
    val quizResult = _quizResult.asStateFlow()

    private var timerJob: Job? = null
    private var quizStartTimeEpoch = 0L

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    // Set search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Admin CSV import previews
    private val _csvPreviewQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val csvPreviewQuestions = _csvPreviewQuestions.asStateFlow()
    private val _csvImportError = MutableStateFlow<String?>(null)
    val csvImportError = _csvImportError.asStateFlow()

    // 1. Load active quiz
    fun startQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            _activeQuiz.value = quiz
            _selectedAnswers.value = emptyMap()
            _isQuizCompleted.value = false
            _quizResult.value = null
            _cheatAttemptCount.value = 0
            _currentQuestionIndex.value = 0

            val questions = repository.getQuestionsForQuizList(quiz.id)
            _quizQuestions.value = questions
            _timeLeftSeconds.value = quiz.timeLimitMinutes * 60
        }
    }

    fun startTimer() {
        quizStartTimeEpoch = System.currentTimeMillis()
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0 && !_isQuizCompleted.value) {
                delay(1000)
                _timeLeftSeconds.value -= 1
            }
            if (_timeLeftSeconds.value <= 0 && !_isQuizCompleted.value) {
                submitQuiz(autoSubmit = true, userId = repository.currentUserState.value?.id ?: "")
            }
        }
    }

    // Question navigation
    fun selectAnswer(questionId: String, optionCode: String) {
        val current = _selectedAnswers.value.toMutableMap()
        current[questionId] = optionCode
        _selectedAnswers.value = current
        
        // Anti-Cheating: Local save progress
        saveProgressLocally()
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _quizQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    // Anti-cheating: detect app switching (onPause)
    fun detectAppSwitching() {
        _cheatAttemptCount.value += 1
        viewModelScope.launch {
            repository.addAuditLog("CHEAT_ATTEMPT", "App switching detected during active Quiz. Warn: ${_cheatAttemptCount.value}")
        }
    }

    // Anti-cheating: verify timer manipulation
    private fun verifySystemTimeAccuracy(expectedSeconds: Int): Boolean {
        val elapsedActual = (System.currentTimeMillis() - quizStartTimeEpoch) / 1000
        val discrepancy = Math.abs(elapsedActual - expectedSeconds)
        return discrepancy < 15 // Under 15-second discrepancy is normal
    }

    // Local save/resume progress
    private fun saveProgressLocally() {
        // Simulates saving draft state offline
    }

    fun submitQuiz(autoSubmit: Boolean, userId: String, onError: (String) -> Unit = {}) {
        timerJob?.cancel()
        if (_isQuizCompleted.value) return
        _isQuizCompleted.value = true

        viewModelScope.launch {
            val questions = _quizQuestions.value
            val selected = _selectedAnswers.value

            var correctCount = 0
            questions.forEach { qn ->
                if (selected[qn.id] == qn.correctAnswer) {
                    correctCount++
                }
            }

            val totalTimeSeconds = (_activeQuiz.value?.timeLimitMinutes ?: 0) * 60
            val timeUsedSeconds = totalTimeSeconds - _timeLeftSeconds.value

            // Anti-cheating check
            if (!verifySystemTimeAccuracy(timeUsedSeconds.toInt())) {
                repository.addAuditLog("SECURITY_ALERT", "Timer manipulation detected on user: $userId.")
            }

            val res = repository.submitQuizAttempt(
                userId = userId,
                quizId = _activeQuiz.value?.id ?: "",
                score = correctCount,
                timeUsedSeconds = timeUsedSeconds.toLong()
            )

            res.fold(
                onSuccess = { attempt ->
                    _quizResult.value = attempt
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed submitting attempt")
                }
            )
        }
    }

    // Leaderboard flow for specific quiz or category
    fun fetchLeaderboard(quizId: String?, categoryId: String?, period: String) =
        repository.getLeaderboard(quizId, categoryId, period)

    // Admin activities
    fun createQuiz(
        title: String,
        desc: String,
        imageUrl: String,
        sponsor: String,
        sponsorLogo: String,
        categoryId: String,
        accessCode: String,
        timeLimit: Int,
        totalQuests: Int,
        sponsorId: String = "",
        maxAttempts: Int = 1
    ) {
        viewModelScope.launch {
            val quiz = QuizEntity(
                id = "quiz_${UUID.randomUUID()}",
                categoryId = categoryId,
                title = title,
                description = desc,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?q=80&w=400" },
                sponsorName = sponsor,
                sponsorLogo = sponsorLogo.ifEmpty { "https://images.unsplash.com/photo-1560179707-f14e90ef3623?q=80&w=100" },
                accessCode = accessCode,
                timeLimitMinutes = timeLimit,
                startDate = "2026-06-01",
                endDate = "2026-12-31",
                totalQuestions = totalQuests,
                createdBy = "admin",
                sponsorId = sponsorId,
                maxAttempts = maxAttempts
            )
            repository.createQuiz(quiz)
        }
    }

    fun deleteQuiz(quizId: String) {
        viewModelScope.launch {
            repository.deleteQuiz(quizId)
        }
    }

    fun toggleQuizActive(quizId: String) {
        viewModelScope.launch {
            repository.toggleQuizActive(quizId)
        }
    }

    fun parseAndPreviewCSV(csvContent: String) {
        _csvImportError.value = null
        try {
            val lines = csvContent.lines().filter { it.isNotBlank() }
            if (lines.size <= 1) {
                _csvImportError.value = "CSV must contain header row and at least one question."
                return
            }

            val parsedQuestions = mutableListOf<QuestionEntity>()
            // Expecting: Question,OptionA,OptionB,OptionC,OptionD,Answer,Explanation
            val header = lines[0].split(",")
            if (header.size < 6) {
                _csvImportError.value = "Invalid CSV format. Missing columns. Expected format: Question,OptionA,OptionB,OptionC,OptionD,Answer,Explanation"
                return
            }

            for (i in 1 until lines.size) {
                val line = lines[i]
                // Parse CSV matching option strings containing potential commas inside quotes helper
                val cols = parseCSVLine(line)
                if (cols.size < 6) {
                    _csvImportError.value = "Row $i does not have enough columns (minimum 6)."
                    return
                }

                val questText = cols[0]
                val optA = cols[1]
                val optB = cols[2]
                val optC = cols[3]
                val optD = cols[4]
                val answer = cols[5].uppercase().trim()
                val explanation = if (cols.size > 6) cols[6] else "No explanation available."

                if (answer !in listOf("A", "B", "C", "D")) {
                    _csvImportError.value = "Row $i has invalid answer: '$answer'. Must be A, B, C, or D."
                    return
                }

                val qnEntity = QuestionEntity(
                    id = "qn_imported_${UUID.randomUUID()}",
                    quizId = "", // To be linked on save
                    questionText = questText,
                    optionA = optA,
                    optionB = optB,
                    optionC = optC,
                    optionD = optD,
                    correctAnswer = answer,
                    explanation = explanation,
                    imageUrl = ""
                )
                parsedQuestions.add(qnEntity)
            }

            _csvPreviewQuestions.value = parsedQuestions
        } catch (e: Exception) {
            _csvImportError.value = "Parsing error: ${e.message}"
        }
    }

    fun saveImportedQuestions(quizId: String) {
        val list = _csvPreviewQuestions.value
        if (list.isNotEmpty()) {
            viewModelScope.launch {
                val prepared = list.map { it.copy(quizId = quizId) }
                repository.insertQuestions(prepared)
                _csvPreviewQuestions.value = emptyList()
                repository.addAuditLog("CSV_IMPORT", "Imported ${prepared.size} questions for Quiz: $quizId")
            }
        }
    }

    fun clearCSVPreview() {
        _csvPreviewQuestions.value = emptyList()
        _csvImportError.value = null
    }

    fun addManualQuestion(
        quizId: String,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: String,
        explanation: String,
        imageUrl: String = ""
    ) {
        viewModelScope.launch {
            val q = QuestionEntity(
                id = java.util.UUID.randomUUID().toString(),
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
            repository.insertQuestion(q)
            repository.addAuditLog("MANUAL_QN_ADD", "Added question: '${if (questionText.length > 30) questionText.take(27) + "..." else questionText}' to Quiz ID: $quizId")
        }
    }

    fun deleteManualQuestion(id: String, quizId: String) {
        viewModelScope.launch {
            repository.deleteQuestion(id)
            repository.addAuditLog("MANUAL_QN_DEL", "Deleted question ID $id from Quiz ID: $quizId")
        }
    }

    fun getQuestionsForQuiz(quizId: String): kotlinx.coroutines.flow.Flow<List<QuestionEntity>> {
        return repository.getQuestionsForQuiz(quizId)
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        for (i in 0 until line.length) {
            val ch = line[i]
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(cur.toString().trim())
                cur = StringBuilder()
            } else {
                cur.append(ch)
            }
        }
        result.add(cur.toString().trim())
        return result
    }

    // Sponsor actions
    fun addSponsor(name: String, description: String, logoUrl: String) {
        viewModelScope.launch {
            repository.createSponsor(
                SponsorEntity(
                    id = "sp_${UUID.randomUUID()}",
                    name = name,
                    logoUrl = logoUrl.ifEmpty { "https://images.unsplash.com/photo-1560179707-f14e90ef3623?q=80&w=100" },
                    description = description
                )
            )
        }
    }

    fun deleteSponsor(id: String) {
        viewModelScope.launch {
            repository.deleteSponsor(id)
        }
    }

    // Announcement actions
    fun createAnnouncement(
        title: String,
        content: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        linkLabel: String? = null
    ) {
        viewModelScope.launch {
            repository.createAnnouncement(title, content, imageUrl, linkUrl, linkLabel)
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
        }
    }

    fun toggleAnnouncementActive(id: String) {
        viewModelScope.launch {
            repository.toggleAnnouncementActive(id)
        }
    }

    // Category actions
    fun addCategory(name: String, description: String, imageUrl: String) {
        viewModelScope.launch {
            repository.createCategory(
                CategoryEntity(
                    id = "cat_${UUID.randomUUID()}",
                    categoryName = name,
                    description = description,
                    categoryImage = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?q=80&w=260" }
                )
            )
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun clearLeaderboardRecords(quizId: String?, categoryId: String?) {
        viewModelScope.launch {
            repository.clearLeaderboard(quizId, categoryId)
            val desc = when {
                quizId != null -> "Leaderboard cleared for Quiz ID: $quizId"
                categoryId != null -> "Leaderboard cleared for Category ID: $categoryId"
                else -> "All global leaderboard historical records purged"
            }
            repository.addAuditLog("LEADERBOARD_PURGE", desc)
        }
    }

    fun deleteLeaderboardEntry(id: String) {
        viewModelScope.launch {
            repository.deleteLeaderboardEntry(id)
        }
    }

    fun updateUserStatus(userId: String, status: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, status)
        }
    }

    fun updateUserRole(userId: String, role: String) {
        viewModelScope.launch {
            repository.updateUserRole(userId, role)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun deleteUsers(userIds: List<String>) {
        viewModelScope.launch {
            userIds.forEach { userId ->
                repository.deleteUser(userId)
            }
        }
    }

    suspend fun getAttemptsForQuizAndUser(quizId: String, userId: String): List<QuizAttemptEntity> {
        return repository.getAttemptsByQuizAndUser(quizId, userId)
    }
}
