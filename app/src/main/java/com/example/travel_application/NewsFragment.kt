package com.example.travel_application

import PlaceAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.hbb20.CountryCodePicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NewsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var sortBtn: ImageButton
    private lateinit var filterBtn: ImageButton
    private lateinit var findBtn: ImageButton
    private lateinit var find: LinearLayout
    private lateinit var search: ImageButton
    private lateinit var searchText: EditText
    private val placeList = mutableListOf<Place>()
    private lateinit var more: Button

    private var selectedOptionId: Int =R.id.data_new
    private var selectedOptionRId: Int?=null
    private var selectedCountry:String ?=null
    private var searchCity: String? =null

    private var lastVisibleDoc: DocumentSnapshot? =null
    private val pageSize =10

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_news, container, false)
        recyclerView = view.findViewById(R.id.list_post)
        recyclerView.layoutManager = LinearLayoutManager(context)
        placeAdapter = PlaceAdapter(placeList)
        recyclerView.adapter = placeAdapter

        sortBtn = view.findViewById(R.id.sort)
        sortBtn.setOnClickListener{showSortDialog()}
        findBtn = view.findViewById(R.id.find)
        find = view.findViewById(R.id.find_id)
        findBtn.setOnClickListener{
            find.visibility = if (find.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        search=view.findViewById(R.id.okbutton4)
        searchText =view.findViewById(R.id.find_edit_text)
        search.setOnClickListener{
            searchCity = searchText.text.toString().trim()
            find.visibility = View.GONE
            fetchPlaces(initialLoad = true)
        }
        filterBtn = view.findViewById(R.id.filter)
        filterBtn.setOnClickListener{showFilterDialog()}
        more = view.findViewById(R.id.button_more)

        more.setOnClickListener{
            fetchPlaces(initialLoad = true)
        }

        fetchPlaces(initialLoad = true)
        return view
    }

  @SuppressLint("MissingInflatedId")
    private fun showSortDialog() {
        val dialogView = layoutInflater.inflate(R.layout.sort_dialog, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)

        var temp = selectedOptionId
        radioGroup.check(selectedOptionId)

        radioGroup.setOnCheckedChangeListener{_, checkedId ->
            selectedOptionId = checkedId
        }

        val cancelBtn = dialogView.findViewById<TextView>(R.id.cancelBtn)
        val applyBtn = dialogView.findViewById<Button>(R.id.acceptBtn)

        applyBtn.setOnClickListener{
            alertDialog.dismiss()
            fetchPlaces(initialLoad = true)
        }
        cancelBtn.setOnClickListener {
            selectedOptionId=temp
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    @SuppressLint("MissingInflatedId")
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.filtr_dialog, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val radioGroupRating = dialogView.findViewById<RadioGroup>(R.id.radioGroupRating)
        val country = dialogView.findViewById<CountryCodePicker>(R.id.country_filter)

        val tempC = selectedCountry
        selectedOptionRId?.let { radioGroupRating.check(it) }

        selectedCountry?.let{
            country.setCountryForNameCode(it)
        }
        country.setOnCountryChangeListener {
            selectedCountry = country.selectedCountryNameCode
        }
        if(selectedCountry.isNullOrEmpty()){
            selectedCountry= "Nie wybrano"
        }

        val tempR =selectedOptionRId


        radioGroupRating.setOnCheckedChangeListener{_, checkedId ->
            selectedOptionRId = checkedId
        }

        val cancelBtn = dialogView.findViewById<TextView>(R.id.cancelBtn)
        val dontBtn = dialogView.findViewById<TextView>(R.id.dontFilter)
        val applyBtn = dialogView.findViewById<Button>(R.id.acceptBtn)

        applyBtn.setOnClickListener{
            alertDialog.dismiss()
            if(tempR!=selectedOptionRId || tempC!=selectedCountry){
            fetchPlaces(initialLoad = true)}
        }
        cancelBtn.setOnClickListener {
            selectedOptionRId= tempR
            selectedCountry = tempC
            alertDialog.dismiss()
        }

        dontBtn.setOnClickListener {
            radioGroupRating.clearCheck()
            selectedOptionRId =null
            selectedCountry = null
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun fetchPlaces(initialLoad: Boolean  =true) {
        val db = FirebaseFirestore.getInstance()
        var query: Query = db.collection("places")

        when(selectedOptionId){
            R.id.rating_up->{
                query = query.orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
            }
            R.id.rating_down->{
                query = query.orderBy("rating", com.google.firebase.firestore.Query.Direction.ASCENDING)
            }
            R.id.data_new->{
              query = query.orderBy("dateTo", com.google.firebase.firestore.Query.Direction.DESCENDING)
            }
            R.id.data_old->{
                query = query.orderBy("dateTo", com.google.firebase.firestore.Query.Direction.ASCENDING)
            }
        }

        if (selectedOptionRId!=null){

            val ratings = when (selectedOptionRId) {
                R.id.rating_4_5 -> listOf(4, 5)
                R.id.rating_2_3 -> listOf(2, 3)
                R.id.rating_0_1 -> listOf(0, 1)
                else -> null
            }

            ratings?.let { query = query.whereIn("rating", it) }
        }

        if(selectedCountry!=null){
            query=query.whereEqualTo("countryCode", selectedCountry )
        }

       if(searchCity!=null && searchCity!=""){
            val lowerCaseCity = searchCity?.trim()?.toLowerCase(Locale.getDefault())
            query = query.whereEqualTo("cityToLowerCase", lowerCaseCity)
        }

        query = query.limit(pageSize.toLong())
        if(!initialLoad && lastVisibleDoc!=null){
            query = query.startAfter(lastVisibleDoc)
        }

        query.get()
            .addOnSuccessListener { documents->
                if(!documents.isEmpty){
                    if(initialLoad)
                        placeList.clear()

                    for(document in documents){
                        val place = document.toObject(Place::class.java)
                        placeList.add(place)
                    }

                    if(selectedOptionId==R.id.data_new){
                    placeList.sortByDescending {
                       parseFlexibleDate(it.dateTo)
                    }}

                    if(selectedOptionId==R.id.data_old){
                        placeList.sortBy{
                            parseFlexibleDate(it.dateTo)
                        }}

                    lastVisibleDoc = documents.documents.last()
                    placeAdapter.notifyDataSetChanged()
                }else{
                    searchCity?.let{
                        if(it.isNotEmpty()){
                            Toast.makeText(context, "Nie znaleziono miasta: \"$it\".", Toast.LENGTH_SHORT).show()
                        }
                    }

                    if(initialLoad){
                        placeList.clear()
                        placeAdapter.notifyDataSetChanged()
                    }
                }
                if (documents.size() < pageSize) {
                    more.isEnabled = false
                    more.text = "Koniec"
                }else
                {
                    more.isEnabled = true
                    more.text = "Zaladuj więcej"
                }
            }.addOnFailureListener { exception ->
                Log.e("", exception.toString())
            }
    }

    fun parseFlexibleDate(dateStr: String): Date?{
        val possibleFormats= listOf(
            "d/M/yyyy",
            "dd/M/yyyy",
            "d/MM/yyyy",
            "dd/MM/yyyy"
        )

        for (format in possibleFormats) {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.isLenient = false
            try {
                return dateFormat.parse(dateStr)
            } catch (e: ParseException) {

            }
        }
        return null
    }

}