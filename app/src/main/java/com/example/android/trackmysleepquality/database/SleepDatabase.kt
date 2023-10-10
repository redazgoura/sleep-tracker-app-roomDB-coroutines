
package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//entities : contain all the tables
// ### version should up after any changes made on the schema
// # exportSchema : provides a version history of the DB
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class  SleepDatabase : RoomDatabase(){

    //DAO associated w/ the entity
    abstract val sleepDatabaseDao : SleepDatabaseDao

    //allows clients to access the methods for creating/getting the DB  w/o instantiating the class
    companion object {
        // initializing the DB w/ null
        // INSTANCE will help us avoid repeatedly opening the connection to DB
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        //return a ref to SleepDatabase
        fun getInstance(context: Context): SleepDatabase {
            //wrapping our code with "synchronized" means only one thread of execution at a time can enter this block ->the DB gets initialized once
            synchronized(this) {
                var instance = INSTANCE

                //check if there's already a DB
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SleepDatabase::class.java,
                        "sleep_history_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}