package com.example.blooddonationapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RequestAdapter(
    private val requestList: MutableList<Request>,
    private val onItemClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val requestUserImage: ImageView = itemView.findViewById(R.id.requestUserImage)
        val requestName: TextView = itemView.findViewById(R.id.requestName)
        val requestStatus: TextView = itemView.findViewById(R.id.requestStatus)
        val requestBloodType: TextView = itemView.findViewById(R.id.requestBloodType)
        val requestDonorId: TextView = itemView.findViewById(R.id.DonotId)

        fun bind(request: Request) {
            if(request.status == "fulfilled"){
                requestStatus.setTextColor(itemView.context.getColor(R.color.Green))
            }else{
                requestStatus.setTextColor(itemView.context.getColor(R.color.BloodRed))
            }
            requestUserImage.setImageBitmap(request.imageUrl)
            requestName.text = request.requesterName
            requestStatus.text = "Status: ${request.status}"
            requestBloodType.text = "Blood Type: ${request.bloodType}"

            if (request.donorId != null) {
                requestDonorId.visibility = View.VISIBLE
                requestDonorId.text = "Donor: ${request.donorId}"
            } else {
                requestDonorId.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(request)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return RequestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }
    fun updateData(newRequests: List<Request>) {
        requestList.clear()
        requestList.addAll(newRequests)
        notifyDataSetChanged()
    }
}
