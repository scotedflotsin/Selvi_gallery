package com.example.selvigallery

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.selvigallery.Adapter.ZoomableImageView
import com.example.selvigallery.Utils.StatusBarSetup
import java.io.File

class image_viewer : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var images: List<File>
    val statusBarSetup = StatusBarSetup()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_viewer)
        statusBarSetup.setUpStatusBar(this@image_viewer)
        viewPager = findViewById(R.id.viewPager)

        // Get list of image file paths and position from intent
        val paths = intent.getStringArrayListExtra("images") ?: arrayListOf()
        images = paths.map { File(it) }

        viewPager.adapter = ImagePagerAdapter(images)

        val startPos = intent.getIntExtra("position", 0)
        viewPager.setCurrentItem(startPos, false)
    }

    inner class ImagePagerAdapter(private val images: List<File>) :
        RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(val zoomableImageView: ZoomableImageView) :
            RecyclerView.ViewHolder(zoomableImageView)

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): ImageViewHolder {
            val zoomView = ZoomableImageView(parent.context)
            zoomView.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            zoomView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            return ImageViewHolder(zoomView)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val file = images[position]
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            holder.zoomableImageView.setImageBitmap(bitmap)
        }

        override fun getItemCount(): Int = images.size
    }
}
