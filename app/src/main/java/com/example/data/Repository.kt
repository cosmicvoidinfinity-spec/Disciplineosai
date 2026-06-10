package com.example.data

import android.util.Log
import com.google.android.gms.tasks.Task as GmsTask
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Await extension for Firebase Tasks to integrate nicely with suspended coroutines
private suspend fun <T> GmsTask<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed"))
        }
    }
}

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
        val isChecking = !dates.contains(today)
        
        val updated = if (!isChecking) {
            // Uncheck
            dates.remove(today)
            val newStreak = calculateStreak(dates)
            val updatedHabit = habit.copy(
                completedDates = dates.joinToString(";"),
                currentStreak = newStreak
            )
            dao.insertHabit(updatedHabit)
            addXpAndScore(-20, -2)
            updatedHabit
        } else {
            // Check
            dates.add(today)
            val newStreak = calculateStreak(dates)
            val updatedHabit = habit.copy(
                completedDates = dates.joinToString(";"),
                currentStreak = newStreak
            )
            dao.insertHabit(updatedHabit)
            addXpAndScore(40, 5)
            updateChallengeByIncrement("Complete 3 Habits")
            updatedHabit
        }
        syncSingleHabitToFirestore(updated)
    }

    suspend fun createHabit(name: String, difficulty: String) {
        val newHabit = Habit(name = name, difficulty = difficulty)
        dao.insertHabit(newHabit)
        addXpAndScore(15, 0)
        
        // Retrieve newly created/flowed items and push to Firestore
        val list = dao.getAllHabits().firstOrNull() ?: emptyList()
        val match = list.firstOrNull { it.name == name && it.difficulty == difficulty }
        if (match != null) {
            syncSingleHabitToFirestore(match)
        } else {
            syncAllHabitsToFirestore()
        }
    }

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit)
        val firestore = getSafeFirestore() ?: return
        try {
            firestore.collection("users")
                .document("default_user")
                .collection("habits")
                .document(habit.id.toString())
                .delete()
                .await()
            Log.d("FirestoreSync", "Habit successfully deleted from Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting habit from Firestore: ${e.message}")
        }
    }

    suspend fun checkAndPerformDailyReset(force: Boolean = false) {
        val currentProgress = dao.getUserProgress().firstOrNull() ?: return
        val lastActiveMillis = currentProgress.lastActive
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastActiveDate = sdf.format(Date(lastActiveMillis))
        val todayDate = sdf.format(Date())
        
        if (force || lastActiveDate != todayDate) {
            // Yes, standard daily reset transitions water and focus seconds back to zero
            val updatedProgress = currentProgress.copy(
                waterIntake = 0,
                focusSecondsToday = 0,
                lastActive = System.currentTimeMillis()
            )
            dao.insertUserProgress(updatedProgress)
            
            val habitsList = dao.getAllHabits().firstOrNull() ?: emptyList()
            for (h in habitsList) {
                val dates = h.completedDates.split(";").filter { it.isNotEmpty() }
                val newStreak = calculateStreak(dates)
                if (h.currentStreak != newStreak) {
                    val updatedh = h.copy(currentStreak = newStreak)
                    dao.insertHabit(updatedh)
                }
            }
            
            // Push reset state to Firestore!
            syncAllHabitsToFirestore()
        }
    }

    // --- Firestore Synchronisation Domain & Utility Layer ---

    private fun getSafeFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            Log.d("FirestoreSync", "FirebaseApp not configured. Synchronising locally only.")
            null
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Firebase not available: ${e.message}")
            null
        }
    }

    suspend fun syncAllHabitsToFirestore() {
        val firestore = getSafeFirestore() ?: return
        try {
            val habitsList = dao.getAllHabits().firstOrNull() ?: emptyList()
            for (h in habitsList) {
                val data = mapOf(
                    "id" to h.id,
                    "name" to h.name,
                    "difficulty" to h.difficulty,
                    "currentStreak" to h.currentStreak,
                    "completedDates" to h.completedDates
                )
                firestore.collection("users")
                    .document("default_user")
                    .collection("habits")
                    .document(h.id.toString())
                    .set(data)
                    .await()
            }
            Log.d("FirestoreSync", "Bulk synchronised ${habitsList.size} habits to Firestore successfully.")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Bulk Firestore push failed: ${e.message}")
        }
    }

    suspend fun syncSingleHabitToFirestore(h: Habit) {
        val firestore = getSafeFirestore() ?: return
        try {
            val data = mapOf(
                "id" to h.id,
                "name" to h.name,
                "difficulty" to h.difficulty,
                "currentStreak" to h.currentStreak,
                "completedDates" to h.completedDates
            )
            firestore.collection("users")
                .document("default_user")
                .collection("habits")
                .document(h.id.toString())
                .set(data)
                .await()
            Log.d("FirestoreSync", "Saved single habit to Firestore: ${h.name}")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error saving single habit to Firestore: ${e.message}")
        }
    }

    suspend fun pullHabitsFromFirestore(): Boolean {
        val firestore = getSafeFirestore() ?: return false
        try {
            val snapshot = firestore.collection("users")
                .document("default_user")
                .collection("habits")
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                val idLong = doc.getLong("id") ?: continue
                val id = idLong.toInt()
                val name = doc.getString("name") ?: ""
                val difficulty = doc.getString("difficulty") ?: "Medium"
                val currentStreakLong = doc.getLong("currentStreak") ?: 0L
                val currentStreak = currentStreakLong.toInt()
                val completedDates = doc.getString("completedDates") ?: ""
                
                val pulledHabit = Habit(
                    id = id,
                    name = name,
                    difficulty = difficulty,
                    currentStreak = currentStreak,
                    completedDates = completedDates
                )
                dao.insertHabit(pulledHabit)
            }
            Log.d("FirestoreSync", "Pulled habits from Firestore successfully.")
            return true
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Firestore pull failed: ${e.message}")
            return false
        }
    }

    fun calculateStreak(completedDates: List<String>): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val distinctSorted = completedDates
            .distinct()
            .mapNotNull { try { sdf.parse(it) } catch(e: Exception) { null } }
            .sortedDescending() // newest execution dates first
        
        if (distinctSorted.isEmpty()) return 0
        
        val today = sdf.parse(sdf.format(Date())) ?: return 0
        
        // Most recent completion day offset relative to actual today date
        val first = distinctSorted.first()
        val diffToday = (today.time - first.time) / (1000 * 60 * 60 * 24)
        
        if (diffToday > 1) {
            // Gap since most recent execution exceeds active day threshold
            return 0
        }
        
        var currentStreak = 0
        var expectedTime = first.time
        
        for (date in distinctSorted) {
            val diff = (expectedTime - date.time) / (1000 * 60 * 60 * 24)
            if (diff == 0L) {
                currentStreak++
                // Expect consecutive previous calendar date sequence
                val cal = java.util.Calendar.getInstance()
                cal.time = Date(expectedTime)
                cal.add(java.util.Calendar.DATE, -1)
                expectedTime = cal.timeInMillis
            } else if (diff > 0L) {
                // Non-sequential gap detected, streak segment complete
                break
            }
        }
        return currentStreak
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
