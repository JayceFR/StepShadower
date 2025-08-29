package com.jaycefr.stepshadower.user

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class UserViewModel (
    private val context : Context
) : ViewModel() {

    val email = MutableStateFlow("")
    val numberOfAttempts = MutableStateFlow(3) // Default value set to 3
    val toOnboard = MutableStateFlow<Boolean?>(null)


    init {
        refresh()
    }

    fun refresh(){
        val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
        email.value = prefs.getString("email", "").toString()
        numberOfAttempts.value = prefs.getInt("numberOfFailedAttempts", 3)
        toOnboard.value = prefs.getBoolean("onboard", true)
    }

    fun insertUser(email : String, numberOfFailedAttempts : Int){

        val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
        prefs.edit() {
            putString("email", email)
            putInt("numberOfFailedAttempts", numberOfFailedAttempts)
            putBoolean("onboard", false)
            putBoolean("activated", true)
            apply()
        }

        this.email.value = email
        this.numberOfAttempts.value = numberOfFailedAttempts
        this.toOnboard.value = false

    }

    fun updateEmail(email : String){
        val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
        prefs.edit(){
            putString("email", email)
            apply()
        }
        this.email.value = email
    }

    fun updateNumberOfFailedAttempts(numberOfFailedAttempts : Int){
        val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
        prefs.edit(){
            putInt("numberOfFailedAttempts", numberOfFailedAttempts)
            apply()
        }
        this.numberOfAttempts.value = numberOfFailedAttempts
    }

}