package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        FocusRecordEntity::class,
        ClassScheduleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract val studyDao: StudyDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_flow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
