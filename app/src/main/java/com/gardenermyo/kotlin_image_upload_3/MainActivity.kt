package com.gardenermyo.kotlin_image_upload_3

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var filepath : Uri?=  null
    internal var storageReserence :StorageReference? = null
    private val PICK_IMAGE_REQUEST= 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storageReserence= FirebaseStorage.getInstance()!!.reference

       select_image.setOnClickListener {
           showFileChooser()
       }
        btn_upload.setOnClickListener {
uploadFile()
        }
        btn_show_upload.setOnClickListener {
            var storeageRef = storageReserence!!.child("upload_images").child(edt_image_name.text.toString())
            storeageRef.downloadUrl.addOnSuccessListener { url->
                loadPohto(url.toString())
            }
        }
        listFiles()
    }

    private fun showFileChooser() {
        val intent= Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"SELECT PICTURE"),PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK && data!= null && data.data !=null){
            filepath= data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,filepath)
                image_view !! .setImageBitmap(bitmap)
            }catch (e:IOException){
                print(e)
            }
        }
    }

        fun loadPohto(downloadUrl:String){
        Glide.with(this).load(downloadUrl).into(image_view)
    }
    private fun uploadFile() {
        if(filepath != null){
            val progressDialog= ProgressDialog(this)
            progressDialog.setTitle("Uploading......")
            progressDialog.show()
            var timestamp:String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            var filename:String = "IMAGE_"+timestamp+"_png"

            val imageRef= storageReserence!!.child("upload_images/$filename")
            imageRef.putFile(filepath!!)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext,"File uploaded", Toast.LENGTH_LONG).show()

                }
                .addOnFailureListener{
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext,"Fialed", Toast.LENGTH_LONG).show()

                }
                .addOnProgressListener {
                    val progress = 100.0 * it.bytesTransferred/it.totalByteCount
                    progressDialog.setMessage("Uploaded "+progress.toInt()+ "%....")

                }
        }
    }

        private fun listFiles()= CoroutineScope(Dispatchers.IO).launch {
        try {
    var images= storageReserence!!.child("upload_images/").listAll().await()
    val imageUrls= mutableListOf<String>()

    for (image in images!!.items){
        val url = image.downloadUrl.await()
    imageUrls.add(url.toString())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+url)
}
            withContext(Dispatchers.Main){
                val imageAdapter = ImageAdapter(imageUrls)

                recyclerview.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }



        }catch (e:Exception){
           withContext(Dispatchers.Main){
               Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_LONG).show()
           }
        }
    }


}