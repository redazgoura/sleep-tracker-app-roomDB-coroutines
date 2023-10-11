/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */

// this view model extends AndroidViewModel
// it takes app context as param & makes it available as a property ## AndroidViewModel(application) ##
class SleepTrackerViewModel(

    //view model access to DB through DOA
    val database: SleepDatabaseDao,

    //app context to access res (strings, styles ....)
    application: Application
) : AndroidViewModel(application) {

    //job instance
    private var viewModelJob = Job()

    // defining a scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // var tonight to hold the current night
    private var tonight = MutableLiveData<SleepNight?>()

    // var nights that holds all nights from DB
    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    //navigation event
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
        //public reference type LiveData which allow the fragment to observe it
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

        // after navigating we need to reset the navigation
        fun doneNavigating(){
                _navigateToSleepQuality.value = null
        }


    //Assign each variable to a Transformations that tests it against the value of tonight.
    // Transformations class offers a way to apply transformations to LiveData objects,
    // which are commonly used for observing and updating UI components
    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    // Snack bar Event
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    init {
        initializeTonight()
    }

    // function to assign values to var tonight
    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    // suspend function to get the values from DB
    private suspend fun getTonightFromDatabase(): SleepNight? {

        // return the result from a coroutine that runs in the Dispatchers.IO context
        return withContext(Dispatchers.IO) {
            // the coroutine get tonight from the database
            var night = database.getTonight()

            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }


    // click handler for the Start button
    fun onStartTracking() {
        // to launch a coroutine in uiScope
        uiScope.launch {
            // SleepNight captures the current time as the start time
            val newNight = SleepNight()

            //insert to the DB
            insert(newNight)

            // Set tonight to the new night:
            tonight.value = getTonightFromDatabase()
        }
    }

    // suspend function to insert  values into DB
    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }


    // click handler for the Stop button
    fun onStopTracking() {
        uiScope.launch {
            //return@ syntax is used for specifying which function among several nested ones this statement returns from
            // It first checks if tonight.value is not null,
            // and if it is null, it returns from the coroutine using return@launch.
            // This likely means that if there's no sleep data to update, the function exits early.

            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

                // triggering navigate To SleepQuality
            //setting _navigateToSleepQuality.value to oldNight is tidy
            //(oldNight is non-null only when you can actually set sleep quality which means if you can't set it u can't navigate)
                _navigateToSleepQuality.value = oldNight
        }
    }

    // suspend function to update values on DB
    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }


    // click handler for the Clean button
    fun onClear() {
        uiScope.launch {
            // clear DB table
            clear()

            // clear tonight since it's no longer in the DB
            tonight.value = null

            //to trigger the snack bar msg
            _showSnackbarEvent.value = true
        }
    }

    // suspend function to delete all selected row on DB
    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()

        //to cancel coroutines
        viewModelJob.cancel()
    }
}

