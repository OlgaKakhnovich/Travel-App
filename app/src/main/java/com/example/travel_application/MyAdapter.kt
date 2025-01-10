package com.example.travel_application

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MyAdapter(private val dataList: ArrayList<Model>): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_wish_list_item, parent, false)
        return  MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]

        holder.city.text =currentItem.city.split(" ")[0]
        holder.countryName.text = nameCodeToName(currentItem.countryName!!.split(" ")[0])


        holder.deleteWish.setOnClickListener{
            val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
            alertDialogBuilder.setTitle("Usunąć żeczenie")
            alertDialogBuilder.setMessage("Czy pewno chcesz usunąć to żeczenie?")
            alertDialogBuilder.setPositiveButton("Tak"){dialogInterface: DialogInterface, i: Int ->
                deleteItemFromDatabase(currentItem.documentId!!)
                dataList.removeAt(position)
                notifyItemRemoved(position)
                dialogInterface.dismiss()
            }
            alertDialogBuilder.setNegativeButton("Nie"){ dialogInterface: DialogInterface, i:Int ->
                dialogInterface.dismiss()
            }
            alertDialogBuilder.show()
        }

        holder.acceptWish.setOnClickListener{
            val fragment: Fragment = AddTripFragment()
            val args = Bundle()
            args.putString("country", currentItem.countryName)
            args.putString("city", currentItem.city)
            args.putDouble("longitude", currentItem.longitude)
            args.putDouble("latitude", currentItem.latitude)
            fragment.arguments = args

            val activity = holder.itemView.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        deleteItemFromDatabase(currentItem.documentId!!)

    }

    private fun deleteItemFromDatabase(documentId: String) {
        db.collection("user").document(userId).collection("wish")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.w(TAG,"Element został usunięty")
            }
            .addOnFailureListener { e->
                Log.w(TAG,"Błąd w usunięciu elementa", e)
            }
    }

    private  fun nameCodeToName(name: String): String{
        val locale = Locale("", name)
        val polishLocale = Locale("pl");
        return locale.getDisplayCountry(polishLocale)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val countryName: TextView = itemView.findViewById(R.id.wishCountry)
        val city: TextView = itemView.findViewById(R.id.wishCity)
        val acceptWish : ImageView = itemView.findViewById(R.id.acceptWish)
        val deleteWish : ImageView = itemView.findViewById(R.id.deleteWish)

    }
}