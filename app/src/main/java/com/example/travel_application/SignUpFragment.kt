package com.example.travel_application

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream


class SignUpFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var changeImageButton: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var buttonImage: TextView
    private val IMAGE_PICK_CODE = 1000

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ):  View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val loginButton: TextView = view.findViewById(R.id.haveaccount_id)

        loginButton.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, SignInFragment())
                addToBackStack(null)
            }
        }

        // Ensure changeImageButton is initialized
        changeImageButton = view.findViewById(R.id.change_img_button)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameEditText = view.findViewById(R.id.name_id)
        lastnameEditText = view.findViewById(R.id.lastname_id)
        userNameEditText = view.findViewById(R.id.username_id)
        emailEditText = view.findViewById(R.id.email_id)
        passwordEditText = view.findViewById(R.id.passw_id)
        confirmPasswordEditText = view.findViewById(R.id.passw)
        signUpButton = view.findViewById(R.id.signupbutton_id)
        buttonImage = view.findViewById(R.id.add_photo_text)

        signUpButton.setOnClickListener {
            performSignUp()
        }

        buttonImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        return view
    }

    private fun performSignUp() {
        val name = nameEditText.text.toString().trim()
        val lastname = lastnameEditText.text.toString().trim()
        val username = userNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()


        if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Wszystkie pola są wymagane", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Hasła nie pasują", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("user")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "Konto z tym adresem e-mail już istnieje.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                val userData = hashMapOf(
                                    "name" to name,
                                    "lastname" to lastname,
                                    "username" to username,
                                    "email" to email
                                )

                                if (userId != null) {

                                    selectedImageUri?.let { uri ->
                                        val encodedImage = encodeImageToBase64(uri)
                                        userData["profileImage"] = encodedImage
                                    }


                                    firestore.collection("user").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Rejestracja zakończona pomyślnie",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            val intent = Intent(activity, MenuActivity::class.java)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Błąd zapisu. Spróbuj ponownie.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Rejestracja nie powiodła się. Spróbuj ponownie.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                // Set the image to the ImageView
                changeImageButton.setImageURI(uri)
            }
        }
    }

    private fun encodeImageToBase64(imageUri: Uri): String {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}