package eu.tutorials.mymemo.room

import androidx.lifecycle.LiveData
import androidx.room.*
import eu.tutorials.mymemo.model.Memo

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo_table ORDER BY date DESC")
    fun getAll(): LiveData<List<Memo>>

    @Query("SELECT * FROM memo_table WHERE folderId = :folderId")
    fun getMemosByFolderId(folderId: Int): LiveData<List<Memo>>

    @Insert
    suspend fun insert(memo: Memo)

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: List<Memo>)

    @Query("DELETE FROM memo_table")
    suspend fun deleteAll()
}