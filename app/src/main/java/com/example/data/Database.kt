package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val score: Int = 50,
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val waterIntake: Int = 0,
    val focusSecondsToday: Int = 0,
    val lastActive: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val difficulty: String = "Medium", // Low, Medium, High
    val currentStreak: Int = 0,
    val completedDates: String = "" // Semicolon separated dates, e.g. "2026-06-10;2026-06-09"
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val priority: String = "Medium", // High, Medium, Low
    val status: String = "Todo", // Todo, InProgress, Done
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val folder: String = "General",
    val aiSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // Push, Pull, Legs, Cardio, Yoga, Other
    val durationMinutes: Int,
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Int,
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val waterMl: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String, // Tactical, Military, Zen
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val requirement: String,
    val xpReward: Int,
    val progressCurrent: Int,
    val progressMax: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "leaderboard_users")
data class LeaderboardUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val score: Int,
    val level: Int,
    val isCurrentUser: Boolean = false
)

// --- Combined DAO Interface ---

@Dao
interface DisciplineDao {
    // User Progress
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)

    @Query("UPDATE user_progress SET waterIntake = :water WHERE id = 1")
    suspend fun updateWaterIntake(water: Int)

    // Habits
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    // Notes
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    // Workouts
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutLog)

    // Food & Nutrition
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(food: FoodLog)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession)

    // Challenges
    @Query("SELECT * FROM challenges ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Query("UPDATE challenges SET progressCurrent = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateChallengeProgress(id: Int, progress: Int, completed: Boolean)

    // Leaderboard
    @Query("SELECT * FROM leaderboard_users ORDER BY score DESC")
    fun getLeaderboard(): Flow<List<LeaderboardUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardUser(user: LeaderboardUser)
}

// --- App Database Definition ---

@Database(
    entities = [
        UserProgress::class,
        Habit::class,
        Task::class,
        Note::class,
        WorkoutLog::class,
        FoodLog::class,
        FocusSession::class,
        Challenge::class,
        LeaderboardUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): DisciplineDao
}
