package eu.tutorials.mymemo.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import eu.tutorials.mymemo.model.Memo
import eu.tutorials.mymemo.room.MemoDao

class MemoRepository(private val memoDao: MemoDao) {

    val memoList: LiveData<List<Memo>> = memoDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(memo: Memo) {
        memoDao.insert(memo)
    }

    suspend fun delete(memo: List<Memo>) {
        memoDao.delete(memo)
    }
}