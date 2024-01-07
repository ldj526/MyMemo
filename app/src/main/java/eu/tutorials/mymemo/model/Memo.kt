package eu.tutorials.mymemo.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "memo_table")   // SQLite 테이블
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(name = "title")  // 테이블의 열 이름
    val title: String?,
    @ColumnInfo(name = "content")  // 테이블의 열 이름
    val content: ByteArray?,
    @ColumnInfo(name = "date")
    val date: Long?,
    @ColumnInfo(name = "check")
    var isChecked: Boolean = false,
    @ColumnInfo(name = "folderId")
    var folderId: Int? = null,
    @ColumnInfo(name = "imagePath")
    val imagePath: String?
) : Parcelable
