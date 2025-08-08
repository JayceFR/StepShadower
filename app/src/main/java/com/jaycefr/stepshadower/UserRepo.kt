package com.jaycefr.stepshadower

import android.content.Context
import androidx.room.Room

class UserRepo(private val appContext : Context){
    private val db : FortifyDatabase = Room.databaseBuilder(
        appContext,
        FortifyDatabase::class.java, "fortifyDB"
    ).build()

    private val dao : UserDAO = db.dao()

    suspend fun getEmail() : String? = dao.getEmail()

    suspend fun getNumberOfFailedAttempts() : Int? = dao.getNumberOfFailedAttempts()

    suspend fun updateNumberOfFailedAttempts(numberOfFailedAttempts : Int) = dao.updateNumberOfFailedAttempts(numberOfFailedAttempts)

    suspend fun updateEmail(email : String) = dao.updateEmail(email)

    suspend fun insertUser(email : String, numberOfFailedAttempts : Int) = dao.insertUser(email, numberOfFailedAttempts)

    suspend fun getUsers() : List<Int> = dao.getUsers()

    suspend fun onboard() : Boolean{
        val users : List<Int> = getUsers()
        return users.isEmpty()
    }

}