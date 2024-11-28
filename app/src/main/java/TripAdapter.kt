import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_application.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.util.Locale

class TripAdapter(options: FirestoreRecyclerOptions<Trip>) :
    FirestoreRecyclerAdapter<Trip, TripAdapter.TripViewHolder>(options) {

    var onItemClickListener: ((Trip) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int, model: Trip) {

        val documentSnapshot = snapshots.getSnapshot(position)
        model.id = documentSnapshot.id


        holder.bind(model)


        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(model)
        }
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
            countryTextView.text = nameCodeToName(trip.countryCode)
            dateFromTextView.text = trip.dateFrom
            dateToTextView.text = trip.dateTo
            ratingTextView.text = trip.rating.toString()


            setImage(trip.headerImage)
        }

        private  fun nameCodeToName(name: String): String{
            val locale = Locale("", name)
            return locale.displayCountry
        }

        private fun setImage(imageBase64: String?) {
            if (!imageBase64.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("TripAdapter", "Błąd dekodowania obrazu Base64: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}
