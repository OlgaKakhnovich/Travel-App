package com.example.travel_application

class Model(var documentId: String?=null, var country: String="", var city: String="") {
    constructor():this("", "", "")
}