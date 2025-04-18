/*
import com.example.prog7313_groupwork.entities.UserDAO

class AuthRepository(
    val userDao: UserDAO,

    ) {
    suspend fun loginUser(email: String, password: String): Boolean {
        val user = userDao.getUserByEmail(email) ?: return false
        val isPasswordValid = BCrypt.checkpw(password, user.passwordHash)
        if (isPasswordValid) {
            val token = generateSecureToken()


            return true
        }
        return false
    }

    private fun generateSecureToken(): String {
        return UUID.randomUUID().toString() // Replace with JWT or other securetoken
        }
}*/
