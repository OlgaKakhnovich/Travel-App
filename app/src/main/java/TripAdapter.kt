import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_application.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.io.ByteArrayInputStream

class TripAdapter(options: FirestoreRecyclerOptions<Trip>) :
    FirestoreRecyclerAdapter<Trip, TripAdapter.TripViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int, model: Trip) {
        holder.bind(model)
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityTextView: TextView = itemView.findViewById(R.id.list_city_id)
        private val countryTextView: TextView = itemView.findViewById(R.id.list_country_id)
        private val dateFromTextView: TextView = itemView.findViewById(R.id.date_from_id)
        private val dateToTextView: TextView = itemView.findViewById(R.id.date_to_id)
        private val ratingTextView: TextView = itemView.findViewById(R.id.star_id)
        private val imageView: ImageView = itemView.findViewById(R.id.listImage)

        fun bind(trip: Trip) {
            cityTextView.text = trip.city
            countryTextView.text = trip.country
            dateFromTextView.text = trip.dateFrom
            dateToTextView.text = trip.dateTo
            ratingTextView.text = trip.rating.toString()


            if (!trip.imageBase64.isNullOrEmpty()) {
                try {

                    val imageBytes = Base64.decode(trip.imageBase64, Base64.DEFAULT)
                    val inputStream = ByteArrayInputStream(imageBytes)
                    val bitmap = BitmapFactory.decodeStream(inputStream)


                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {

                    imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else if (!trip.imageUrl.isNullOrEmpty()) {

                Glide.with(itemView.context)
                    .load(trip.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imageView)
            } else {

                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
}
