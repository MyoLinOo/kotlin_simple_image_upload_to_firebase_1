package com.gardenermyo.kotlin_image_upload_3.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.gardenermyo.kotlin_image_upload_3.R
import com.gardenermyo.kotlin_image_upload_3.model.UserInfos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_add.*

import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {
    private lateinit var firebaseDatabase: FirebaseDatabase
   private lateinit var dbReference: DatabaseReference
        private var mAuth:FirebaseAuth ? = null
    private var mProgress:ProgressDialog? = null
    var currentFirebaseUser :FirebaseUser ?= null
    var selectedPhotoUri :Uri ?= null
    private var userId:String= ""
    var boo : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boo = true
        mProgress = ProgressDialog(this.requireContext())
        mAuth = FirebaseAuth.getInstance()
firebaseDatabase = FirebaseDatabase.getInstance()
        dbReference= firebaseDatabase.getReference("userInfo")
        currentFirebaseUser = FirebaseAuth.getInstance().currentUser

    cv_select_image.setOnClickListener {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type="image/*"
    startActivityForResult(intent,0)
}
        btn_save.setOnClickListener {
    if(edt_name.text!!.isNotEmpty() && edt_price.text!!.isNotEmpty() && edt_qty.text!!.isNotEmpty()){
    uploadImageToFirebaseStorage()
             }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("InsertFragment>>","Photo is selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver,selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable( scaleBitMap(bitmap))

            image_review.setImageDrawable(bitmapDrawable)
        }
    }


    private fun scaleBitMap (bm : Bitmap) : Bitmap {
        var width : Int = bm.width
        var height : Int = bm.height

        var bitmap = bm

        Log.d("Pictures", "Width and height are " + width + "--" + height);

        height=600
        width=600

        Log.d("Pictures", "after scaling Width and height are " + width + "--" + height)

        bitmap = Bitmap.createScaledBitmap(bm, width, height, true);

        return bitmap
    }

    private fun uploadImageToFirebaseStorage(){

        mProgress = ProgressDialog(this.requireContext())

        mProgress!!.show()
        if (selectedPhotoUri == null) return
        // random filename
        var timestamp:String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            var filename:String = "IMAGE_"+timestamp+"_png"
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("InsertFragment>>", "Successfully upload image : ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("InsertFragment>>", "File location : $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
              }
            .addOnFailureListener{
                mProgress!!.dismiss()
                Toast.makeText(context,"Fialed", Toast.LENGTH_LONG).show()
            }

            .addOnProgressListener {
                val progress = 100.0 * it.bytesTransferred/it.totalByteCount
                mProgress!!.setMessage("Uploaded "+progress.toInt()+ "%....")
            }
    }

    private fun saveUserToFirebaseDatabase(iteImageUrl: String){
        var itemName:String = edt_name.text.toString()
        var itemPrice: Int = edt_price.text.toString().toInt()
        var itemQty : Int = edt_qty.text.toString().toInt()
        Log.d("test>>",""+itemName +" "+ itemPrice.toString() + " "+itemQty.toString())



        dbReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (boo){

                    val count = snapshot.child(currentFirebaseUser!!.uid).child("itemCount").getValue().hashCode()

                    var itemcount:Int = count.toString().toInt()
                    var userInfo = UserInfos(itemcount,iteImageUrl,itemName,itemPrice,itemQty)

                    dbReference.child(currentFirebaseUser!!.uid).child("Data").child(count.toString()).setValue(userInfo)

                   ++itemcount

                    dbReference.child(currentFirebaseUser!!.uid).child("itemCount").setValue(itemcount)
                    boo =false
                    mProgress!!.dismiss()

                    findNavController().popBackStack()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


//    private fun uploadFile() {
//        if(filepath != null){
//            val progressDialog= ProgressDialog(context)
//            progressDialog.setTitle("Uploading......")
//            progressDialog.show()
//            var timestamp:String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//            var filename:String = "IMAGE_"+timestamp+"_png"
//
//            val imageRef= storageReserence!!.child("upload_images/$filename")
//            imageRef.putFile(filepath!!)
//                .addOnSuccessListener {
//                    progressDialog.dismiss()
//                    Toast.makeText(context,"File uploaded", Toast.LENGTH_LONG).show()
//
//                }
//                .addOnFailureListener{
//                    progressDialog.dismiss()
//                    Toast.makeText(context,"Fialed", Toast.LENGTH_LONG).show()
//                }
//
//                .addOnProgressListener {
//                    val progress = 100.0 * it.bytesTransferred/it.totalByteCount
//                    progressDialog.setMessage("Uploaded "+progress.toInt()+ "%....")
//                }
//        }
//    }

//    private fun listFiles()= CoroutineScope(Dispatchers.IO).launch {
//        try {
//            var images= storageReserence!!.child("upload_images/").listAll().await()
//            val imageUrls= mutableListOf<UserInfo>()
//
//            for (image in images!!.items){
//                val url = image.downloadUrl.await()
//                imageUrls.add(UserInfo(url,"",""))
//
//
//                println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+url)
//            }
//            withContext(Dispatchers.Main){
//                val imageAdapter = ImageAdapter(imageUrls)
//
//                recyclerview.apply {
//                    adapter = imageAdapter
//                    layoutManager = LinearLayoutManager(context)
//                }
//            }
//
//        }catch (e:Exception){
//            withContext(Dispatchers.Main){
//                Toast.makeText(context,e.message,Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
}