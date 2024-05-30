package com.busraileri.recipebook

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter


class ListFragment : Fragment() {

    var foodNameList = ArrayList<String>()
    var foodIdList = ArrayList<Int>()
    private lateinit var listAdapter: ListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = ListRecyclerAdapter(foodNameList, foodIdList)
        val recyclerView : RecyclerView = requireView().findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listAdapter


        getDataSql()
    }

    fun getDataSql(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)

                val cursor = database.rawQuery("SELECT * FROM foods", null)

                val IndexofFoodName = cursor.getColumnIndex("foodname")
                val IdOfFood = cursor.getColumnIndex("id")

                foodIdList.clear()
                foodNameList.clear()

                while(cursor.moveToNext()){

                  foodNameList.add(cursor.getString(IndexofFoodName))
                  foodIdList.add(cursor.getInt(IdOfFood))
                }

                cursor.close()
            }

        }catch (e: Exception){

        }
    }


}