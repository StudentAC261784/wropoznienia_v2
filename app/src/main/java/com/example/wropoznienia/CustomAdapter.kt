package com.example.wropoznienia


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(), Filterable {

    //private lateinit var mListener: OnItemClickListener

    private var originalData: List<ItemsViewModel> = mList
    private var filteredData: List<ItemsViewModel> = mList
    private var itemClickListener: OnItemClickListener? = null
    private var filteredButtonData: List<ItemsViewModel> = filteredData

    interface OnItemClickListener {
        fun onItemClick(position: Int, lineNumber: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view, itemClickListener!!)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = filteredData[position]

        holder.imageView.setImageResource(ItemsViewModel.image)

        holder.textView.text = ItemsViewModel.text

    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    inner class ViewHolder(ItemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onItemClick(position, filteredData[position].lineNumber)
                }
                //listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<ItemsViewModel>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(mList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()
                    for (item in mList) {
                        if (item.lineNumber.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredData = results?.values as List<ItemsViewModel>
                notifyDataSetChanged()
            }
        }
    }

    fun filterBusLines() {
        val filteredLines = originalData.filter { it.lineNumber.toIntOrNull() ?: 0 >= 100 || it.lineNumber.all { it.isLetter() } }
        filteredData = filteredLines
        notifyDataSetChanged()
    }

    fun filterTramLines() {
        val filteredLines = originalData.filter { it.lineNumber.toIntOrNull() != null && it.lineNumber.toInt() < 100 }
        filteredData = filteredLines
        notifyDataSetChanged()
    }
}