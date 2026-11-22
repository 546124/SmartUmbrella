package com.smartumbrella.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UmbrellaSlotEntity::class, LoanEventEntity::class, SyncQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UmbrellaDatabase : RoomDatabase() {
    abstract fun umbrellaDao(): UmbrellaDao

    companion object {
        @Volatile private var instance: UmbrellaDatabase? = null

        fun getInstance(context: Context): UmbrellaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                UmbrellaDatabase::class.java,
                "smart-umbrella.db"
            ).build().also { instance = it }
        }
    }
}
