package eu.tutorials.mymemo.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import eu.tutorials.mymemo.model.Folder
import eu.tutorials.mymemo.room.FolderDao

class FolderRepository(private val folderDao: FolderDao) {

    val folderList: LiveData<List<Folder>> = folderDao.getAllFolders()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(folder: Folder) {
        folderDao.insert(folder)
    }

    suspend fun delete(folder: List<Folder>) {
        folderDao.delete(folder)
    }
}