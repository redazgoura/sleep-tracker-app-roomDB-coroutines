package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.*

class SleepQualityViewModel(
    private val sleepNightKey: Long = 0L,
    val database: SleepDatabaseDao
) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope =  CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToSleepTracker =  MutableLiveData<Boolean?>()

    //To navigate back to the SleepTrackerFragment
    val navigateToSleepTracker: LiveData<Boolean?>
        get() = _navigateToSleepTracker

    fun doneNavigating() {
        _navigateToSleepTracker.value = null
    }

    //click handler for all the smiley sleep quality
    fun onSetSleepQuality(quality: Int) {
        uiScope.launch {
            // withContext(Dispatchers.IO) is used to switch to the IO dispatcher,
            // which is typically used for database operations.
            // This ensures that the database operations are performed off the main thread
            withContext(Dispatchers.IO) {
                // get tonight from DB using the sleepNightKey
                //Check if 'tonight' is null; if it is, return@withContext
                //return@withContext is used To exit the coroutine early and prevent further execution of the code
                val tonight = database.get(sleepNightKey) ?: return@withContext
                tonight.sleepQuality = quality
                database.update(tonight)
            }
            _navigateToSleepTracker.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}