package com.jaycefr.stepshadower

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class UserViewModel (
    private val repo: UserRepo
) : ViewModel() {

    val email = MutableStateFlow("")
    val numberOfAttempts = MutableStateFlow(3) // Default value set to 3
    val toOnboard = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            toOnboard.value = repo.onboard()
            email.value = runCatching { repo.getEmail() }.getOrDefault("").toString()
            numberOfAttempts.value = runCatching { repo.getNumberOfFailedAttempts() }.getOrDefault(3)!!
        }
    }

    fun updateEmail(email : String){
        this.email.value = email
        viewModelScope.launch {
            repo.updateEmail(email)
        }
    }

    fun updateNumberOfFailedAttempts(numberOfFailedAttempts : Int){
        this.numberOfAttempts.value = numberOfFailedAttempts
        viewModelScope.launch {
            repo.updateNumberOfFailedAttempts(numberOfFailedAttempts)
        }
    }

}