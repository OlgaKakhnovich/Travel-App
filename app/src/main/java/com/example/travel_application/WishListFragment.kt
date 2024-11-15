package com.example.travel_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_application.databinding.FragmentWishListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WishListFragment : Fragment() {

    private lateinit var recview: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var binding: FragmentWishListBinding
    private lateinit var dataList: ArrayList<Model>
    private lateinit var db: FirebaseFirestore
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWishListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recview = binding.listTravel
        recview.layoutManager= LinearLayoutManager(requireContext())
        dataList= ArrayList()
        adapter = MyAdapter(dataList)
        recview.adapter = adapter
        db = FirebaseFirestore.getInstance()
        db.collection("user").document(userId).collection("wish").get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents
                for(document in list){
                    val obj = document.toObject(Model::class.java)
                    obj?.let{
                        it.documentId = document.id
                        dataList.add(it)
                    }
                    dataList.reverse()
                    adapter.notifyDataSetChanged()
                }
            }
    }

    companion object {
        fun  newInstance(): WishListFragment{
            return  WishListFragment()
        }

    }
}