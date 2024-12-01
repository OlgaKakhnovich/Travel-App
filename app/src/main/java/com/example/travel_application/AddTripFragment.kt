package com.example.travel_application

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hbb20.CountryCodePicker
import java.io.ByteArrayOutputStream
import java.util.Calendar

class AddTripFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var headerImageUri: Uri? = null
    private val galleryImageUris = mutableListOf<Uri>()
    private lateinit var addHeaderImage: ImageButton
    private lateinit var addCity: EditText
    private lateinit var addCountry: EditText
    private lateinit var addOpinion: EditText
    private lateinit var addTips: EditText
    private lateinit var buttonAddPhotos: Button
    private lateinit var addDateFrom: Button
    private lateinit var addDateTo: Button
    private lateinit var buttonSaveTrip: Button
    private lateinit var starViews: List<ImageView>
    private var selectedRating = 0
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var countryCodePicker: CountryCodePicker

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

        countryCodePicker = view.findViewById(R.id.ccp_add)

        val args = arguments
        if(args!=null){
            val country = args.getString("country");
            val city = args.getString("city");

            countryCodePicker.setCountryForNameCode(country)
            addCity.setText(city);
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
            showGalleryImageSelectionDialog()
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
            parentFragmentManager.commit {
                replace(R.id.frame_container, ProfilFragment())
                addToBackStack(null)
            }

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
        if (galleryImageUris.size >= 10) {
            Toast.makeText(requireContext(), "Możesz dodać maksymalnie 10 zdjęć", Toast.LENGTH_SHORT).show()
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
                            if (galleryImageUris.size < 10) {
                                galleryImageUris.add(uri)
                            } else {
                                Toast.makeText(requireContext(), "Osiągnięto limit 10 zdjęć", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }
                    } else if (data.data != null) {
                        val uri = data.data!!
                        if (galleryImageUris.size < 10) {
                            galleryImageUris.add(uri)
                        } else {
                            Toast.makeText(requireContext(), "Osiągnięto limit 10 zdjęć", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Toast.makeText(requireContext(), "Dodano zdjęcia do galerii", Toast.LENGTH_SHORT).show()
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
                    if (galleryImageUris.size < 10) {
                        galleryImageUris.add(it)
                        Toast.makeText(requireContext(), "Dodano zdjęcie do galerii", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Osiągnięto limit 10 zdjęć", Toast.LENGTH_SHORT).show()
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
        val city = addCity.text.toString()
        val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
        val opinion = addOpinion.text.toString()
        val tips = addTips.text.toString()
        val dateFrom = addDateFrom.text.toString()
        val dateTo = addDateTo.text.toString()
        val userId = auth.currentUser?.uid ?: ""


        val headerImageBase64 = headerImageUri?.let { uri -> convertUriToBase64(uri) }
        val galleryImagesBase64 = galleryImageUris.mapNotNull { convertUriToBase64(it) }

        val tripData = hashMapOf(
            "city" to city,
            "countryCode" to selectedCountryCodeName,
            "opinion" to opinion,
            "tips" to tips,
            "dateFrom" to dateFrom,
            "dateTo" to dateTo,
            "rating" to selectedRating,
            "userId" to userId,
            "headerImage" to headerImageBase64,
            "galleryImages" to galleryImagesBase64
        )

        db.collection("places").add(tripData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Wyjazd zapisany", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
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

    companion object{

        fun newInstance()=AddTripFragment().apply {
            arguments = Bundle().apply {  }
        }
    }
}
