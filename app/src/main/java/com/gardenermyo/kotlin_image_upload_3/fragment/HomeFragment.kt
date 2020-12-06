package com.gardenermyo.kotlin_image_upload_3.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gardenermyo.kotlin_image_upload_3.ImageAdapter
import com.gardenermyo.kotlin_image_upload_3.R
import com.gardenermyo.kotlin_image_upload_3.model.UserInfos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() ,ImageAdapter.ClickListener{

    private  var dbReference: DatabaseReference ?= null
    private var currentFirebaseUser:FirebaseUser?= null
    lateinit var imageAdapter: ImageAdapter

    var boo: Boolean = false

    companion object{
        var infoList :ArrayList<UserInfos> = ArrayList()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
      hideSoftKeyboard(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



floatingActionButton.setOnClickListener {
    var action = HomeFragmentDirections.actionHomeFragmentToAddFragment()
    findNavController().navigate(action)
  }

        boo = true
        infoList.clear()
        dbReference= FirebaseDatabase.getInstance().getReference("userInfo")
        currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        imageAdapter = ImageAdapter(infoList)

        imageAdapter.setOnClickListener(this)

        recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter= imageAdapter
        }


        dbReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (boo) {
                    Log.d("ItemListFb>>", "work")
                    val count = snapshot.child(currentFirebaseUser!!.uid).child("itemCount").getValue().hashCode()
                    var countItem = (count.toString().toInt()) - 1
                    for (dataCount in 0..countItem) {
                        val key= snapshot.child(currentFirebaseUser!!.uid).child("Data").child(
                            dataCount.toString()).child("key").getValue()
                        val name =
                            snapshot.child(currentFirebaseUser!!.uid).child("Data").child(
                                dataCount.toString()
                            ).child("name").getValue()
                        val price =
                            snapshot.child(currentFirebaseUser!!.uid).child("Data").child(
                                dataCount.toString()
                            ).child("price").getValue()
                        val qty =
                            snapshot.child(currentFirebaseUser!!.uid).child("Data").child(
                                dataCount.toString()
                            ).child("qity").getValue()
                        val imgUrl =
                            snapshot.child(currentFirebaseUser!!.uid).child("Data").child(
                                dataCount.toString()
                            ).child("imageUrl").getValue()
                        Log.d(
                            "ItemList>>",
                            "$imgUrl" + name + " " + price.toString() + " " + qty.toString()
                        )
                        if (name != null && price != null) {
                            infoList.add(
                                UserInfos(
                                    key = key.toString().toInt(),
                                    imageUrl = imgUrl.toString(),
                                    name = name.toString(),
                                    price = price.toString().toInt(),
                                    qity = qty.toString().toInt())
                            )
                        }
                    }
                    if (!infoList.isEmpty()) {
                        if (txtNoItemDataLabel != null) {
                            txtNoItemDataLabel.visibility = View.INVISIBLE
                        }
                            imageAdapter.updateData(infoList)
                            boo = false

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
        findNavController().popBackStack()
    }

    fun hideSoftKeyboard(mFragment: Fragment?) {
        try {
            if (mFragment == null || mFragment.activity == null) {
                return
            }
            val view = mFragment.requireActivity().currentFocus
            if (view != null) {
                val inputManager = mFragment.requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(userinfos: UserInfos) {
        val index = infoList.indexOf(UserInfos(userinfos.key,userinfos.imageUrl,userinfos.name,userinfos.price,userinfos.qity))

        var action = HomeFragmentDirections.actionHomeFragmentToEditFragment(userinfos.imageUrl,userinfos.name,userinfos.price,userinfos.qity,userinfos.key,index)
        findNavController().navigate(action)
            }
}