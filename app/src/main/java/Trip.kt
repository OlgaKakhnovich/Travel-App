import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude

data class Trip(
    var city: String = "",
    var countryCode: String = "",
    var dateFrom: String = "",
    var dateTo: String = "",
    var rating: Double = 0.0,
    var headerImage: String? = null,
    var userId: String? = null,
    var tips: String? = null,
    var opinion: String? = null,
    val imageBase64: String? = null,
    val galleryImages: List<String>? = null,

    @get:Exclude var id: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        city = parcel.readString() ?: "",
        countryCode = parcel.readString() ?: "",
        dateFrom = parcel.readString() ?: "",
        dateTo = parcel.readString() ?: "",
        rating = parcel.readDouble(),
        headerImage = parcel.readString(),
        userId = parcel.readString(),
        tips = parcel.readString(),
        opinion = parcel.readString(),
        imageBase64 = parcel.readString(),
        galleryImages = parcel.createStringArrayList(),
        id = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(city)
        parcel.writeString(countryCode)
        parcel.writeString(dateFrom)
        parcel.writeString(dateTo)
        parcel.writeDouble(rating)
        parcel.writeString(headerImage)
        parcel.writeString(userId)
        parcel.writeString(tips)
        parcel.writeString(opinion)
        parcel.writeString(imageBase64)
        parcel.writeStringList(galleryImages)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Trip> {
        override fun createFromParcel(parcel: Parcel): Trip {
            return Trip(parcel)
        }

        override fun newArray(size: Int): Array<Trip?> {
            return arrayOfNulls(size)
        }
    }
}
