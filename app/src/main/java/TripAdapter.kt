import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_application.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
//import kotlinx.android.synthetic.main.card_trip.view.* // Importuj odpowiednie widoki

class TripAdapter(options: FirestoreRecyclerOptions<Trip>) :
    FirestoreRecyclerAdapter<Trip, TripAdapter.TripViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false) // Ładujemy layout card_trip
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int, model: Trip) {
        holder.bind(model)
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityTextView = itemView.findViewById<TextView>(R.id.list_city_id)
        private val countryTextView = itemView.findViewById<TextView>(R.id.list_country_id)
        private val dateFromTextView = itemView.findViewById<TextView>(R.id.date_from_id)
        private val dateToTextView = itemView.findViewById<TextView>(R.id.date_to_id)
        private val ratingTextView = itemView.findViewById<TextView>(R.id.star_id)

        fun bind(trip: Trip) {
            cityTextView.text = trip.city
            countryTextView.text = trip.country
            dateFromTextView.text = trip.dateFrom
            dateToTextView.text = trip.dateTo
            ratingTextView.text = trip.rating.toString()
        }
    }
}
