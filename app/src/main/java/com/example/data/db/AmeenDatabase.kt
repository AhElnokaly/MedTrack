package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        FamilyMember::class,
        Medication::class,
        ScheduleEntry::class,
        DoseLog::class,
        ShoppingListItem::class,
        Prescription::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AmeenDatabase : RoomDatabase() {
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun prescriptionDao(): PrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AmeenDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AmeenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val passphrase = com.example.security.SecurityKeyManager.getOrCreateDatabasePassphrase(context)
                    val openHelperFactory = com.example.security.SecurityKeyManager.createSupportFactory(context, passphrase)
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AmeenDatabase::class.java,
                        "ameen_health_database"
                    )
                    .openHelperFactory(openHelperFactory)
                    .fallbackToDestructiveMigration()
                    .addCallback(AmeenDatabaseCallback(scope))
                    .build()
                    INSTANCE = instance
                    instance
                }
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (_: Exception) {}
                INSTANCE = null
            }
        }

        suspend fun clearAllData() {
            INSTANCE?.clearAllTables()
        }
    }

    private class AmeenDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Empty - database starts clean without pre-seeded dummy data as requested
        }

    }
}
