package com.codecoy.bijy.users.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecoy.bijy.databinding.ItemFlagHistoryBinding
import com.codecoy.bijy.databinding.ItemUserStatusBinding
import com.codecoy.bijy.owner.callBacks.OwnerFlagCallBack
import com.codecoy.bijy.owner.models.Flag

class UserStatuses(
    private val flagList: MutableList<Flag>,
    private val mContext: Context,
) : RecyclerView.Adapter<UserStatuses.HistoryViewHolder>() {

    private lateinit var binding: ItemUserStatusBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        binding = ItemUserStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        with(holder.binding){
            this.tvStatus.text = "${flagList.get(position).title} Voted ${flagList.get(position).flagStatus}"
            this.tvDate.text = flagList.get(position).date
            this.tvTime.text = flagList.get(position).time
        }

    }

    fun updateData(flagList: MutableList<Flag>){
        this.flagList.clear()
        this.flagList.addAll(flagList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = flagList.size

    class HistoryViewHolder(val binding: ItemUserStatusBinding) : RecyclerView.ViewHolder(binding.root) {

    }


}


