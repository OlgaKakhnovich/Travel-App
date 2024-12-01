package com.example.travel_application

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    fun getName(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.getString("name") ?: "Imię"
                        onSuccess(name)
                    }else{
                        onFailure(Exception("Nie znaleziono danych z imieniem użytkownika"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania imienia "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getSurname(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.getString("lastname") ?: "Nazwisko"
                        onSuccess(name)
                    }else{
                        onFailure(Exception("Nie znaleziono danych z nazwiskom użytkownika"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania nazwiska "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getUserName(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val userName = document.getString("username") ?: "Imię użytkownika"
                        onSuccess(userName)
                    }else{
                        onFailure(Exception("Nie znaleziono danych z imieniem użytkownika"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania imienia "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getLocation(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val location = document.getString("location")
                        if(location!=null){
                            onSuccess(location)
                        }else{
                            onSuccess("")
                        }
                    }else{
                        onFailure(Exception("Nie znaleziono danych użytkownika"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania lokalizacji "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getAbout(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val about = document.getString("about")
                        if(about!=null){
                            onSuccess(about)
                        }else{
                            onSuccess("")
                        }
                    }else{
                        onFailure(Exception("Nie znaleziono danych użytkownika"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania lokalizacji "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getEmail(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.get("email") ?: "email"
                        onSuccess(name.toString())
                    }else{
                        onFailure(Exception("Nie znaleziono danych z email użytkownika"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania email "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }


    fun getCountryCode(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection("user").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.get("countryCode") ?: "countryCode"
                        onSuccess(name.toString())
                    }else{
                        onFailure(Exception("Nie znaleziono danych"))
                    }
                }
                .addOnFailureListener {
                    onFailure(Exception("Błąd pobierania kraja"))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }



    data class CountryData(val countryCode:String, val rating: Int, val date: Date)

    fun fetchCountryCodeRatingDate(callback: (List<CountryData>) -> Unit){
       val countryDataList = mutableListOf<CountryData>()

        db.collection("places")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                for(document in querySnapshot.documents){
                    try{
                        val countryCode = document.getString("countryCode")
                        val rating = document.getLong("rating")?.toInt()?:0
                        val dateString = document.getString("dateTo")
                        val date = dateString?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it)
                        }
                        if(countryCode!= null && date !=null){
                            countryDataList.add(CountryData(countryCode, rating, date))
                        }
                    }catch (e: Exception){
                        println("Error: ${e.message}")
                    }
                }
                callback(countryDataList)
            }
            .addOnFailureListener { exception ->
                println("Error: ${exception.message}")
                callback(emptyList())
            }
    }

    fun fetchCountryCount(callback: (Int)->Unit){
        db.collection("places")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                val countries = mutableSetOf<String>()
                for(document in querySnapshot.documents){
                    val countryCode = document.getString("countryCode")
                    if(countryCode!=null){
                        countries.add(countryCode)
                    }
                }
                callback(countries.size)
            }
            .addOnFailureListener { exception ->
                println("Error: ${exception.message}")
                callback(0)
            }
    }

    fun fetchCityCount(callback: (Int)->Unit){
        db.collection("places")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                val cities = mutableSetOf<String>()
                for(document in querySnapshot.documents){
                    val city = document.getString("city")
                    if(city!=null){
                        cities.add(city)
                    }
                }
                callback(cities.size)
            }
            .addOnFailureListener { exception ->
                println("Error: ${exception.message}")
                callback(0)
            }
    }


}