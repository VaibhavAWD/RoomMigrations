package practice.roommigrations.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "contacts")
data class Contact(
    var name: String,
    var mobile: String,
    @PrimaryKey
    @ColumnInfo(name = "entryId")
    val id: String = UUID.randomUUID().toString()
)