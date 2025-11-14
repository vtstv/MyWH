package com.murr.mywh.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.murr.mywh.database.dao.StorageDao
import com.murr.mywh.database.dao.FolderDao
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.database.entities.Folder

@Database(
    entities = [Storage::class, Folder::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storageDao(): StorageDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новые поля в таблицу folders
                database.execSQL("ALTER TABLE folders ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE folders ADD COLUMN parentFolderId INTEGER")
                database.execSQL("ALTER TABLE folders ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")

                // Создаем индекс для parentFolderId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_folders_parentFolderId ON folders(parentFolderId)")

                // Мигрируем данные из products в folders
                database.execSQL("""
                    INSERT INTO folders (name, description, storageId, parentFolderId, isMarked, createdAt, updatedAt)
                    SELECT p.name, p.description, f.storageId, p.folderId, p.isMarked, p.createdAt, p.createdAt
                    FROM products p
                    INNER JOIN folders f ON p.folderId = f.id
                """)

                // Удаляем таблицу products
                database.execSQL("DROP TABLE IF EXISTS products")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "warehouse.db"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
        }
    }
}
