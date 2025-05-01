package com.example.prog7313_groupwork

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.User
import com.example.prog7313_groupwork.entities.UserDAO
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserDAOTest {
    private lateinit var database: AstraDatabase
    private lateinit var dao: UserDAO

    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AstraDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.userDAO()
    }

    @After
    fun teardown(){
        database.close()
    }

    @Test
    fun insertUser() = runBlocking {
        val user = User(
            NameSurname = "Christiano Ronaldo",
            PhoneNumber = 648593746,
            userEmail = "ron@gmail.com",
            passwordHash = "bloop123"
        )
        dao.insertUser(user)
        val confirmUser = dao.getUserByEmail("ron@gmail.com")
        assertThat(confirmUser?.NameSurname).isEqualTo("Christiano Ronaldo")
    }

    @Test
    fun getLatestUser() = runBlocking {
        // Insert first user
        val user1 = User(
            NameSurname = "User One",
            PhoneNumber = 123456789,
            userEmail = "user1@test.com",
            passwordHash = "pass1"
        )
        dao.insertUser(user1)

        // Insert second user
        val user2 = User(
            NameSurname = "User Two",
            PhoneNumber = 987654321,
            userEmail = "user2@test.com",
            passwordHash = "pass2"
        )
        dao.insertUser(user2)

        val latestUser = dao.getLatestUser()
        assertThat(latestUser?.userEmail).isEqualTo("user2@test.com")
    }

    @Test
    fun getUserById() = runBlocking {
        val user = User(
            NameSurname = "Test User",
            PhoneNumber = 123456789,
            userEmail = "test@test.com",
            passwordHash = "testpass"
        )
        dao.insertUser(user)
        
        val insertedUser = dao.getUserByEmail("test@test.com")
        val retrievedUser = dao.getUserById(insertedUser!!.id)
        
        assertThat(retrievedUser?.NameSurname).isEqualTo("Test User")
    }

    @Test
    fun updateUserCredentials() = runBlocking {
        // Insert initial user
        val user = User(
            NameSurname = "Original User",
            PhoneNumber = 123456789,
            userEmail = "original@test.com",
            passwordHash = "originalpass"
        )
        dao.insertUser(user)

        // Get user ID
        val insertedUser = dao.getUserByEmail("original@test.com")
        
        // Update credentials
        dao.updateUserCredentials(insertedUser!!.id, "new@test.com", "newpass")
        
        // Verify update
        val updatedUser = dao.getUserById(insertedUser.id)
        assertThat(updatedUser?.userEmail).isEqualTo("new@test.com")
        assertThat(updatedUser?.passwordHash).isEqualTo("newpass")
    }

    @Test
    fun updateUserLanguage() = runBlocking {
        val user = User(
            NameSurname = "Test User",
            PhoneNumber = 123456789,
            userEmail = "test@test.com",
            passwordHash = "testpass"
        )
        dao.insertUser(user)
        
        val insertedUser = dao.getUserByEmail("test@test.com")
        dao.updateUserLanguage(insertedUser!!.id, "fr")
        
        val updatedUser = dao.getUserById(insertedUser.id)
        assertThat(updatedUser?.language).isEqualTo("fr")
    }

    @Test
    fun updateUserCurrency() = runBlocking {
        val user = User(
            NameSurname = "Test User",
            PhoneNumber = 123456789,
            userEmail = "test@test.com",
            passwordHash = "testpass"
        )
        dao.insertUser(user)
        
        val insertedUser = dao.getUserByEmail("test@test.com")
        dao.updateUserCurrency(insertedUser!!.id, "EUR")
        
        val updatedUser = dao.getUserById(insertedUser.id)
        assertThat(updatedUser?.currency).isEqualTo("EUR")
    }

    @Test
    fun updateUserThemeColor() = runBlocking {
        val user = User(
            NameSurname = "Test User",
            PhoneNumber = 123456789,
            userEmail = "test@test.com",
            passwordHash = "testpass"
        )
        dao.insertUser(user)
        
        val insertedUser = dao.getUserByEmail("test@test.com")
        dao.updateUserThemeColor(insertedUser!!.id, 1)
        
        val updatedUser = dao.getUserById(insertedUser.id)
        assertThat(updatedUser?.themeColor).isEqualTo(1)
    }

    @Test
    fun deleteUser() = runBlocking {
        val user = User(
            NameSurname = "Test User",
            PhoneNumber = 123456789,
            userEmail = "test@test.com",
            passwordHash = "testpass"
        )
        dao.insertUser(user)
        
        val insertedUser = dao.getUserByEmail("test@test.com")
        dao.deleteUserById(insertedUser!!.id)
        
        val deletedUser = dao.getUserById(insertedUser.id)
        assertThat(deletedUser).isNull()
    }
}