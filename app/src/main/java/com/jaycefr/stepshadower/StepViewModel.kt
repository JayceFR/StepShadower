package com.jaycefr.stepshadower

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StepViewModel (
    private val repo: StepsRepo
) : ViewModel(){

    val steps = MutableStateFlow<Int>(0);
    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() = viewModelScope.launch {
        runCatching { repo.todaySteps() }
            .onSuccess { step -> steps.value = step }
            .onFailure { steps.value = 0 }
    }

}