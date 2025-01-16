import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_application.Place
import com.example.travel_application.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class PlaceAdapter(private val places: List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(place: Place) {
            bindPlaceData(place, itemView)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]

        val db = FirebaseFirestore.getInstance()
        place.userId.let { userId ->
            db.collection("user").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        place.username = document.getString("username").toString()
                        place.profileImage = document.getString("profileImage").toString()

                        holder.bind(place)
                    }
                }
                .addOnFailureListener {

                    holder.bind(place)
                }
        }
    }

    override fun getItemCount(): Int = places.size
}

fun bindPlaceData(place: Place, view: View) {
    val cityTextView = view.findViewById<TextView>(R.id.post_city)
    val countryTextView = view.findViewById<TextView>(R.id.post_country)
    val dateFromTextView = view.findViewById<TextView>(R.id.date_from_view)
    val dateToTextView = view.findViewById<TextView>(R.id.date_to_view)
    val opinionTextView = view.findViewById<TextView>(R.id.list_opinion_id)
    val ratingTextView = view.findViewById<TextView>(R.id.star_id_view)
    val headerImageView = view.findViewById<ImageView>(R.id.headerImage)
    val profileImageView = view.findViewById<ImageView>(R.id.photo_who_post)
    val usernameTextView = view.findViewById<TextView>(R.id.change_img_text)
    
    val saveToWish = view.findViewById<ImageButton>(R.id.saveforlaterButton1)
    val savedWish = view.findViewById<ImageButton>(R.id.saveforlaterButton2)

    if(place.isSaved){
        savedWish.visibility=View.VISIBLE
        saveToWish.isEnabled = false
    }
    
    saveToWish.setOnClickListener{
        saveDataToWish(place.city, place.countryCode, place.latitude, place.longitude, savedWish, saveToWish, place)
        
    }


    usernameTextView.text = place.username
    cityTextView.text = place.city
    countryTextView.text = place.countryCode
    dateFromTextView.text = place.dateFrom
    dateToTextView.text = place.dateTo
    opinionTextView.text = place.opinion
    ratingTextView.text = place.rating.toString()


    place.profileImage.let { base64Image ->
        val bitmap = base64ToBitmap(base64Image)
        bitmap?.let { profileImageView.setImageBitmap(it) }
    }

    place.headerImage?.let { base64Image ->
        val bitmap = base64ToBitmap(base64Image)
        bitmap?.let { headerImageView.setImageBitmap(it) }
    }

    val galleryImageViews = listOf(
        view.findViewById(R.id.listImage1),
        view.findViewById(R.id.listImage2),
        view.findViewById(R.id.listImage3),
        view.findViewById(R.id.listImage4),
        view.findViewById(R.id.listImage5),
        view.findViewById<ShapeableImageView>(R.id.listImage6)
    )

    val allImages = mutableListOf<String>()
    place.headerImage?.let { allImages.add(it) }
    allImages.addAll(place.galleryImages)

    galleryImageViews.forEachIndexed { index, imageView ->
        if (index < allImages.size) {
            val base64Image = allImages[index]
            val bitmap = base64ToBitmap(base64Image)
            bitmap?.let {
                imageView.setImageBitmap(it)
                imageView.visibility = View.VISIBLE

                imageView.setOnClickListener {
                    val drawable = imageView.drawable
                    if (drawable is BitmapDrawable) {
                        val bitmapFromThumbnail = drawable.bitmap
                        headerImageView.setImageBitmap(bitmapFromThumbnail)
                    }
                }
            }
        } else {
            imageView.visibility = View.GONE
        }
    }
}

fun saveDataToWish(
    city: String,
    countryCode: String,
    latitude: Double,
    longitude: Double,
    savedWish: ImageButton,
    saveToWish: ImageButton,
    place: Place
) {
     val userId = FirebaseAuth.getInstance().currentUser!!.uid
     var db = Firebase.firestore
    val wishRef = db.collection("user").document(userId).collection("wish")
    val wishMap = hashMapOf(
        "city" to city,
        "countryName" to countryCode,
        "latitude" to latitude,
        "longitude" to longitude
    )

    wishRef.add(wishMap)
        .addOnSuccessListener {
            savedWish.visibility = View.VISIBLE
            saveToWish.isEnabled = false
            place.isSaved=true
        }
        .addOnFailureListener { exception ->
            Log.e("", exception.toString())
        }

}


fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}