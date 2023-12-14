package com.example.wropoznienia

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VehiclesFilterRecycler : AppCompatActivity() {

    private lateinit var filterBusButton: Button
    private lateinit var filterTramButton: Button

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicles_filter_recycler)

        val vehicleLines = listOf<String>("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17",
            "18", "19", "20","21","22","23","31","32","33","70","72","74","A","D","K","N","100","101","102","103","104",
            "105","106","107","108","109","110","111","112","113","114","115","116","117","118","119","120","121","122",
            "123","124","125","126","127","128","129","130","131","132","133","134","136","137","138","142","143","144",
            "145","146","147","148","149","150","151","206","240","241","242","243","244","245","246","247","248","249",
            "250","251","253","255","257","259","315","319","345","602","607","612","714","733","903","904","907","908",
            "909","911","913","914","917","920","921","923","924","927","930","931","903","933","934","936","937","938",
            "940","941","947","948","958","967")

        // getting the recyclerview by its id
        val searchView = findViewById<SearchView>(R.id.searchView)
        val recyclerview = findViewById<RecyclerView>(R.id.recycler_filter)
        filterBusButton = findViewById(R.id.filterBusButton)
        filterTramButton = findViewById(R.id.filterTramButton)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        for (i in vehicleLines) {
            data.add(ItemsViewModel(R.drawable.mymarkerbus, "Linia $i", i))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })


        adapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, lineNumber: String) {
                Toast.makeText(
                    this@VehiclesFilterRecycler,
                    "Wybrano pojazdy linii $lineNumber",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@VehiclesFilterRecycler, MainActivity::class.java)
                intent.putExtra("numer linii", lineNumber)
                startActivity(intent)
                finish()
            }
        })

        filterBusButton.setOnClickListener {
            adapter.filterBusLines()
        }

        filterTramButton.setOnClickListener {
            adapter.filterTramLines()
        }

    }

}