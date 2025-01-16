package com.example.travel_application

class Model(var documentId: String?=null, var countryName: String?=null, var city: String="", var latitude: Double, var longitude: Double) {
    constructor():this("", "", "", 0.0, 0.0)

}

data class Place(
    val id: String = "",
    val city: String = "",
    val countryCode: String = "",
    val dateFrom: String = "",
    val dateTo: String = "",
    val galleryImages: List<String> = emptyList(),
    val headerImage: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val opinion: String = "",
    val rating: Int = 0,
    val tips: String = "",
    val userId: String = "",
    var username: String = "",
    var profileImage: String = "",
    var isSaved: Boolean = false
)

data class User(
    val username: String = "",
    val profileImage: String = ""
)