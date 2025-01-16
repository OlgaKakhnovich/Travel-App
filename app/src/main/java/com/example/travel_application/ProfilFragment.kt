package com.example.travel_application

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.devs.vectorchildfinder.VectorChildFinder
import com.example.travel_application.databinding.FragmentProfilBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.hbb20.CountryCodePicker
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt


class ProfilFragment : Fragment() {

    private lateinit var binding: FragmentProfilBinding
    private  val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var myTrips: Button
    private lateinit var addTrip: Button
    private lateinit var addWish: Button
    private var selectedCoordinates: Pair<Double, Double>? = null


    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentProfilBinding.inflate(inflater,container, false)
        firebaseRepository =FirebaseRepository(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

       val imageView = binding.map
        imageView.setOnClickListener {
            showImageDialog()
        }

        colorMap(imageView)

        firebaseRepository.getAbout(
            onSuccess = { about ->
                binding.about.text = about
            },
            onFailure = { binding.about.text = "" }
        )

        firebaseRepository.getUserName(
            onSuccess = { userName ->
                binding.userName.text = userName
            },
            onFailure = { binding.about.text = "" }
        )

        firebaseRepository.getCountryCode(
            onSuccess = {countryCode ->
                if(!countryCode.isNullOrEmpty()){
                    val countryName = getCountryNameFromCode(countryCode)
                    binding.userCity.text = countryName
                }
            },
            onFailure = {binding.userCity.text=""}
        )


        firebaseRepository.fetchCountryCount { countries ->
            if(countries!=0){
                val procent = ((countries * 100)/195.0).roundToInt()
                binding.procentWorld.text = procent.toString()
            }
            binding.countCountry.text = countries.toString()
        }

        firebaseRepository.fetchCityCount { cities ->
            binding.countCity.text = cities.toString()
        }

        getImageFromFirestore()




        //bez menu

        addTrip = binding.addPlace
        addTrip.setOnClickListener {

            val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation)
            bottomNavigationView.visibility = View.GONE

            parentFragmentManager.commit {
                replace(R.id.frame_container, AddTripFragment())
                addToBackStack(null)
            }
        }

        myTrips=binding.tripList
        myTrips.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.frame_container, ListTravelFragment() )
                addToBackStack(null)
            }
        }

        addWish = binding.addWish
        addWish.setOnClickListener {
            showAddWishDialog()
        }

        return binding.root
    }

    private fun showImageDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_map)

        val photoView: PhotoView = dialog.findViewById(R.id.photo_view_map)
        photoView.setImageResource(R.drawable.map_low)
        colorMap(photoView)

        dialog.show()
    }

    private fun colorMap(imageView: ImageView) {
        val vector = VectorChildFinder(requireContext(), R.drawable.map_low, imageView )

        firebaseRepository.fetchCountryCodeRatingDate {  countryData ->
            if(countryData.isNotEmpty()){

                val latestCountryDate = countryData
                    .groupBy { it.countryCode }
                    .mapValues { entry ->
                        entry.value.maxByOrNull { it.date }
                    }

                val starColors = listOf(
                    R.color.star_1,
                    R.color.star_2,
                    R.color.star_3,
                    R.color.star_4,
                    R.color.star_5
                )

                latestCountryDate.values.forEach{ countryData ->
                    if(countryData !=null){
                        try{
                            val path =vector.findPathByName(countryData.countryCode)
                            val color = starColors.getOrElse(countryData.rating -1){R.color.star}
                            path.fillColor = ContextCompat.getColor(requireContext(), color)
                        }catch (e: Exception){
                            println("Error: ${e.message}")
                        }
                    }
                }
                imageView.invalidate()
            }else{
                println("Error-")
            }
        }

    }


    private fun getCountryNameFromCode(countryCode: String): String {
        val locale = Locale("", countryCode)
        val polishLocale = Locale("pl");
        return locale.getDisplayCountry(polishLocale)
    }


    @SuppressLint("MissingInflatedId")
    private fun showAddWishDialog() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_add_wish, null)

        countryCodePicker = dialogView.findViewById<CountryCodePicker>(R.id.ccp_wish)

        val enterCity = dialogView.findViewById<EditText>(R.id.enterCityWish)
        val acceptBtn = dialogView.findViewById<Button>(R.id.acceptBtn)
        val cancelBtn = dialogView.findViewById<TextView>(R.id.cancelBtn)

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()



        acceptBtn.setOnClickListener {
            val selectedCountryCodeInt = countryCodePicker.selectedCountryCodeAsInt
            val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
            val city = enterCity.text.toString().trim()

            if (selectedCoordinates != null) {
                val (latitude, longitude) = selectedCoordinates!!
                if (selectedCountryCodeInt != null && city.isNotEmpty()) {
                    saveDataToBase(city, selectedCountryCodeName, latitude, longitude)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                GetCoordinatesTask(city, selectedCountryCodeName) { coordinates ->
                    if (coordinates.isNullOrEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Nie znaleziono lokalizacji",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (coordinates.size == 1) {
                        val (latitude, longitude) = coordinates[0]
                        saveDataToBase(city, selectedCountryCodeName, latitude, longitude)
                        alertDialog.dismiss()
                    } else {
                        showLocationSelectionMap(coordinates, city, selectedCountryCodeName)
                        alertDialog.dismiss()
                    }
                }.execute()
            }

        }

        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
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

    ) {
        val dialog = MapSelectionFragment.newInstance(locations)
        dialog.setOnLocationSelectedListener { lat, lon ->
            saveDataToBase(city, countryCode, lat, lon)
            dialog.dismiss()

        }
        dialog.show(parentFragmentManager, "MapSelectionFragment")
    }





    private fun saveDataToBase(city: String,  countryName: String, latitude: Double, longitude: Double) {
        val wishRef = db.collection("user").document(userId).collection("wish")
        val wishMap = hashMapOf(
            "city" to city,
            "countryName" to countryName,
            "latitude" to latitude,
            "longitude" to longitude
        )

        wishRef.add(wishMap)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Twoje życzenie zostało dodano",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Twoje życzenie nie zostało dodano. Błąd serwera",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNavigationView.visibility = View.VISIBLE
    }


    private fun getImageFromFirestore() {
        db.collection("user").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val base64Image = document.getString("profileImage")
                    if (!base64Image.isNullOrEmpty()) {

                        val bitmap = decodeBase64ToBitmap(base64Image)
                        if (bitmap != null) {

                            binding.img.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(context, "Nie udało się przekonwertować obrazu", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Brak danych w Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Błąd pobierania obrazu: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    companion object {

    }
}