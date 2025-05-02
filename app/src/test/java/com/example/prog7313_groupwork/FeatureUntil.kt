package com.example.prog7313_groupwork
import com.example.prog7313_groupwork.entities.RegistrationUtil
import com.google.common.truth.Truth.assertThat

import org.junit.Test

class FeatureUntil {

    @Test
    fun `missingUsername Return False`(){
        var result = RegistrationUtil.validateInput("", "Password1234","Password1234")
        assertThat(result).isFalse()
    }

    @Test
    fun `missingPassword Return False`(){
        var result = RegistrationUtil.validateInput("des@gmail.com", "","Password1234")
        assertThat(result).isFalse()
    }

    @Test
    fun `password and confirmPassword not matching Return False`(){
        var result = RegistrationUtil.validateInput("des@gmail.com", "Password12345","Password1234")
        assertThat(result).isFalse()
    }

    @Test
    fun `Password contains atleast 2 digits Return True`() {
        var result =
            RegistrationUtil.validateInput("des@gmail.com", "Password12345", "Password12345")
        assertThat(result).isTrue()
    }
}