package com.jaycefr.stepshadower.step

import android.os.Build
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

    val steps = MutableStateFlow(0)

    init {
        viewModelScope.launch{
            while (true){
                refresh()
                delay(5_000)
            }
        }
    }

    private suspend fun refresh(){
        steps.value = runCatching { repo.todaySteps() }.getOrDefault(0)
    }

}