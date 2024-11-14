package com.example.travel_application

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapter(private val dataList: ArrayList<Model>): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_wish_list_item, parent, false)
        return  MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]

        holder.city.text = currentItem.city.split(" ")[0]
        holder.country.text = currentItem.country.split(" ")[0]

        holder.deleteWish.setOnClickListener{
            val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
            alertDialogBuilder.setTitle("Usunąć żeczenie")
            alertDialogBuilder.setMessage("Czy pewno chcesz usunąć to żeczenie?")
            alertDialogBuilder.setPositiveButton("Tak"){dialogInterface: DialogInterface, i: Int ->

            }
        }

        holder.acceptWish.setOnClickListener{}

    }


    override fun getItemCount(): Int {
        return dataList.size
    }
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val country: TextView = itemView.findViewById(R.id.wishCountry)
        val city: TextView = itemView.findViewById(R.id.wishCity)
        val imageView: ImageView = itemView.findViewById(R.id.wishImage)
        val acceptWish : ImageView = itemView.findViewById(R.id.acceptWish)
        val deleteWish : ImageView = itemView.findViewById(R.id.deleteWish)

    }
}