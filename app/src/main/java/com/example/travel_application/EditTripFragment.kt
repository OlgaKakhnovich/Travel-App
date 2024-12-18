package com.example.travel_application

import Trip
import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.travel_application.databinding.FragmentEditTripBinding
import com.example.travel_application.databinding.FragmentProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.hbb20.CountryCodePicker
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar


class EditTripFragment : Fragment() {

    private lateinit var binding: FragmentEditTripBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var editCity: EditText
    private lateinit var editDateFrom: TextView
    private lateinit var editDateTo: TextView
    private var editRating = 0
    private lateinit var editTips: EditText
    private lateinit var editOpinion: EditText
    private lateinit var back: ImageButton
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var saveBtn: Button

    private lateinit var starViews: List<ImageView>

    private lateinit var tripId: String
    private lateinit var trip: Trip


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentEditTripBinding.inflate(inflater,container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        editCity = binding.editCity
        editOpinion = binding.editOpinion
        editTips = binding.editTips
        countryCodePicker = binding.ccpEdit
        saveBtn = binding.buttonSave1
        back = binding.back5
        db = FirebaseFirestore.getInstance()

        starViews = listOf(
            binding.star11,
            binding.star21,
            binding.star31,
            binding.star41,
            binding.star51
        )

        starViews.forEachIndexed{index, imageView->
            imageView.setOnClickListener {
                editRating=index+1
                updateStars(editRating)
            }
        }

        back.setOnClickListener{
            parentFragmentManager.commit {
                replace(R.id.frame_container, ListTravelFragment())
                addToBackStack(null)
            }
        }

        editDateTo = binding.editDateTo
        editDateFrom = binding.editDateFrom

        editDateFrom.setOnClickListener {
            showDatePickerDialog { date ->
                editDateFrom.text = date
            }
        }

        editDateTo.setOnClickListener {
            showDatePickerDialog{ date ->
                editDateTo.text = date
            }
        }


        saveBtn.setOnClickListener {
            updateDataInFirebase()
            parentFragmentManager.commit {
                replace(R.id.frame_container, ProfilFragment())
                addToBackStack(null)
            }
        }

        tripId = arguments?.getString("tripId") ?: ""
        if(tripId.isNotEmpty()){
            fetchTripDataFromFirestore()
        }else{
            Log.w("ViewTripFragment", "Trip ID is empty")
        }

        return binding.root
    }

    private fun updateDataInFirebase() {
        val city = editCity.text.toString()
        val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
        val opinion = editOpinion.text.toString()
        val tips = editTips.text.toString()
        val dateFrom = editDateFrom.text.toString()
        val dateTo=editDateTo.text.toString()


        GetCoordinatesTask(city, selectedCountryCodeName) { coordinates ->
            if (coordinates.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Nie znaleziono lokalizacji", Toast.LENGTH_SHORT).show()
            } else if (coordinates.size == 1) {
                val (latitude, longitude) = coordinates[0]
                updateDate(city, selectedCountryCodeName, opinion, tips, dateFrom, dateTo, latitude, longitude)
            } else {
                showLocationSelectionMap(coordinates, city, selectedCountryCodeName, opinion, tips, dateFrom, dateTo)
            }
        }.execute()


    }




    private fun updateDate(city: String, countryCode: String, opinion: String, tips: String,
                         dateFrom: String, dateTo: String, latitude: Double, longitude: Double
                         ){
        val tripData = hashMapOf(
            "city" to city,
            "countryCode" to countryCode,
            "opinion" to opinion,
            "tips" to tips,
            "dateFrom" to dateFrom,
            "dateTo" to dateTo,
            "rating" to editRating,
            "latitude" to latitude,
            "longitude" to longitude
        )

        db.collection("places").document(tripId)
            .update(tripData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Dane zostali zmienione", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Nie udalo sie zmienic danych", Toast.LENGTH_SHORT).show()
            }
    }


    private inner class GetCoordinatesTask(
        private val city: String,
        private val country: String,
        private val callback: (List<Pair<Double, Double>>?) -> Unit
    ) : AsyncTask<Void, Void, List<Pair<Double, Double>>?>() {

        override fun doInBackground(vararg params: Void?): List<Pair<Double, Double>>? {
            val query = "$city,$country".replace(" ", "+")
            val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&addressdetails=1"

            return try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Travel-Application")
                connection.connect()

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val results = JSONArray(response)
                    val locations = mutableListOf<Pair<Double, Double>>()

                    for (i in 0 until results.length()) {
                        val location = results.getJSONObject(i)
                        val lat = location.getDouble("lat")
                        val lon = location.getDouble("lon")
                        locations.add(Pair(lat, lon))
                    }

                    locations
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: List<Pair<Double, Double>>?) {
            callback(result)
        }
    }


    private fun showLocationSelectionMap(
        locations: List<Pair<Double, Double>>,
        city: String,
        countryCode: String,
        opinion: String,
        tips: String,
        dateFrom: String,
        dateTo: String,
    ) {
        val dialog = MapSelectionFragment.newInstance(locations)
        dialog.setOnLocationSelectedListener { lat, lon ->
            updateDate(
                city, countryCode, opinion, tips,
                dateFrom, dateTo, lat, lon,
            )
            dialog.dismiss()
        }
        dialog.show(parentFragmentManager, "MapSelectionFragment")
    }

    private fun updateStars(editRating: Int) {

        val starColors = listOf(
            R.color.star_1,
            R.color.star_2,
            R.color.star_3,
            R.color.star_4,
            R.color.star_5,
        )

        starViews.forEachIndexed { index, imageView ->
            if(index< editRating){
                val color = starColors.getOrElse(index){R.color.grey}
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), color))
            }else{
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }

    }

    private fun showDatePickerDialog(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), {_, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth+1}/$selectedYear"
            callback(date)
        },year, month, day)

        datePickerDialog.show()
    }

    private fun fetchTripDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("places").document(tripId)
            .get()
            .addOnSuccessListener { document ->
                if (document!=null){
                    val tripData = document.toObject(Trip::class.java)
                    if(tripData!=null){
                        trip=tripData
                        updateFragment()
                    }
                }
            }
            .addOnFailureListener { e->
                Log.w("Firestore", "Error getting document", e)
            }
    }

    private fun updateFragment() {
        editTips.setText(trip.tips ?: "")
        editOpinion.setText(trip.opinion ?: "")
        editCity.setText(trip.city)
        countryCodePicker.setCountryForNameCode(trip.countryCode)
        editDateTo.text = trip.dateTo
        editDateFrom.text = trip.dateFrom

        editRating = trip.rating.toInt()
        updateStars(editRating)

    }

    companion object {

    }
}