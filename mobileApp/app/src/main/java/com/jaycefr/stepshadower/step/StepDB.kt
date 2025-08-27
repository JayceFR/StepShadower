package com.jaycefr.stepshadower.step

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

/*
Usage

val db = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java, "database-name"
).build()

val stepDAO = db.stepDao()
now you can use stepDAO to access the database

while inserting data into the database, use the following code
val today = LocalData.now()
val row   = Step(today.toEpochDay().toInt(), steps = 10000)
stepDAO.insertAll(row)

 */

@Entity
data class Step(
    @PrimaryKey val date : Int,
    @ColumnInfo(name = "step") val step : Int
)

@Dao
interface StepDAO{
    @Query("Select * FROM step")
    suspend fun getAll() : List<Step>

    @Query("Select * FROM step WHERE date = :date")
    suspend fun getStepByDate(date : Int) : Step?

    @Query("UPDATE step SET step = :step WHERE date = :date")
    suspend fun updateStep(date : Int, step : Int)

    @Query("INSERT INTO step (date, step) VALUES (:date, :step)")
    suspend fun insertStep(date : Int, step : Int)

    @Update
    suspend fun update(step: Step)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg steps : Step) : List<Long>

    @Delete
    suspend fun delete(step : Step)

}

@Database(entities = [Step::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao() : StepDAO
}