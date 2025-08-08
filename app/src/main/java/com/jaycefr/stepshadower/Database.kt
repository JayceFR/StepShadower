package com.jaycefr.stepshadower

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "numberOfFailedAttempts") val numberOfFailedAttempts : Int
)

@Dao
interface UserDAO{
    @Query("SELECT uid FROM user")
    suspend fun getUsers() : List<Int>

    @Query("SELECT email from user where uid = 0")
    suspend fun getEmail() : String?

    @Query("SELECT numberOfFailedAttempts from user where uid = 0")
    suspend fun getNumberOfFailedAttempts() : Int?

    @Query("UPDATE user SET numberOfFailedAttempts = :numberOfFailedAttempts WHERE uid = 0")
    suspend fun updateNumberOfFailedAttempts(numberOfFailedAttempts : Int)

    @Query("UPDATE user SET email = :email WHERE uid = 0")
    suspend fun updateEmail(email : String)

    @Query("INSERT INTO user (uid, email, numberOfFailedAttempts) VALUES (0, :email, :numberOfFailedAttempts)")
    suspend fun insertUser(email : String, numberOfFailedAttempts : Int)
}

@Database(entities = [User::class], version = 1)
abstract class FortifyDatabase : RoomDatabase(){
    abstract fun dao() : UserDAO
}