package com.gardenermyo.kotlin_image_upload_3


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gardenermyo.kotlin_image_upload_3.model.UserInfos


import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_image.view.*

class ImageAdapter(var userinfolist : ArrayList<UserInfos> = ArrayList()): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {



    private var clickListener: ClickListener?=null

fun setOnClickListener(clickListener: ClickListener){
    this.clickListener = clickListener
}

    fun updateData(datalist :ArrayList<UserInfos>){
       this.userinfolist== datalist
        notifyDataSetChanged()
   }

    inner class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),View.OnClickListener{

       init {
           itemView.setOnClickListener(this)
       }
        lateinit var userinfo: UserInfos

        fun bind(userinfo: UserInfos){
            this.userinfo= userinfo
            itemView.item_name.text = userinfo.name
            itemView.item_price.text = userinfo.price.toString()
            itemView.item_qrd.text = userinfo.qity.toString()

            Picasso.get()
                .load(userinfo.imageUrl)
                .placeholder(R.drawable.image_24)
                .into(itemView.item_image_view)

        }

        override fun onClick(v: View?) {
            clickListener?.onClick(userinfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_image,parent,false))
    }

    override fun getItemCount(): Int {
        return userinfolist.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
       holder.bind(userinfolist[position])

    }
    interface ClickListener{
        fun onClick(userinfos: UserInfos)
    }

}