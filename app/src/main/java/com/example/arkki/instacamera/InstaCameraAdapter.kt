package com.example.arkki.instacamera


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_insta_camera.view.*
import org.tensorflow.lite.examples.classification.R

class InstaCameraAdapter(private val itemList: List<Bird>,private val instaCameraActivity: InstaCameraActivity): RecyclerView.Adapter<InstaCameraAdapter.CustomViewHolder>() {

    class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.recycler_insta_camera, parent, false)
        return CustomViewHolder(cellForRow)
    }

    //numberOfItems
    override fun getItemCount(): Int {
        return itemList.count()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int){
        holder.view.textView?.text = itemList[position].name
        holder.view.imageView2?.setImageResource(itemList[position].img)

        holder.itemView.setOnClickListener {
            //Log.d("qwerty",position.toString())
            if(position == 0){instaCameraActivity.applyImage(R.drawable.bird_temp)}
            if(position == 1){instaCameraActivity.applyImage(R.drawable.chiken)}
            if(position == 2){instaCameraActivity.applyImage(R.drawable.bird_temp)}
            if(position == 3){instaCameraActivity.applyImage(R.drawable.chiken)}
        }
    }
}