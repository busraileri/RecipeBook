package com.busraileri.recipebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class ListRecyclerAdapter (val foodList: ArrayList<String>, val idList: ArrayList<Int>): RecyclerView.Adapter<ListRecyclerAdapter.FoodHolder>(){

    class FoodHolder (itemView: View) : RecyclerView.ViewHolder(itemView){

        val recyclerRowText: TextView = itemView.findViewById(R.id.recycler_row_text)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row, parent, false)
        return FoodHolder(view)

    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.recyclerRowText.text = foodList[position]
        holder.itemView.setOnClickListener{
            val action = ListFragmentDirections.actionListFragmentToRecipeFragment("fromrecycler", idList[position])
            Navigation.findNavController(it).navigate(action)
        }
    }
}