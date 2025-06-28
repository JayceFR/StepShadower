package com.jaycefr.stepshadower

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.Q)
class StepViewModel (
    private val repo: StepsRepo
) : ViewModel(){

    val steps = MutableStateFlow<Int>(0);

    init {
        viewModelScope.launch{
            while (true){
                refresh()
                delay(1000);
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun refresh() = viewModelScope.launch {
//        runCatching { repo.todaySteps() }
//            .onSuccess { step -> steps.value = step }
//            .onFailure { steps.value = 0 }
//    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun refresh(){
        steps.value = kotlin.runCatching { repo.todaySteps() }.getOrDefault(0);
    }

}