package com.example.travel_application

import Trip
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream

class ViewTripFragment : Fragment() {

    private lateinit var headerImageView: ImageView
    private lateinit var galleryLayout: LinearLayout
    private lateinit var cityTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var dateFromTextView: TextView
    private lateinit var dateToTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var tipsTextView: TextView
    private lateinit var opinionTextView: TextView

    private lateinit var tripId: String
    private lateinit var trip: Trip

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
        profileImageView = view.findViewById(R.id.profil_image_view)
        tipsTextView = view.findViewById(R.id.list_tips_id)
        opinionTextView = view.findViewById(R.id.list_opinion_id)

        cityTextView.visibility = View.VISIBLE
        countryTextView.visibility = View.VISIBLE
        dateFromTextView.visibility = View.VISIBLE
        dateToTextView.visibility = View.VISIBLE
        ratingTextView.visibility = View.VISIBLE
        profileImageView.visibility = View.VISIBLE
        tipsTextView.visibility = View.VISIBLE
        opinionTextView.visibility = View.VISIBLE

        tripId = arguments?.getString("tripId") ?: ""

        if (tripId.isNotEmpty()) {
            fetchTripDataFromFirestore()
        } else {
            Log.w("ViewTripFragment", "Trip ID is empty")
        }

        return view
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

    private fun updateUI() {


        cityTextView.text = trip.city ?: "Unknown city"
        countryTextView.text = trip.country ?: "Unknown country"
        dateFromTextView.text = trip.dateFrom ?: "No date"
        dateToTextView.text = trip.dateTo ?: "No date"
        ratingTextView.text = trip.rating?.toString() ?: "No rating"
        tipsTextView.text = trip.tips ?: "No tips"
        opinionTextView.text = trip.opinion ?: "No opinion"

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
            view?.findViewById<ImageView>(R.id.listImage1),
            view?.findViewById<ImageView>(R.id.listImage2),
            view?.findViewById<ImageView>(R.id.listImage3),
            view?.findViewById<ImageView>(R.id.listImage4),
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
