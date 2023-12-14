package com.example.wropoznienia

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class Statistics : AppCompatActivity() {

    // on below line we are creating
    // variables for our bar chart
    lateinit var barChart: BarChart

    // on below line we are creating
    // a variable for bar data
    lateinit var barData: BarData

    // on below line we are creating a
    // variable for bar data set
    lateinit var barDataSet: BarDataSet

    // on below line we are creating array list for bar data
    lateinit var barEntriesList: ArrayList<BarEntry>


    private val daysOfWeek = arrayOf("Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Ndz")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // on below line we are initializing
        // our variable with their ids.
        barChart = findViewById(R.id.idBarChart)

        // on below line we are calling get bar
        // chart data to add data to our array list
        getBarChartData()

        // on below line we are initializing our bar data set
        barDataSet = BarDataSet(barEntriesList, "Statystyki opóźnień")

        // on below line we are initializing our bar data
        barData = BarData(barDataSet)

        // on below line we are setting data to our bar chart
        barChart.data = barData

        // on below line we are setting colors for our bar chart text
        barDataSet.valueTextColor = Color.WHITE

        // on below line we are setting color for our bar data set
        barDataSet.setColor(resources.getColor(R.color.purple_200))

        // on below line we are setting text size
        barDataSet.valueTextSize = 16f

        // on below line we are enabling description as false
        barChart.description.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = DayAxisValueFormatter() // Custom label formatter
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setGranularity(1f) // Display 1 value per day
        xAxis.textColor = Color.WHITE

        barChart.invalidate()

    }

    inner class DayAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // Ensure the value is within bounds to avoid IndexOutOfBoundsException
            val index = value.toInt().coerceIn(0, daysOfWeek.size - 1)
            return daysOfWeek[index]
        }
    }

    private fun getBarChartData() {
        barEntriesList = ArrayList()

        // on below line we are adding data
        // to our bar entries list
        for (i in daysOfWeek.indices) {
            barEntriesList.add(BarEntry(i.toFloat(), i + 1.toFloat()))
        }
    }
}