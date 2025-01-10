package com.example.travel_application

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hbb20.CountryCodePicker
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale







class AddTripFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var back: ImageButton
    private var headerImageUri: Uri? = null
    private val galleryImageUris = mutableListOf<Uri>()
    private lateinit var addHeaderImage: ImageButton
    private lateinit var addCity: EditText
    private lateinit var addOpinion: EditText
    private lateinit var addTips: EditText
    private lateinit var buttonAddPhotos: Button
    private lateinit var addDateFrom: Button
    private lateinit var addDateTo: Button
    private lateinit var buttonSaveTrip: Button
    private lateinit var starViews: List<ImageView>
    private var selectedRating = 0
    private lateinit var countryCodePicker: CountryCodePicker

    private val maxPhotos = 5
    private lateinit var viewPhotos: LinearLayout
    private lateinit var photoViews: List<ImageView>
    private var selectedCoordinates: Pair<Double, Double>? = null

    var argLon: Double = 0.0
    var argLat: Double =0.0


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_trip, container, false)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        addHeaderImage = view.findViewById(R.id.add_headerImage)
        addCity = view.findViewById(R.id.add_city)
        addOpinion = view.findViewById(R.id.add_opinion)
        addTips = view.findViewById(R.id.add_tips)
        buttonAddPhotos = view.findViewById(R.id.button_add_photos)
        addDateFrom = view.findViewById(R.id.add_date_from)
        addDateTo = view.findViewById(R.id.add_date_to)
        buttonSaveTrip = view.findViewById(R.id.button_save)
        back = view.findViewById(R.id.back2)

        countryCodePicker = view.findViewById(R.id.ccp_add)
        viewPhotos = view.findViewById(R.id.view_photos)

        photoViews = listOf(
            view.findViewById(R.id.listImage1),
            view.findViewById(R.id.listImage2),
            view.findViewById(R.id.listImage3),
            view.findViewById(R.id.listImage4),
            view.findViewById(R.id.listImage5)
        )




        val args = arguments
        if(args!=null){
            val country = args.getString("country")
            val city = args.getString("city")
            argLon = args.getDouble("longitude");
            argLat = args.getDouble("latitude");

            countryCodePicker.setCountryForNameCode(country)
            addCity.setText(city)
        }

        starViews = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1
                updateStars(selectedRating)
            }
        }

        addHeaderImage.setOnClickListener {
            showHeaderImageSelectionDialog()
        }

        buttonAddPhotos.setOnClickListener {
            if (galleryImageUris.size < maxPhotos) {
                showGalleryImageSelectionDialog()
            }
        }

        addDateFrom.setOnClickListener {
            showDatePickerDialog { date ->
                addDateFrom.text = date
            }
        }

        addDateTo.setOnClickListener {
            showDatePickerDialog { date ->
                addDateTo.text = date
            }
        }

        buttonSaveTrip.setOnClickListener {
            saveTripToFirestore()

        }

        back.setOnClickListener{
            parentFragmentManager.commit {
                replace(R.id.frame_container, ProfilFragment())
                addToBackStack(null)
            }
        }

        addCity.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {

                fetchCoordinatesForCityAndCountry()
            }
        }


        countryCodePicker.setOnCountryChangeListener {
            fetchCoordinatesForCityAndCountry()
        }





        return view
    }





    private fun showHeaderImageSelectionDialog() {
        val options = arrayOf("Wybierz z Galerii", "Zrób Zdjęcie")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Dodaj Zdjęcie Nagłówka")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> selectHeaderImageFromGallery()
                1 -> takeHeaderPhoto()
            }
        }
        builder.show()
    }

    private fun selectHeaderImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectHeaderImageLauncher.launch(intent)
    }

    private val selectHeaderImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                headerImageUri = result.data!!.data
                addHeaderImage.setImageURI(headerImageUri)
            }
        }

    private fun takeHeaderPhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takeHeaderPhotoLauncher.launch(intent)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val takeHeaderPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val bitmap = result.data!!.extras?.get("data") as Bitmap
                headerImageUri = getImageUriFromBitmap(bitmap)
                addHeaderImage.setImageURI(headerImageUri)
            }
        }

    private fun showGalleryImageSelectionDialog() {

        if (galleryImageUris.size >= 5) {
            Toast.makeText(requireContext(), "Możesz dodać maksymalnie 5 zdjęć", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("Wybierz z Galerii", "Zrób Zdjęcie")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Dodaj Zdjęcia do Galerii")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> selectMultipleImagesFromGallery()
                1 -> takeGalleryPhoto()
            }
        }
        builder.show()
    }


    private fun selectMultipleImagesFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        selectGalleryImageLauncher.launch(intent)
    }

    private val selectGalleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.let { data ->
                    if (data.clipData != null) {
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            if (galleryImageUris.size < 5) {
                                galleryImageUris.add(uri)
                                updatePhotoViews()

                            } else {
                                Toast.makeText(requireContext(), "Osiągnięto limit 5 zdjęć", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }
                    } else if (data.data != null) {
                        val uri = data.data!!
                        if (galleryImageUris.size < 5) {
                            galleryImageUris.add(uri)
                            updatePhotoViews()
                            Toast.makeText(requireContext(), "Dodano zdjęcia do galerii", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Przekroczono limit 5 zdjęć", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                updatePhotoViews()

            }
        }




    private fun takeGalleryPhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takeGalleryPhotoLauncher.launch(intent)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    private val takeGalleryPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val bitmap = result.data!!.extras?.get("data") as Bitmap
                val uri = getImageUriFromBitmap(bitmap)
                uri?.let {
                    if (galleryImageUris.size < 5) {
                        galleryImageUris.add(it)
                        updatePhotoViews()
                        Toast.makeText(requireContext(), "Dodano zdjęcie do galerii", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Przekroczono limit 5 zdjęć", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }




    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "TempImage", null)
        return Uri.parse(path)
    }


    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Repeat action
            } else {
                Toast.makeText(requireContext(), "Wymagana zgoda na użycie kamery", Toast.LENGTH_SHORT).show()
            }
        }



    private fun convertUriToBase64(uri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun updatePhotoViews() {

        photoViews.forEachIndexed { index, imageView ->
            if (index < galleryImageUris.size) {
                Glide.with(requireContext())
                    .load(galleryImageUris[index])
                    .into(imageView)
                imageView.visibility = View.VISIBLE


                imageView.setOnClickListener {
                    galleryImageUris.removeAt(index)

                    updatePhotoViews()
                }
            } else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }

        buttonAddPhotos.visibility = if (galleryImageUris.size < 5) View.VISIBLE else View.GONE
    }






    private fun showDatePickerDialog(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            callback(date)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun updateStars(rating: Int) {

        val starColors = listOf(
            R.color.star_1,
            R.color.star_2,
            R.color.star_3,
            R.color.star_4,
            R.color.star_5
        )
        starViews.forEachIndexed { index, imageView ->
            if (index < rating) {
                val color = starColors.getOrElse(index){ R.color.grey }
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), color))
            } else {
                imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }
    }

    private fun saveTripToFirestore() {
        val city = addCity.text.toString().trim()
        val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
        val opinion = addOpinion.text.toString().trim()
        val tips = addTips.text.toString().trim()
        val dateFrom = addDateFrom.text.toString()
        val dateTo = addDateTo.text.toString()
        val userId = auth.currentUser?.uid.orEmpty()


        if (city.isNotEmpty() && dateFrom.isNotEmpty() && dateTo.isNotEmpty() && selectedRating != 0) {


            val headerImageBase64 = headerImageUri?.let { uri -> convertUriToBase64(uri) }
            val galleryImagesBase64 = galleryImageUris.mapNotNull { convertUriToBase64(it) }


            if(argLon!=0.0 && argLat!=0.0){
                saveDataToFirestore(
                    city, selectedCountryCodeName, opinion, tips,
                    dateFrom, dateTo, userId, argLat, argLon,
                    headerImageBase64, galleryImagesBase64
                )
            }
            else if (selectedCoordinates != null) {
                val (latitude, longitude) = selectedCoordinates!!
                saveDataToFirestore(
                    city, selectedCountryCodeName, opinion, tips,
                    dateFrom, dateTo, userId, latitude, longitude,
                    headerImageBase64, galleryImagesBase64
                )
            } else {
                GetCoordinatesTask(city, selectedCountryCodeName) { coordinates ->
                    when {
                        coordinates.isNullOrEmpty() -> {
                            Toast.makeText(
                                requireContext(),
                                "Nie znaleziono lokalizacji",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        coordinates.size == 1 -> {
                            val (latitude, longitude) = coordinates[0]
                            saveDataToFirestore(
                                city, selectedCountryCodeName, opinion, tips,
                                dateFrom, dateTo, userId, latitude, longitude,
                                headerImageBase64, galleryImagesBase64
                            )
                        }
                        else -> {
                            showLocationSelectionMap(coordinates)
                        }
                    }
                }.execute()
            }

            parentFragmentManager.commit {
                replace(R.id.frame_container, ProfilFragment())
                addToBackStack(null)
            }

        }

        else{  Toast.makeText(requireContext(), "Uzupełnij wymagane pola: kraj, miasto, data i ocena.", Toast.LENGTH_SHORT).show()
            return
        }

    }

    private fun saveDataToFirestore(
        city: String, countryCode: String, opinion: String, tips: String,
        dateFrom: String, dateTo: String, userId: String, latitude: Double, longitude: Double,
        headerImageBase64: String?, galleryImagesBase64: List<String>
    ) {
        val tripData = hashMapOf(
            "city" to city,
            "countryCode" to countryCode,
            "opinion" to opinion,
            "tips" to tips,
            "dateFrom" to dateFrom,
            "dateTo" to dateTo,
            "rating" to selectedRating,
            "userId" to userId,
            "headerImage" to headerImageBase64,
            "galleryImages" to galleryImagesBase64,
            "latitude" to latitude,
            "longitude" to longitude
        )

        db.collection("places").add(tripData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Wyjazd zapisany", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { _ ->
                Toast.makeText(requireContext(), "Błąd zapisu. Spróbuj ponownie.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun countryCodeFromCountryName(country: String): String {
        return try {
            Locale.getISOCountries()
                .map { isoCode -> Locale("", isoCode) }
                .firstOrNull { locale -> locale.displayCountry.equals(country, ignoreCase = true) }
                ?.country ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }





    private inner class GetCoordinatesTask(
        private val city: String,
        private val country: String,
        private val callback: (List<Pair<Double, Double>>?) -> Unit
    ) : AsyncTask<Void, Void, List<Pair<Double, Double>>?>() {

        override fun doInBackground(vararg params: Void?): List<Pair<Double, Double>>? {
            val query = "$city".replace(" ", "+")
            val url = "https://nominatim.openstreetmap.org/search?q=$query&countrycodes=$country&format=json&addressdetails=1"

            return try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Travel-Application")
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val results = JSONArray(response)
                    val locations = mutableListOf<Pair<Double, Double>>()

                    for (i in 0 until results.length()) {
                        val location = results.getJSONObject(i)
                        val lat = location.getDouble("lat")
                        val lon = location.getDouble("lon")
                        locations.add(Pair(lat, lon))
                    }

                    val uniqueLocations = mutableListOf<Pair<Double, Double>>()
                    for (loc in locations) {
                        if (uniqueLocations.none { calculateDistance(it.first, it.second, loc.first, loc.second) < 5 }) {
                            uniqueLocations.add(loc)
                        }
                    }

                    uniqueLocations
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: List<Pair<Double, Double>>?) {
            super.onPostExecute(result)
            callback(result)
        }
    }


    private fun showLocationSelectionMap(locations: List<Pair<Double, Double>>) {
        val dialog = MapSelectionFragment.newInstance(locations)
        dialog.setOnLocationSelectedListener { lat, lon ->
            selectedCoordinates = Pair(lat, lon)
            Toast.makeText(requireContext(), "Lokalizacja wybrana: $lat, $lon", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show(parentFragmentManager, "MapSelectionFragment")
    }








    private fun fetchCoordinatesForCityAndCountry() {
        val city = addCity.text.toString().trim()
        val country = countryCodePicker.selectedCountryNameCode

        if (city.isNotEmpty() && country.isNotEmpty()) {
            GetCoordinatesTask(city, country) { coordinates ->
                if (coordinates.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Nie znaleziono lokalizacji", Toast.LENGTH_SHORT).show()

                } else if (coordinates.size == 1) {

                    selectedCoordinates = coordinates[0]
                    val (latitude, longitude) = coordinates[0]
                    Toast.makeText(requireContext(), "Lokalizacja znaleziona: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                } else {

                    showLocationSelectionMap(coordinates)
                }
            }.execute()
        }
    }


}
