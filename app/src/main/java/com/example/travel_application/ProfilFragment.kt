package com.example.travel_application

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.devs.vectorchildfinder.VectorChildFinder
import com.example.travel_application.databinding.FragmentProfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.hbb20.CountryCodePicker
import java.util.Locale


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


    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentProfilBinding.inflate(inflater,container, false)
        firebaseRepository =FirebaseRepository(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()

       val imageView = binding.map

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

    private fun colorMap(imageView: ImageView) {
        val vector = VectorChildFinder(requireContext(), R.drawable.map_low, imageView )
        firebaseRepository.fetchCountryCodeByUserId {countryCodes ->
            if(countryCodes.isNotEmpty()){
                for(countryCode in countryCodes){
                    try{
                        val path = vector.findPathByName(countryCode)
                        path.fillColor = Color.YELLOW
                    }catch (e: Exception){
                        println("Not find country with id: ${countryCode}")
                    }
                }
                imageView.invalidate()
            }
            else{
                println("Not find")
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
            val city = enterCity.text.toString()

            if(selectedCountryCodeInt!=null && city.isNotEmpty()){
                saveDataToBase(city, selectedCountryCodeInt, selectedCountryCodeName)
                alertDialog.dismiss()
            }else{
                Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }

        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }

       alertDialog.show()
    }

    private fun saveDataToBase(city: String, countryCode: Int, countryName: String) {
        val wishRef = db.collection("user").document(userId).collection("wish")
        val wishMap = hashMapOf(
            "countryInt" to countryCode,
            "city" to city,
            "countryName" to countryName,
        )

        wishRef.document().set(wishMap).addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "Twoje życzenie zostało dodano",
                Toast.LENGTH_SHORT
            ).show()

        }

    }


    override fun onResume() {
        super.onResume()
        val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNavigationView.visibility = View.VISIBLE
    }

    companion object {

    }
}