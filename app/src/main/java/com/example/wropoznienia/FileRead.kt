package com.example.wropoznienia

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.opencsv.CSVReader
import java.io.*
import kotlin.math.roundToInt

var pastEnteredText = ""

class FileRead {

    fun readCsvFile(
        context: Context,
        file: File,
        vehicleMap: HashMap<String, Marker>,
        stopMap: HashMap<String, Marker>,
        googleMap: GoogleMap,
        enteredText: String,
        callback: (HashMap<String, Marker>) -> Unit
    ) {
        var sumLineDelay = 0
        var vehicleCount = 0
        var vehicleMapCopy = HashMap<String, Marker>()
        vehicleMapCopy.putAll(vehicleMap)
        var csvLines = ""
        try {
            val fileInputStream = FileInputStream(file)
            val reader = CSVReader(InputStreamReader(fileInputStream))
            var nextLine: Array<String>?
            nextLine = reader.readNext()
            var vehicleIdList = mutableListOf<String>()
            while (reader.readNext().also { nextLine = it } != null) {
                // nextLine[] is an array of values from the line
                csvLines = nextLine!!.joinToString(separator = ",")
                val values = csvLines.split(",")
                try {
                    vehicleIdList.add(values[0])
                    vehicleMapCopy = addVehicleToMap(vehicleMapCopy, stopMap, values, googleMap, context, enteredText)
                    if (enteredText != "" && enteredText == values[1]) {
                        sumLineDelay += values[7].toDouble().roundToInt()
                        vehicleCount += 1
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(context, "Different error, maybe can't add rat?", Toast.LENGTH_SHORT).show()
                }
            }
            var avgDelay = 0
            if (enteredText != "") {
                try {
                    avgDelay = (sumLineDelay / vehicleCount).toFloat().roundToInt()
                } catch (e: java.lang.ArithmeticException) {
                    Log.e("Liczba pojazdów", "0 pojazdów tej linii")
                }

                for (vehicleId in vehicleIdList) {
                    if (vehicleMapCopy.containsKey(vehicleId)) {
                        val currentSnippet = vehicleMapCopy[vehicleId]?.snippet
                        val updatedSnippet = "$currentSnippet\n\nŚrednie opóźnienie linii: $avgDelay s"
                        vehicleMapCopy[vehicleId]?.snippet = updatedSnippet
                    }
                }
            }

            var deleteFlag = false
            for ((key, vehicle) in vehicleMapCopy) {
                for (vehicleId in vehicleIdList) {
                    if (key == vehicleId) {
                        deleteFlag = false
                        break
                    } else {
                        deleteFlag = true
                    }
                }
                if (deleteFlag) {
                    vehicleMapCopy[key]?.remove()
                }
            }

            reader.close()
            fileInputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, "The specified file was not found", Toast.LENGTH_SHORT).show()
        }
        // Invoke the callback with the updated map
        callback(vehicleMapCopy)
    }

    private fun addVehicleToMap(vehicleMap: HashMap<String, Marker>, stopMap: HashMap<String, Marker>, values: List<String>, googleMap: GoogleMap, context: Context, enteredText: String): HashMap<String, Marker> {
        val transportMpkPosition = LatLng(values[3].toDouble(), values[4].toDouble())
        var delayMessage = " Opóźnienie: " + values[7].toDouble().roundToInt() + " s"
        if (vehicleMap.containsKey(values[0])) {
            vehicleMap[values[0]]?.position = transportMpkPosition
            vehicleMap[values[0]]?.snippet = "${values[2]}\n${delayMessage}"
            vehicleMap[values[0]]?.tag = values[5] + "&" + values[6] + "&" + values[7]
        } else {
            val markerName: Marker = googleMap.addMarker(
                MarkerOptions()
                    .position(transportMpkPosition)
                    .title("Pojazd - linia ${values[1]}")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkerbus))
                    .snippet("${values[2]}\n${delayMessage}"))!!
            markerName.tag = values[5] + "&" + values[6] + "&" + values[7]
            vehicleMap[values[0]] = markerName
        }
        if (enteredText != "") {
            if (values[1] != enteredText) {
                if (vehicleMap.containsKey(values[0])) {
                    vehicleMap[values[0]]?.isVisible = false
                }
            } else {
                vehicleMap[values[0]]?.isVisible = true
                val stopsId = values[5].split("/")
                if (enteredText != pastEnteredText) {
                    for ((_, stop) in stopMap) {
                        stop.isVisible = false
                    }
                    pastEnteredText = enteredText
                }
                for (stopId in stopsId) {
                    stopMap[stopId]?.isVisible = true
                }
            }
        } else {
            vehicleMap[values[0]]?.isVisible = true
            pastEnteredText = enteredText
        }
        return vehicleMap
    }

    fun readTxtFile(
        context: Context,
        file: File,
        stopMap: HashMap<String, Marker>,
        googleMap: GoogleMap,
        enteredText: String,
        callback: (HashMap<String, Marker>) -> Unit
    ) {
        var stopMapCopy = HashMap<String, Marker>()
        stopMapCopy.putAll(stopMap)
        try {
            val fileInputStream = FileInputStream(file)
            val reader = BufferedReader(InputStreamReader(fileInputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                try {
                    stopMapCopy = addStopToMap(stopMapCopy, line!!, googleMap, context, enteredText)
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(context, "Different error, maybe can't add cheese?", Toast.LENGTH_SHORT).show()
                }
            }
            reader.close()
            fileInputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(context, "The specified file was not found", Toast.LENGTH_SHORT).show()
        }
        // Invoke the callback with the updated map
        callback(stopMapCopy)
    }

    private fun addStopToMap(stopMap: HashMap<String, Marker>, mpkLine: String, googleMap: GoogleMap, context: Context, enteredText: String): HashMap<String, Marker> {
        val values = mpkLine.split(",")
        val stopPosition = LatLng(values[3].toDouble(), values[4].toDouble())
        val markerName: Marker = googleMap.addMarker(
            MarkerOptions()
                .position(stopPosition)
                .title(values[2])
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkerstop)))!!
        markerName.tag = values[0]
        stopMap[values[0]] = markerName
        stopMap[values[0]]?.isVisible = false
        return stopMap
    }
}