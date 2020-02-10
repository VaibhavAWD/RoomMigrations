package practice.roommigrations.data.source.local

import androidx.room.*
import practice.roommigrations.data.entities.Contact

@Dao
interface ContactsDao {

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM contacts WHERE entryId = :id")
    suspend fun getContactById(id: String): Contact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact): Int

    @Query("DELETE FROM contacts WHERE entryId = :id")
    suspend fun deleteContactById(id: String): Int

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts(): Int

}