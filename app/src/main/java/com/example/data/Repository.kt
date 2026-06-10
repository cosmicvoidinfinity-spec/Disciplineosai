package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DisciplineRepository(private val dao: DisciplineDao) {

    val userProgress: Flow<UserProgress?> = dao.getUserProgress()
    val allHabits: Flow<List<Habit>> = dao.getAllHabits()
    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    val allNotes: Flow<List<Note>> = dao.getAllNotes()
    val allWorkouts: Flow<List<WorkoutLog>> = dao.getAllWorkouts()
    val allFoodLogs: Flow<List<FoodLog>> = dao.getAllFoodLogs()
    val allFocusSessions: Flow<List<FocusSession>> = dao.getAllFocusSessions()
    val allChallenges: Flow<List<Challenge>> = dao.getAllChallenges()
    val leaderboard: Flow<List<LeaderboardUser>> = dao.getLeaderboard()

    suspend fun getOrCreateUserProgress(): UserProgress {
        val current = dao.getUserProgress().firstOrNull() ?: UserProgress()
        if (dao.getUserProgress().firstOrNull() == null) {
            dao.insertUserProgress(current)
            // Seed leaderboard and status
            seedInitialData()
        }
        return current
    }

    suspend fun addXpAndScore(xpGain: Int, scoreGain: Int) {
        val current = dao.getUserProgress().firstOrNull() ?: UserProgress()
        var newXp = current.xp + xpGain
        var newLevel = current.level
        val xpNeeded = 100 * newLevel

        if (newXp >= xpNeeded) {
            newXp -= xpNeeded
            newLevel += 1
        }

        val newScore = (current.score + scoreGain).coerceIn(0, 100)
        
        val updated = current.copy(
            xp = newXp,
            level = newLevel,
            score = newScore,
            lastActive = System.currentTimeMillis()
        )
        dao.insertUserProgress(updated)

        // Sync local current user in leaderboard
        dao.insertLeaderboardUser(
            LeaderboardUser(
                id = 1,
                username = "You (Discipline)",
                score = newScore,
                level = newLevel,
                isCurrentUser = true
            )
        )
    }

    suspend fun logWater(ml: Int) {
        val current = dao.getUserProgress().firstOrNull() ?: UserProgress()
        val updatedWater = current.waterIntake + ml
        dao.insertUserProgress(current.copy(waterIntake = updatedWater))
        // Award modest XP
        addXpAndScore(10, 1)
    }

    suspend fun completeHabit(habit: Habit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dates = habit.completedDates.split(";").filter { it.isNotEmpty() }.toMutableList()
        
        if (dates.contains(today)) {
            // Uncheck
            dates.remove(today)
            val updated = habit.copy(
                completedDates = dates.joinToString(";"),
                currentStreak = (habit.currentStreak - 1).coerceAtLeast(0)
            )
            dao.insertHabit(updated)
            addXpAndScore(-20, -2)
        } else {
            // Check
            dates.add(today)
            val updated = habit.copy(
                completedDates = dates.joinToString(";"),
                currentStreak = habit.currentStreak + 1
            )
            dao.insertHabit(updated)
            addXpAndScore(40, 5)
            updateChallengeByIncrement("Complete 3 Habits")
        }
    }

    suspend fun createHabit(name: String, difficulty: String) {
        dao.insertHabit(Habit(name = name, difficulty = difficulty))
        addXpAndScore(15, 0)
    }

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit)
    }

    suspend fun createTask(title: String, description: String, priority: String, category: String) {
        dao.insertTask(Task(title = title, description = description, priority = priority, category = category))
        addXpAndScore(10, 1)
    }

    suspend fun toggleTaskStatus(task: Task) {
        val newStatus = if (task.status == "Done") "Todo" else "Done"
        val updated = task.copy(status = newStatus)
        dao.insertTask(updated)
        
        if (newStatus == "Done") {
            addXpAndScore(30, 4)
            updateChallengeByIncrement("Finish 5 Tasks")
        } else {
            addXpAndScore(-30, -4)
        }
    }

    suspend fun deleteTask(id: Int) {
        dao.deleteTaskById(id)
    }

    suspend fun createNote(title: String, content: String, folder: String, aiSummary: String = "") {
        dao.insertNote(Note(title = title, content = content, folder = folder, aiSummary = aiSummary))
        addXpAndScore(15, 1)
        updateChallengeByIncrement("Write deep journaling note")
    }

    suspend fun deleteNote(id: Int) {
        dao.deleteNoteById(id)
    }

    suspend fun logWorkout(type: String, duration: Int, calories: Int) {
        dao.insertWorkout(WorkoutLog(type = type, durationMinutes = duration, calories = calories))
        addXpAndScore(50, 7)
        updateChallengeByIncrement("Log 30 minutes fitness workout")
    }

    suspend fun logFood(name: String, calories: Int, mealType: String) {
        dao.insertFoodLog(FoodLog(name = name, calories = calories, mealType = mealType))
        addXpAndScore(20, 2)
        updateChallengeByIncrement("Log daily meals")
    }

    suspend fun saveFocusSession(mode: String, seconds: Int) {
        dao.insertFocusSession(FocusSession(mode = mode, durationSeconds = seconds))
        val xpGain = (seconds / 60) * 2
        addXpAndScore(xpGain, (seconds / 300))
        updateChallengeByIncrement("Accumulate 10m Focus")
    }

    private suspend fun updateChallengeByIncrement(title: String) {
        val all = dao.getAllChallenges().firstOrNull() ?: return
        val target = all.firstOrNull { it.title.lowercase().contains(title.lowercase()) || title.lowercase().contains(it.title.lowercase()) } ?: return
        
        if (!target.isCompleted) {
            val nextProg = (target.progressCurrent + 1).coerceAtMost(target.progressMax)
            val complete = nextProg >= target.progressMax
            dao.updateChallengeProgress(target.id, nextProg, complete)
            if (complete) {
                // Large Reward
                addXpAndScore(target.xpReward, 15)
            }
        }
    }

    suspend fun seedInitialData() {
        // Core Leaderboard
        val seededLeaderboard = listOf(
            LeaderboardUser(username = "David Goggins (AI)", score = 98, level = 42),
            LeaderboardUser(username = "Jocko Willink (AI)", score = 96, level = 38),
            LeaderboardUser(username = "Andrew Huberman (AI)", score = 89, level = 27),
            LeaderboardUser(username = "Lex Fridman (AI)", score = 82, level = 22),
            LeaderboardUser(username = "Elon Musk (AI)", score = 78, level = 19)
        )
        for (user in seededLeaderboard) {
            dao.insertLeaderboardUser(user)
        }

        // Active Challenges
        val challenges = listOf(
            Challenge(title = "Complete 3 Habits", requirement = "Finish your primary scheduled daily habit routines", xpReward = 150, progressCurrent = 0, progressMax = 3),
            Challenge(title = "Finish 5 Tasks", requirement = "Process prioritised tasks in your kanban queue", xpReward = 200, progressCurrent = 0, progressMax = 5),
            Challenge(title = "Accumulate 10m Focus", requirement = "Log intervals utilizing high-intensity focus modes", xpReward = 250, progressCurrent = 0, progressMax = 1),
            Challenge(title = "Log daily meals", requirement = "Input nutritional details to remain on track", xpReward = 100, progressCurrent = 0, progressMax = 1),
            Challenge(title = "Log 30 minutes fitness workout", requirement = "Perform highly strenuous level exercise", xpReward = 300, progressCurrent = 0, progressMax = 1)
        )
        for (ch in challenges) {
            dao.insertChallenge(ch)
        }

        // Default profile values
        dao.insertUserProgress(UserProgress(score = 65, level = 1, xp = 10, streak = 2))

        // Save local in leaderboard too
        dao.insertLeaderboardUser(
            LeaderboardUser(
                id = 1,
                username = "You (Discipline)",
                score = 65,
                level = 1,
                isCurrentUser = true
            )
        )

        // Seed default habits
        dao.insertHabit(Habit(name = "Cold Shower 5 AM", difficulty = "High", currentStreak = 3))
        dao.insertHabit(Habit(name = "100 Pushups & Plank", difficulty = "Medium", currentStreak = 1))
        dao.insertHabit(Habit(name = "Deep Journal Reflection", difficulty = "Low", currentStreak = 5))

        // Seed default tasks
        dao.insertTask(Task(title = "Configure ultimate life routine", priority = "High", category = "Planner"))
        dao.insertTask(Task(title = "Complete tactical run (5km)", priority = "Medium", category = "Fitness"))
    }
}
