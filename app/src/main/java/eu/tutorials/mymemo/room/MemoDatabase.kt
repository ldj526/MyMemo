package eu.tutorials.mymemo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.model.Memo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Memo::class, Folder::class], version = 2, exportSchema = false)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun folderDao(): FolderDao

    private class MemoDatabaseCallback(private val scope: CoroutineScope) :
        Callback() {  // 앱을 만들 때마다 모든 콘텐츠를 삭제하고 데이터베이스를 다시 채우기 위함
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var memoDao = database.memoDao()
                    var folderDao = database.folderDao()
                    memoDao.deleteAll()
                    folderDao.deleteAll()
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                ).addCallback(MemoDatabaseCallback(scope)).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}