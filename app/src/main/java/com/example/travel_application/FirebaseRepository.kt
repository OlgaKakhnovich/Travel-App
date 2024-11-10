package com.example.travel_application

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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



    //getPassword
}