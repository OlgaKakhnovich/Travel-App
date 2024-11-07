package com.example.travel_application

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    fun getName(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection(userId).document("user")
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.get("name") ?: "Imię"
                        onSuccess(name.toString())
                    }else{
                        onFailure(Exception("Nie znaleziono danych z imieniem użytkownika"))
                    }
                }
                .addOnSuccessListener {
                    onFailure(Exception("Błąd pobierania imienia "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getSurname(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection(userId).document("user")
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.get("lastname") ?: "Nazwisko"
                        onSuccess(name.toString())
                    }else{
                        onFailure(Exception("Nie znaleziono danych z nazwiskom użytkownika"))
                    }
                }
                .addOnSuccessListener {
                    onFailure(Exception("Błąd pobierania nazwiska "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    fun getEmail(onSuccess: (String)->Unit, onFailure: (Exception)->Unit){
        if(userId!=null){
            val userRef = db.collection(userId).document("user")
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document!=null && document.exists()){
                        val name = document.get("email") ?: "email"
                        onSuccess(name.toString())
                    }else{
                        onFailure(Exception("Nie znaleziono danych z email użytkownika"))
                    }
                }
                .addOnSuccessListener {
                    onFailure(Exception("Błąd pobierania email "))
                }
        }else{
            onFailure(Exception("Użytkownik niezalogowany"))
        }
    }

    //getLocation
    //getPassword
}