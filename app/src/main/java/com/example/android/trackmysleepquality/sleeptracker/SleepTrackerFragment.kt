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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        //reference to the app that this fragment is attached to
            // requireNotNull() throws illegal exception if the value is null
        val application = requireNotNull(this.activity).application

        //reference to datasource via reference to DAO
        val datasource = SleepDatabase.getInstance(application).sleepDatabaseDao

        //create of SleepDatabaseFactory
        val viewModelFactory = SleepTrackerViewModelFactory(datasource, application)

        //using SleepDatabaseFactory we can get SleepTrackerViewModel from ViewModelProvider
        val sleepTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        //Assign the "sleepTrackerViewModel" binding variable to the" sleepTrackerViewModel".
        binding.sleepTrackerViewModel = sleepTrackerViewModel

        /**
            attach the adapter to RV
         */
        val adapter = SleepNightAdapter()
        binding.sleepList.adapter = adapter
        sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer{ it?.let {

            //ListAdapter method that  diff the new list against the old one
            adapter.submitList(it)
        }})

        //specify a current activity as the lifecycle owner of the binding
        // this is necessary so that the binding can observe LiveData updates
        binding.lifecycleOwner = this


        //observe navigate to sleep quality to know when to navigate
        sleepTrackerViewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer{night ->
            night?.let{
                this.findNavController().navigate(
                    //night.nightID : id of the current night
                    SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
                sleepTrackerViewModel.doneNavigating()
            }
        })

        sleepTrackerViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {

            if (it == true) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_LONG // How long to display the message.
                ).show()
                sleepTrackerViewModel.doneShowingSnackbar()
            }
        })

        return binding.root
    }
}
