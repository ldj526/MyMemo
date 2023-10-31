package eu.tutorials.mymemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_table")
data class Folder (
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val name: String?,
    val parentInt: Int? = null
)