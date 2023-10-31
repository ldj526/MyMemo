package eu.tutorials.mymemo

import android.app.Application
import eu.tutorials.mymemo.repository.FolderRepository
import eu.tutorials.mymemo.repository.MemoRepository
import eu.tutorials.mymemo.room.MemoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MemosApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    // by lazy를 사용하여 데이터베이스와 저장소가 애플리케이션 시작 시가 아니라 필요할 때만 생성되도록 합니다.
    val database by lazy { MemoDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { MemoRepository(database.memoDao()) }

    val folderDatabase by lazy { MemoDatabase.getDatabase(this, applicationScope) }
    val folderRepository by lazy { FolderRepository(database.folderDao()) }
}