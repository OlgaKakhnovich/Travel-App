package com.example.travel_application

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travel_application.databinding.FragmentEditProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.hbb20.CountryCodePicker
import java.io.ByteArrayOutputStream
import androidx.fragment.app.commit
import android.provider.MediaStore
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity

class EditProfilFragment : Fragment() {

    private lateinit var binding: FragmentEditProfilBinding
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
     private lateinit var firebaseRepository: FirebaseRepository
     private lateinit var countryCodePicker: CountryCodePicker

    private lateinit var changeImageButton: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var buttonImage: TextView
    private val IMAGE_PICK_CODE = 1000


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfilBinding.inflate(inflater,container,false)
        firebaseRepository = FirebaseRepository(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

        countryCodePicker = binding.ccp

        buttonImage = binding.changeImgText
        changeImageButton = binding.changeImg

        buttonImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        firebaseRepository.getName(
            onSuccess = {
                name->binding.name.setText(name)
            },
            onFailure = {
                binding.name.setText("Edytuj imię")
            })

        firebaseRepository.getSurname(
            onSuccess = {surname->
                binding.surname.setText(surname)
            },
            onFailure = {
                binding.surname.setText("Edytuj nazwisko")
            }
        )

        firebaseRepository.getAbout(
            onSuccess = { about ->
                binding.about.setText(about)
            },
            onFailure = {binding.about.setText("O mnie")}
        )


        firebaseRepository.getUserName(
            onSuccess = { userName ->
                binding.username.setText(userName)
            },
            onFailure = {binding.about.setText("Imię użytkownika")}
        )

        firebaseRepository.getCountryCode(
            onSuccess = { countryCode ->
            countryCodePicker.setCountryForNameCode(countryCode)
            },
            onFailure = {}
        )



        binding.confirmButton.setOnClickListener{
            updateUser()

            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        getImageFromFirestore()

        return binding.root
    }


    private fun updateUser(){
        val name = binding.name.text.toString().trim()
        val lastname = binding.surname.text.toString().trim()
        val username = binding.username.text.toString().trim()

        val selectedCountryCodeInt = countryCodePicker.selectedCountryCodeAsInt
        val selectedCountryCodeName = countryCodePicker.selectedCountryNameCode
        val about = binding.about.text.toString().trim().ifEmpty { "" }


        if(name.isNotEmpty() && lastname.isNotEmpty()){
            val userData = mutableMapOf<String, Any>(
                "name" to name,
                "lastname" to lastname,
                "username" to username,
            )
            val userRef = db.collection("user").document(userId)

            userRef.get().addOnSuccessListener { document->
                if(document!=null && document.exists()){
                    val existingImage = document.getString("profileImage")
                    if(selectedImageUri!=null){
                        val encodedImage = encodeImageToBase64(selectedImageUri!!)
                        userData["profileImage"] = encodedImage
                    }else if(!existingImage.isNullOrEmpty()){
                        userData["profileImage"] = existingImage
                    }


                    val countryExists = document.contains("country")
                    val aboutExists = document.contains("about")

                   if(selectedCountryCodeInt!=null){
                       val countryData = mutableMapOf<String, Any>(
                           "countryCode" to selectedCountryCodeName,
                       )
                        if(countryExists){
                            userRef.update( countryData)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }else{
                            userRef.set(countryData)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }
                    }

                    if(about!=null){
                        if(aboutExists){
                            userRef.update( "about", about)
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }else{
                            userRef.set(mapOf("about" to about), SetOptions.merge())
                                .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                                .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }
                        }
                    }

                    userRef.update(userData)
                        .addOnSuccessListener { Log.d(TAG, "Zmiany zostały zapisane") }
                        .addOnFailureListener { e->Log.w(TAG, "Zmiany nie zostały zapisane ", e) }

                }else{
                    Log.w(TAG, "User document does not exist")
                }
            }.addOnFailureListener { e->
                Log.w(TAG, "Failed to fetch user document", e)
            }
        }
        else{
            Toast.makeText(requireContext(), "Wypełnij pola", Toast.LENGTH_SHORT).show()
        }
    }

//dodac funkcje po zmianie zdjecia

    private var cachedProfileImage: Bitmap? = null
    private fun getImageFromFirestore() {

        if(cachedProfileImage!=null){
            binding.changeImg.setImageBitmap(cachedProfileImage)
            return
        }

        db.collection("user").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val base64Image = document.getString("profileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        cachedProfileImage = decodeBase64ToBitmap(base64Image)
                        cachedProfileImage?.let {
                            binding.changeImg.setImageBitmap(it)
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

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                // Set the image to the ImageView
                changeImageButton.setImageURI(uri)
            }
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



    companion object {
       fun newInstance(param1: String, param2: String) =
           EditProfilFragment().apply {
               arguments = Bundle().apply {  }
           }
    }
}