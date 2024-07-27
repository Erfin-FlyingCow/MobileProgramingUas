package com.example.projectuas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter
    private lateinit var listlokasi: MutableList<ListHead>
    private lateinit var databaseReference: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.Tempatview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        listlokasi = mutableListOf()
        adapter = ListAdapter(listlokasi)
        recyclerView.adapter = adapter

        // Get current user and set up Firebase database reference
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid).child("listHeads")

            // Retrieve data from Firebase
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listlokasi.clear()
                    for (postSnapshot in snapshot.children) {
                        val listHead = postSnapshot.getValue(ListHead::class.java)
                        if (listHead != null) {
                            listlokasi.add(listHead)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
