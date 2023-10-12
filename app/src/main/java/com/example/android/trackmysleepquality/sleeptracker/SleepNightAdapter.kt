package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding

/**
 * ListAdapter keep track of the list & notify the adapter when the list is updated
 ** reference to clickListener for databinding to know where to actually call on click
 **/

class SleepNightAdapter(val clickListener: SleepNightListener): ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()){

    /**
     * function that creates view Holders
     **/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // viewHolder responsibility to know what layout to inflate by nesting from() inside it
        return ViewHolder.from(parent)
    }

    /**
     * tell RV how to draw an item
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(getItem(position)!!, clickListener)
        //Log.i("SleepQualityItems", "${item.sleepQuality.toString()}")
    }

    /**
     * to hold ref to the view that this ViewHolder will update
     * private constructor : means it can only be called inside the class
     **/
    class ViewHolder private constructor (val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SleepNight,
            clickListener: SleepNightListener
        ) {
            // tell data binding abt the new sleep night
            binding.sleep = item
            binding.executePendingBindings()
            // pass clickListener to binding object
            binding.clickListener = clickListener
        }

        companion object {
             fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                 val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

    }

    class SleepNightDiffCallback :
        DiffUtil.ItemCallback<SleepNight>() {

        override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem.nightId == newItem.nightId
        }

        override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem == newItem
        }
    }

}
class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    fun onClick(night: SleepNight) = clickListener(night.nightId)
}