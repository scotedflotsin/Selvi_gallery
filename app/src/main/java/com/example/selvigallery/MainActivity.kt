//package com.example.selvigallery
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.View
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.FileProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import com.example.selvigallery.Adapter.GalleryAdapter
//import com.example.selvigallery.Camera.CameraActivity
//import com.example.selvigallery.Model.ImageItem
//import com.example.selvigallery.Utils.StatusBarSetup
//import com.example.selvigallery.databinding.ActivityMainBinding
//import java.io.File
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private val setUpStatusBar = StatusBarSetup()
//
//    private lateinit var adapter: GalleryAdapter
//    private val imageList = mutableListOf<ImageItem>()
//
//    companion object {
//        private const val CAMERA_REQUEST_CODE = 1001
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        setUpStatusBar.setUpStatusBar(this)
//
//        // RecyclerView setup
//        val recyclerView = binding.galleryRecyclerView
//        recyclerView.layoutManager = GridLayoutManager(this, 3)
//        adapter = GalleryAdapter(imageList) { position ->
//            // When image clicked, open image_viewer activity
//            val intent = Intent(this, image_viewer::class.java).apply {
//                putStringArrayListExtra("images", ArrayList(imageList.map { it.file.absolutePath }))
//                putExtra("position", position)
//            }
//            startActivity(intent)
//        }
//        recyclerView.adapter = adapter
//
//        loadImages()
//
//        // Camera button
//        binding.fabCamera.setOnClickListener {
//            launchCamera()
//        }
//
//        // Share button
//        binding.btnShare.setOnClickListener {
//            shareSelectedImages()
//        }
//
//        // Download button
//        binding.btnDownload.setOnClickListener {
//            downloadSelectedImages()
//        }
//    }
//
//    @SuppressLint("PrivateResource")
//    private fun launchCamera() {
//        val intent = Intent(this, CameraActivity::class.java)
//        startActivityForResult(intent, CAMERA_REQUEST_CODE)
//        overridePendingTransition(
//            androidx.appcompat.R.anim.abc_fade_in,
//            androidx.appcompat.R.anim.abc_fade_out
//        )
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == CAMERA_REQUEST_CODE) {
//            val success = loadImages()  // Refresh the image list after taking photos
//        }else{
//
//        }
//    }
//
//    private fun loadImages():Boolean {
//        imageList.clear()
//        val dir = getExternalFilesDir("Photos") ?: return false
//
//        val files = dir.listFiles()?.filter {
//            it.extension.lowercase() in listOf("jpg", "jpeg", "png")
//        } ?: return false
//
//        if (files.isEmpty()) return false
//
//        files.sortedByDescending { it.lastModified() }
//            .forEach { imageList.add(ImageItem(it, isSelected = false)) }
//
//        adapter.notifyDataSetChanged()
//        return true
//    }
//
//
//    private fun shareSelectedImages() {
//        val selectedFiles = imageList.filter { it.isSelected }
//        if (selectedFiles.isEmpty()) {
//            Toast.makeText(this, "Select images to share", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val uris = selectedFiles.map {
//            FileProvider.getUriForFile(
//                this,
//                "$packageName.fileprovider",
//                it.file
//            )
//        }
//
//        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
//            type = "image/*"
//            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        startActivity(Intent.createChooser(shareIntent, "Share images"))
//    }
//
//    private fun downloadSelectedImages() {
//        val downloadDir = File(getExternalFilesDir(null), "Downloads")
//        if (!downloadDir.exists()) {
//            downloadDir.mkdir()
//        }
//
//        val selectedImages = imageList.filter { it.isSelected }
//        if (selectedImages.isEmpty()) {
//            Toast.makeText(this, "Select images to download", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        selectedImages.forEach { item ->
//            val newFile = File(downloadDir, item.file.name)
//            item.file.copyTo(newFile, overwrite = true)
//        }
//
//        Toast.makeText(this, "${selectedImages.size} images downloaded", Toast.LENGTH_SHORT).show()
//    }
//}




package com.example.selvigallery

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.selvigallery.Adapter.GalleryAdapter
import com.example.selvigallery.Camera.CameraActivity
import com.example.selvigallery.Model.ImageItem
import com.example.selvigallery.Utils.StatusBarSetup
import com.example.selvigallery.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val setUpStatusBar = StatusBarSetup()

    private lateinit var adapter: GalleryAdapter
    private val imageList = mutableListOf<ImageItem>()

    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpStatusBar.setUpStatusBar(this)

        // RecyclerView setup
        val recyclerView = binding.galleryRecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = GalleryAdapter(imageList) { position ->
            // When image clicked, open image_viewer activity
            val intent = Intent(this, image_viewer::class.java).apply {
                putStringArrayListExtra("images", ArrayList(imageList.map { it.file.absolutePath }))
                putExtra("position", position)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        toggleGalleryVisibility(loadImages()) // << NEW LINE

        // Camera button
        binding.fabCamera.setOnClickListener {
            launchCamera()
        }

        // Share button
        binding.btnShare.setOnClickListener {
            shareSelectedImages()
        }

        // Download button
        binding.btnDownload.setOnClickListener {
            downloadSelectedImages()
        }
    }

    @SuppressLint("PrivateResource")
    private fun launchCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
        overridePendingTransition(
            androidx.appcompat.R.anim.abc_fade_in,
            androidx.appcompat.R.anim.abc_fade_out
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE) {
            toggleGalleryVisibility(loadImages()) // << NEW LINE
        } else {

        }
    }

    private fun loadImages(): Boolean {
        imageList.clear()
        val dir = getExternalFilesDir("Photos") ?: return false

        val files = dir.listFiles()?.filter {
            it.extension.lowercase() in listOf("jpg", "jpeg", "png")
        } ?: return false

        if (files.isEmpty()) return false

        files.sortedByDescending { it.lastModified() }
            .forEach { imageList.add(ImageItem(it, isSelected = false)) }

        adapter.notifyDataSetChanged()
        return true
    }

    private fun shareSelectedImages() {
        val selectedFiles = imageList.filter { it.isSelected }
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "Select images to share", Toast.LENGTH_SHORT).show()
            return
        }

        val uris = selectedFiles.map {
            FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                it.file
            )
        }

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share images"))
    }

    private fun downloadSelectedImages() {
        val downloadDir = File(getExternalFilesDir(null), "Downloads")
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }

        val selectedImages = imageList.filter { it.isSelected }
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Select images to download", Toast.LENGTH_SHORT).show()
            return
        }

        selectedImages.forEach { item ->
            val newFile = File(downloadDir, item.file.name)
            item.file.copyTo(newFile, overwrite = true)
        }

        Toast.makeText(this, "${selectedImages.size} images downloaded", Toast.LENGTH_SHORT).show()
    }

    // << ADDED METHOD >>
    private fun toggleGalleryVisibility(imagesLoaded: Boolean) {
        binding.defaultMessage.visibility = if (imagesLoaded) View.GONE else View.VISIBLE
        binding.galleryRecyclerView.visibility = if (imagesLoaded) View.VISIBLE else View.GONE
    }
}
