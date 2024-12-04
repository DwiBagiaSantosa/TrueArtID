package com.example.androidstudioprojects.trueartid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidstudioprojects.trueartid.databinding.ActivityClassifyBinding

class ClassifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassifyBinding

    private val mInputSize = 224
    private val mModelPath = "GAN_Counter_ScenarioV5.tflite"
    private val mLabelPath = "label.txt"
    private lateinit var classifier: Classifier

    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var confidenceTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClassifier()

        cameraButton = binding.cameraBtn
        galleryButton = binding.galleryBtn
        imageView = binding.imageView4
        resultTextView = binding.result
        confidenceTextView = binding.confident

        cameraButton.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 3)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }

        galleryButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }
    }

    private fun initClassifier() {
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1){
            var uri = data?.data;
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            imageView.setImageBitmap(bitmap)
            val result = classifier.recognizeImage(bitmap)

            resultTextView.text = result[0].title
//            confidenceTextView.text = result[0].confidence.toString()
            confidenceTextView.text = String.format("%.2f%%", result[0].confidence * 100)
        }

        if (requestCode == 3) {
            val image = data?.extras?.get("data") as Bitmap
            val dimension = image.width.coerceAtMost(image.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
            imageView.setImageBitmap(thumbnail)

            val bitmap = Bitmap.createScaledBitmap(thumbnail, mInputSize, mInputSize, false)
            val result = classifier.recognizeImage(bitmap)

            resultTextView.text = result[0].title
//            confidenceTextView.text = result[0].confidence.toString()
            confidenceTextView.text = String.format("%.2f%%", result[0].confidence * 100)
        }
    }
}