package com.example.travel_application

class Model(var documentId: String?=null, var country: String?=null, var city: String="", var img: String? = null) {
    constructor():this("", "", "")
}