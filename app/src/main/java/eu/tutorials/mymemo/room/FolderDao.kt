package eu.tutorials.mymemo.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import eu.tutorials.mymemo.model.Folder

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder_table")
    fun getAllFolders(): LiveData<List<Folder>>

    @Query("SELECT name FROM folder_table")
    fun getFolderNames(): LiveData<List<String>>

    @Insert
    suspend fun insert(folder: Folder)

    @Delete
    suspend fun delete(folder: List<Folder>)

    @Query("DELETE FROM folder_table")
    suspend fun deleteAll()
}