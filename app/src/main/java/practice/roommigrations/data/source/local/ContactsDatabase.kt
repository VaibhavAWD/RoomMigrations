package practice.roommigrations.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import practice.roommigrations.data.entities.Contact

@Database(
    entities = [Contact::class],
    version = 1,
    exportSchema = false
)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao

    companion object {
        @Volatile
        var INSTANCE: ContactsDatabase? = null

        fun getInstance(context: Context): ContactsDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                return instance
            }
        }

        private fun buildDatabase(context: Context): ContactsDatabase {
            return Room.databaseBuilder(
                context,
                ContactsDatabase::class.java,
                "contacts.db"
            ).build()
        }
    }

}