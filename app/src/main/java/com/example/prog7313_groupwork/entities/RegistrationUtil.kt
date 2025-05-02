package com.example.prog7313_groupwork.entities

object RegistrationUtil {

    /**
     * Valid email, password containing atleast 2 digits, password matching confirm password
     */
    fun validateInput(userEmail: String, password:String, confirmPassword:String): Boolean{
        if (userEmail == "" || password == "" || confirmPassword == "" ){
            return false
        }

        if (password != confirmPassword){
            return false
        }

        if (password.count{it.isDigit()} < 2){
            return false
        }
    return true}


}