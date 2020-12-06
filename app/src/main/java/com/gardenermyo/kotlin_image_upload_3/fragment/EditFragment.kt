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
import androidx.navigation.fragment.navArgs
import com.gardenermyo.kotlin_image_upload_3.EditFragmentArgs
import com.gardenermyo.kotlin_image_upload_3.R
import com.gardenermyo.kotlin_image_upload_3.model.UserInfos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit.*
import java.text.SimpleDateFormat
import java.util.*

class EditFragment : Fragment() {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference
    private var mAuth: FirebaseAuth? = null
    private var mProgress:ProgressDialog? = null
    var currentFirebaseUser : FirebaseUser?= null
    var selectedPhotoUri : Uri?= null
    private var userId:String= ""
    var boo : Boolean = false

    private val args by navArgs<EditFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boo = true
        mProgress = ProgressDialog(this.requireContext())
        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        dbReference= firebaseDatabase.getReference("userInfo")
        currentFirebaseUser = FirebaseAuth.getInstance().currentUser


        var name:String? =args?.name
        var price:Int? = args?.price
        var qity:Int?= args?.qity
        var key:Int? = args?.key
        var index:Int?= args?.index

        update_edt_name.setText(name.toString())
        update_edt_price.setText(price.toString())
        update_edt_qty.setText(qity.toString())

        Picasso.get()
            .load(args?.imageUrl)
            .fit()
            .into(image_review_update)
        var userInfosList= HomeFragment.infoList

        update_cv_select_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }

        update.setOnClickListener {
            mProgress = ProgressDialog(this.requireContext())
            mProgress!!.setMessage("Uploading to Server....")
            mProgress!!.show()
            if (selectedPhotoUri==null){
                saveUserToFirebaseDatabase(args.imageUrl!!,key!!)
            }else{
                uploadImageToFirebaseStorage(key!!)
            }
        }

        delete.setOnClickListener{

            dbReference!!.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (boo){
                        snapshot.child(currentFirebaseUser!!.uid).child("Data").child(key.toString()).ref.removeValue()
                        userInfosList.removeAt(index!!)
                        dbReference!!.child(currentFirebaseUser!!.uid).child("Data").setValue(userInfosList.size)
                        snapshot.child(currentFirebaseUser!!.uid).child("Data").ref.removeValue()
                        dbReference!!.child(currentFirebaseUser!!.uid).child("Data").setValue(userInfosList)
                        boo=false

                        findNavController().popBackStack()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("InsertFragment>>","Photo is selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver,selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable( scaleBitMap(bitmap))

            image_review_update.setImageDrawable(bitmapDrawable)
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

    private fun uploadImageToFirebaseStorage(key:Int){

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
                    saveUserToFirebaseDatabase(it.toString(),key)
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

    private fun saveUserToFirebaseDatabase(iteImageUrl: String,key:Int){

        var itemName:String =update_edt_name.text.toString()
        var itemPrice: Int = update_edt_price.text.toString().toInt()
        var itemQty : Int = update_edt_qty.text.toString().toInt()
        Log.d("test>>",""+itemName +" "+ itemPrice.toString() + " "+itemQty.toString())

        var userInfo = UserInfos(key,iteImageUrl,itemName,itemPrice,itemQty)

        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (boo){

                    dbReference.child(currentFirebaseUser!!.uid).child("Data").child(key.toString()).setValue(userInfo)

                    boo =false
                    mProgress!!.dismiss()

                    findNavController().popBackStack()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}