package com.example.travel_application

class Model(var documentId: String?=null, var countryName: String?=null, var city: String="") {
    constructor():this("", "", "")

}