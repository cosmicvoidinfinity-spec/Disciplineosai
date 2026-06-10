package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import com.example.service.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DisciplineViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "discipline_os_database"
    ).fallbackToDestructiveMigration().build()

    private val repository = DisciplineRepository(db.dao())

    // UI States - streams directly from SQLite Database
    val userProgress: StateFlow<UserProgress?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkouts: StateFlow<List<WorkoutLog>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodLogs: StateFlow<List<FoodLog>> = repository.allFoodLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFocusSessions: StateFlow<List<FocusSession>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChallenges: StateFlow<List<Challenge>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardUser>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Coach Chat States
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    private val _selectedCoachIndex = MutableStateFlow(0) // 0 = Sarge, 1 = Business, 2 = Monk, 3 = Friend
    val selectedCoachIndex: StateFlow<Int> = _selectedCoachIndex.asStateFlow()

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    init {
        viewModelScope.launch {
            // Retrieve or trigger Initial Seed
            repository.getOrCreateUserProgress()
            repository.checkAndPerformDailyReset()
            resetCoachMessages()
        }
    }

    val coaches = listOf(
        Coach("STRICT_SERGEANT", "Sergeant Goggins", "STRICT DRILL INSTRUCTOR", "Stay hard. No excuses."),
        Coach("BUSINESS_MASTER", "Mentor Dalio", "STRATEGIC SYSTEMS ADVISOR", "Track execution velocity."),
        Coach("WISE_MONK", "Stoic Zen Master", "MINDFULNESS & FOCUS GUIDE", "Observe and control flow."),
        Coach("SUPPORTIVE_FRIEND", "Coach Sarah", "COMPASSIONATE ACCOUNTABILITY", "Micro-wins count.")
    )

    fun changeCoach(index: Int) {
        _selectedCoachIndex.value = index
        resetCoachMessages()
    }

    fun resetCoachMessages() {
        val coach = coaches[_selectedCoachIndex.value]
        val welcome = when (coach.id) {
            "STRICT_SERGEANT" -> "WHO DO WE HAVE HERE? WAKE UP! GET UP! LEAVE YOUR COMFORT ZONE. WHAT ARE WE CONQUERING TODAY?"
            "BUSINESS_MASTER" -> "Execution metrics are loaded. Please state your priority target and system leverage requirements. We must optimize today's throughput."
            "WISE_MONK" -> "Breathe in. Feel the quiet focus within you. What mental obstacle is hindering your path at this moment?"
            else -> "Hey friend! It is so wonderful to see you today. I hope you're taking care of yourself. Let's tackle today's habits one step at a time, okay?"
        }
        _chatMessages.value = listOf(Pair(welcome, false))
    }

    // --- Action Methods ---

    fun earnXpAndScore(xpGain: Int, scoreGain: Int) {
        viewModelScope.launch {
            repository.addXpAndScore(xpGain, scoreGain)
        }
    }

    fun resetEntireApp() {
        viewModelScope.launch {
            db.clearAllTables()
            repository.seedInitialData()
        }
    }

    fun simulateDayReset() {
        viewModelScope.launch {
            // Setup a previous active status so we transition into a new day's constraints
            val progress = repository.getOrCreateUserProgress()
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DATE, -1)
            val yesterdayMillis = cal.timeInMillis
            
            // Re-insert stating that we were last active yesterday with water logged, so we can prove it clears
            _isCoachTyping.value = true // Show a brief activity indicator
            db.dao().insertUserProgress(progress.copy(
                lastActive = yesterdayMillis,
                waterIntake = 1800,
                focusSecondsToday = 3600
            ))
            
            // Execute the evaluation logic
            repository.checkAndPerformDailyReset(force = true)
            _isCoachTyping.value = false
        }
    }

    fun completeHabit(habit: Habit) {
        viewModelScope.launch {
            repository.completeHabit(habit)
        }
    }

    fun createHabit(name: String, difficulty: String) {
        viewModelScope.launch {
            repository.createHabit(name, difficulty)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun syncHabitsWithFirestore() {
        viewModelScope.launch {
            repository.syncAllHabitsToFirestore()
        }
    }

    fun pullHabitsFromFirestore(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.pullHabitsFromFirestore()
            onResult(res)
        }
    }

    fun isFirestoreConfigured(): Boolean {
        return try {
            com.google.firebase.FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun createTask(title: String, description: String, priority: String, category: String) {
        viewModelScope.launch {
            repository.createTask(title, description, priority, category)
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskStatus(task)
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun createNote(title: String, content: String, folder: String) {
        viewModelScope.launch {
            // Fetch AI summary asynchronously
            val aiSummary = GeminiService.getNoteAISummary(title, content)
            repository.createNote(title, content, folder, aiSummary)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun logWorkout(type: String, duration: Int, calories: Int) {
        viewModelScope.launch {
            repository.logWorkout(type, duration, calories)
        }
    }

    fun logFood(name: String, calories: Int, mealType: String) {
        viewModelScope.launch {
            repository.logFood(name, calories, mealType)
        }
    }

    fun logWater(ml: Int) {
        viewModelScope.launch {
            repository.logWater(ml)
        }
    }

    fun saveFocusSession(mode: String, durationSeconds: Int) {
        viewModelScope.launch {
            repository.saveFocusSession(mode, durationSeconds)
        }
    }

    fun sendMessageToCoach(userMsgString: String) {
        if (userMsgString.isBlank()) return

        // Append user message
        val currentMsgs = _chatMessages.value.toMutableList()
        currentMsgs.add(Pair(userMsgString, true))
        _chatMessages.value = currentMsgs

        _isCoachTyping.value = true

        viewModelScope.launch {
            // Build rich contextual string for coach
            val progress = userProgress.value
            val habitsList = allHabits.value
            val tasksList = allTasks.value

            val completedHabitsCount = habitsList.filter { it.completedDates.contains(getCurrentDateString()) }.size
            val activeTasksTodo = tasksList.filter { it.status != "Done" }.size

            val contextStr = "Score: ${progress?.score}/100, Level: ${progress?.level}, Streak: ${progress?.streak} days. Habits Checked Today: $completedHabitsCount / ${habitsList.size}. Pending Tasks: $activeTasksTodo."

            val currentCoach = coaches[_selectedCoachIndex.value]
            val coachReply = GeminiService.getCoachResponse(userMsgString, currentCoach.id, contextStr)

            val updatedMsgs = _chatMessages.value.toMutableList()
            updatedMsgs.add(Pair(coachReply, false))
            _chatMessages.value = updatedMsgs

            _isCoachTyping.value = false
        }
    }

    private fun getCurrentDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
}

data class Coach(
    val id: String,
    val name: String,
    val role: String,
    val sampleText: String
)
