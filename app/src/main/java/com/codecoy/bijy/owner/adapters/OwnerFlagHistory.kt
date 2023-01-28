package com.codecoy.bijy.owner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecoy.bijy.databinding.ItemFlagHistoryBinding
import com.codecoy.bijy.owner.callBacks.OwnerFlagCallBack
import com.codecoy.bijy.owner.models.Flag


class OwnerFlagHistory(
    private val flagList: MutableList<Flag>,
    private val mContext: Context,
    private val callBack: OwnerFlagCallBack,
) : RecyclerView.Adapter<OwnerFlagHistory.HistoryViewHolder>() {

    private lateinit var binding: ItemFlagHistoryBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        binding = ItemFlagHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
       with(holder.binding){
         this.tvAddress.text = flagList.get(position).title

       }
        holder.itemView.setOnClickListener{
            callBack.onFlagItemClick( flagList.get(position))
        }


    }

    fun updateData(flagList: MutableList<Flag>){
        this.flagList.clear()
        this.flagList.addAll(flagList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = flagList.size

    class HistoryViewHolder(val binding: ItemFlagHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

    }


}


