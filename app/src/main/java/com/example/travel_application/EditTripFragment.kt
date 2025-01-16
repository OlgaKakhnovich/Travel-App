package com.example.travel_application

import Trip
import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.example.travel_application.databinding.FragmentEditTripBinding
import com.example.travel_application.databinding.FragmentProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.hbb20.CountryCodePicker
import org.json.JSONArray
import java.io.ByteArrayOutputStream
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


    private var selectedCoordinates: Pair<Double, Double>? = null
    private var headerImageUri: Uri? = null
    private lateinit var changeHeaderImage: ImageView
    private val IMAGE_PICK_CODE = 1000

    private lateinit var galleryLayout: LinearLayout
    private val maxPhotos = 5
    private lateinit var photoViews: List<ImageView>
    private val galleryImageUris = mutableListOf<Uri>()
    private lateinit var buttonAddPhotos: Button



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
        buttonAddPhotos = binding.buttonAddPhotos2


        galleryLayout = binding.viewPhotos2

        photoViews = listOf(
            binding.listImage12,
            binding.listImage22,
            binding.listImage32,
            binding.listImage42,
            binding.listImage52
        )

        changeHeaderImage = binding.headerImage
        db = FirebaseFirestore.getInstance()

        changeHeaderImage.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

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

        buttonAddPhotos.setOnClickListener {
        if(galleryImageUris.size<maxPhotos){
            showGalleryImageSelectionDialog()
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

        getImageFromFirestore()

        return binding.root
    }

    private fun showGalleryImageSelectionDialog() {
        if(galleryImageUris.size >= 5){
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

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Repeat action
            } else {
                Toast.makeText(requireContext(), "Wymagana zgoda na użycie kamery", Toast.LENGTH_SHORT).show()
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


    private fun updatePhotoViews() {
        photoViews.forEachIndexed { index, imageView ->
            val deleteButtonId = resources.getIdentifier("deleteButton${(index + 1) * 10 + 2}", "id", requireContext().packageName)
            val deleteButton = view?.findViewById<ImageButton>(deleteButtonId)

            if (index < galleryImageUris.size) {
                Glide.with(requireContext())
                    .load(galleryImageUris[index])
                    .into(imageView)
                imageView.visibility = View.VISIBLE

                deleteButton?.visibility = View.GONE

                imageView.setOnClickListener {
                    deleteButton?.visibility = View.VISIBLE
                }

                deleteButton?.setOnClickListener {
                    galleryImageUris.removeAt(index)
                    updatePhotoViews()
                }
            } else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
                deleteButton?.visibility = View.GONE
            }
        }

        buttonAddPhotos.visibility = if (galleryImageUris.size < maxPhotos) View.VISIBLE else View.GONE
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

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "TempImage", null)
        return Uri.parse(path)
    }

    private var cachedProfileImage: Bitmap? = null
    private fun getImageFromFirestore() {

        if(cachedProfileImage!=null){
            binding.headerImage.setImageBitmap(cachedProfileImage)
            return
        }

        db.collection("places").document(tripId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val base64Image = document.getString("headerImage")
                    if (!base64Image.isNullOrEmpty()) {
                        cachedProfileImage = decodeBase64ToBitmap(base64Image)
                        cachedProfileImage?.let {
                            binding.headerImage.setImageBitmap(it)
                        }?: run{
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                        }
                        /*
                                                val bitmap = decodeBase64ToBitmap(base64Image)
                                                if (bitmap != null) {

                                                    binding.changeImg.setImageBitmap(bitmap)
                                                } else {
                                                    Toast.makeText(context, "Nie udało się przekonwertować obrazu", Toast.LENGTH_SHORT).show()
                                                }*/
                    }
                } else {
                    Toast.makeText(context, "Brak danych w Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Błąd pobierania obrazu: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDataInFirebase() {
        val city = editCity.text.toString()
        val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
        val opinion = editOpinion.text.toString()
        val tips = editTips.text.toString()
        val dateFrom = editDateFrom.text.toString()
        val dateTo=editDateTo.text.toString()

        if(city.isNotEmpty() && dateFrom.isNotEmpty() && dateTo.isNotEmpty() && !selectedCountryCodeName.isNullOrEmpty()) {


            if (selectedCoordinates != null) {
                val (latitude, longitude) = selectedCoordinates!!
                updateDate(
                    city,
                    selectedCountryCodeName,
                    opinion,
                    tips,
                    dateFrom,
                    dateTo,
                    latitude,
                    longitude,

                )
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
                        updateDate(
                            city,
                            selectedCountryCodeName,
                            opinion,
                            tips,
                            dateFrom,
                            dateTo,
                            latitude,
                            longitude,

                        )
                    } else {
                        showLocationSelectionMap(
                            coordinates,
                            city,
                            selectedCountryCodeName,
                            opinion,
                            tips,
                            dateFrom,
                            dateTo
                        )
                    }
                }.execute()
            }
        }

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
            "longitude" to longitude,
        )

        val existingImage = trip.headerImage
        if(headerImageUri!=null){
            val encodedImage = encodeImageToBase64(headerImageUri!!)
            tripData["headerImage"] = encodedImage
        }
        else if(!existingImage.isNullOrEmpty()){
            tripData["headerImage"] =existingImage
        }

        if (galleryImageUris.isNotEmpty()) {
            val galleryImagesBase64: List<String> = galleryImageUris.mapNotNull { convertUriToBase64(it) }
          // tripData["galleryImages"] = galleryImagesBase64
        }

        db.collection("places").document(tripId)
            .update(tripData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Dane zostali zmienione", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Nie udalo sie zmienic danych", Toast.LENGTH_SHORT).show()
            }
    }

    private fun encodeImageToBase64(imageUri: Uri): String {
        return try {
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }catch (e:Exception){
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
        dateTo: String
    ) {
        val dialog = MapSelectionFragment.newInstance(locations)
        dialog.setOnLocationSelectedListener { lat, lon ->
            selectedCoordinates = Pair(lat, lon)
            updateDate(
                city, countryCode, opinion, tips,
                dateFrom, dateTo, lat, lon
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            headerImageUri = data?.data
            headerImageUri?.let { uri ->
                // Set the image to the ImageView
                changeHeaderImage.setImageURI(uri)
            }
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

        if (!trip.galleryImages.isNullOrEmpty()) {
            loadGalleryImages(trip.galleryImages ?: listOf())
        }

    }

    private fun loadGalleryImages(images: List<String>) {
        images.forEachIndexed { index, base64Image ->
            val decodedBitmap = decodeBase64ToBitmap(base64Image)
            decodedBitmap?.let {
                val decodedUri = getImageUriFromBitmap(decodedBitmap)
                decodedUri?.let { it1 -> galleryImageUris.add(it1) }

                updatePhotoViews()
            } ?: run {
                Log.e("LoadGalleryImages", "Failed to decode image at index $index")
            }
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


}