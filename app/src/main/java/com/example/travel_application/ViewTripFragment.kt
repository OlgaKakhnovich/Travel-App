package com.example.travel_application

import Trip
import android.content.ContentValues
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayInputStream
import java.util.Locale

class ViewTripFragment : Fragment() {

    private lateinit var headerImageView: ImageView
    private lateinit var galleryLayout: LinearLayout
    private lateinit var cityTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var dateFromTextView: TextView
    private lateinit var dateToTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var tipsTextView: TextView
    private lateinit var opinionTextView: TextView
    private lateinit var back: ImageButton
    private lateinit var editTripButton: ImageButton

    private lateinit var tripId: String
    private lateinit var trip: Trip

    private lateinit var deleteTrip: ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_trip, container, false)

        headerImageView = view.findViewById(R.id.headerImage)
        galleryLayout = view.findViewById(R.id.detailsContainer)
        cityTextView = view.findViewById(R.id.list_city_id)
        countryTextView = view.findViewById(R.id.list_country_id)
        dateFromTextView = view.findViewById(R.id.date_from_view)
        dateToTextView = view.findViewById(R.id.date_to_view)
        ratingTextView = view.findViewById(R.id.star_id_view)
        tipsTextView = view.findViewById(R.id.list_tips_id)
        opinionTextView = view.findViewById(R.id.list_opinion_id)
        back = view.findViewById(R.id.back)
        editTripButton = view.findViewById(R.id.edit_trip_button)


        cityTextView.visibility = View.VISIBLE
        countryTextView.visibility = View.VISIBLE
        dateFromTextView.visibility = View.VISIBLE
        dateToTextView.visibility = View.VISIBLE
        ratingTextView.visibility = View.VISIBLE
        tipsTextView.visibility = View.VISIBLE
        opinionTextView.visibility = View.VISIBLE

        tripId = arguments?.getString("tripId") ?: ""

        if (tripId.isNotEmpty()) {
            fetchTripDataFromFirestore()
        } else {
            Log.w("ViewTripFragment", "Trip ID is empty")
        }

        back.setOnClickListener{
            parentFragmentManager.commit {
                replace(R.id.frame_container, ListTravelFragment())
                addToBackStack(null)
            }
        }

        editTripButton.setOnClickListener {
            val fragment: Fragment = EditTripFragment()
            val args = Bundle()
            args.putString("tripId", tripId)

            fragment.arguments=args

            parentFragmentManager.commit {
                replace(R.id.frame_container, fragment )
                addToBackStack(null)
            }


        }

        deleteTrip = view.findViewById(R.id.delete_trip_button)

        deleteTrip.setOnClickListener{
            val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(deleteTrip.context)
            alertDialogBuilder.setTitle("Usunąć podróż")
            alertDialogBuilder.setMessage("Czy pewno chcesz usunąć tą podróż?")
            alertDialogBuilder.setPositiveButton("Tak"){ dialogInterface: DialogInterface, i: Int ->
                deleteItemFromDatabase()
                dialogInterface.dismiss()
            }
            alertDialogBuilder.setNegativeButton("Nie"){ dialogInterface: DialogInterface, i:Int ->
                dialogInterface.dismiss()
            }
            alertDialogBuilder.show()
        }


        return view
    }

    private fun deleteItemFromDatabase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("places").document(tripId).delete()
            .addOnSuccessListener {
                Log.w(ContentValues.TAG,"Element został usunięty")
            }
            .addOnFailureListener { e->
                Log.w(ContentValues.TAG,"Błąd w usunięciu elementa", e)
            }
    }

    private fun fetchTripDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("places").document(tripId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val tripData = document.toObject(Trip::class.java)
                    if (tripData != null) {
                        trip = tripData
                        Log.d("Firestore", "Fetched trip data: $trip")
                        updateUI()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting document", e)
            }
    }

    private fun getCountryNameFromCode(countryCode: String): String {
        val locale = Locale("", countryCode)
        val polishLocale = Locale("pl")
        return locale.getDisplayCountry(polishLocale)
    }
    private fun updateUI() {


        cityTextView.text = trip.city
        countryTextView.text = getCountryNameFromCode(trip.countryCode)
        dateFromTextView.text = trip.dateFrom
        dateToTextView.text = trip.dateTo
        ratingTextView.text = trip.rating.toString()
        tipsTextView.text = trip.tips ?: ""
        opinionTextView.text = trip.opinion ?: ""

        trip.headerImage?.let {
            val headerBitmap = convertBase64ToBitmap(it)
            if (headerBitmap != null) {

                val heightInPx = convertDpToPx(360f)
                val widthInPx = ViewGroup.LayoutParams.MATCH_PARENT

                headerImageView.layoutParams.height = heightInPx
                headerImageView.layoutParams.width = widthInPx

                headerImageView.setImageBitmap(headerBitmap)
            }
        }

        loadGalleryImages(trip.galleryImages ?: listOf())
    }

    private fun convertBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            val decodedByte = ByteArrayInputStream(decodedString)
            BitmapFactory.decodeStream(decodedByte)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadGalleryImages(galleryImagesBase64: List<String>) {
        val imageViews = listOf(
            view?.findViewById(R.id.listImage1),
            view?.findViewById(R.id.listImage2),
            view?.findViewById(R.id.listImage3),
            view?.findViewById(R.id.listImage4),
            view?.findViewById<ImageView>(R.id.listImage5)
        )


        for (i in galleryImagesBase64.indices) {
            if (i < imageViews.size) {
                val bitmap = convertBase64ToBitmap(galleryImagesBase64[i])
                bitmap?.let {
                    imageViews[i]?.setImageBitmap(bitmap)
                }
            } else {
                break
            }
        }
    }

    private fun convertDpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
