package com.example.pestaside

import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*;
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var mClassifier: Classifier
    private lateinit var mBitmap: Bitmap

    private val mCameraRequestCode = 0
    private val mGalleryRequestCode = 2

    private val mInputSize = 224
    private val mModelPath = "model.tflite"
    private val mLabelPath = "labels.txt"
    private val mSamplePath = "icon.png"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)

        resources.assets.open(mSamplePath).use {
            mBitmap = BitmapFactory.decodeStream(it)
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
            mPhotoImageView.setImageBitmap(mBitmap)
        }

        mCameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, mCameraRequestCode)
        }

        mGalleryButton.setOnClickListener {
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            startActivityForResult(callGalleryIntent, mGalleryRequestCode)
        }

        mDetectButton.setOnClickListener {
            val results = mClassifier.recognizeImage(mBitmap).firstOrNull()
            mResultTextView.text = results?.title

            if (mResultTextView.text == "Not Pest") {
                val dialogBinding = layoutInflater.inflate(R.layout.activity_error_screen, null)

                val myDialog = Dialog(this)
                myDialog.setContentView(dialogBinding)

                myDialog.setCancelable(true)
                myDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog.show()

                val okbtn = dialogBinding.findViewById<Button>(R.id.mErrorButton)
                okbtn.setOnClickListener {
                    myDialog.dismiss()
                }
            }
        }

        mRecommendButton.setOnClickListener {
            if (mResultTextView.text == "Insect") {
                startActivity(Intent(this, InsecticideScreen::class.java))
            }
            else if (mResultTextView.text == "Rodent"){
                startActivity(Intent(this, RodenticideScreen::class.java))
            }
            else if (mResultTextView.text == "Snail"){
                startActivity(Intent(this, MolluscicideScreen::class.java))
            }
            else if (mResultTextView.text == "Spider"){
                startActivity(Intent(this, AcaricideScreen::class.java))
            }
            else if (mResultTextView.text == "Bird"){
                startActivity(Intent(this, AvicideScreen::class.java))
            }
            else {
                val dialogBinding = layoutInflater.inflate(R.layout.activity_error_screen, null)

                val myDialog = Dialog(this)
                myDialog.setContentView(dialogBinding)

                myDialog.setCancelable(true)
                myDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog.show()

                val okbtn = dialogBinding.findViewById<Button>(R.id.mErrorButton)
                okbtn.setOnClickListener {
                    myDialog.dismiss()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == mCameraRequestCode){

            if(resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = data.extras!!.get("data") as Bitmap
                mBitmap = scaleImage(mBitmap)
                val toast = Toast.makeText(this, ("Image crop to: w= ${mBitmap.width} h= ${mBitmap.height}"), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                toast.show()
                mPhotoImageView.setImageBitmap(mBitmap)
                mResultTextView.text= ""
            } else {
                Toast.makeText(this, "Camera cancel..", Toast.LENGTH_LONG).show()
            }
        } else if(requestCode == mGalleryRequestCode) {
            if (data != null) {
                val uri = data.data

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                println("Success!!!")
                mBitmap = scaleImage(mBitmap)
                mPhotoImageView.setImageBitmap(mBitmap)
            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show()
        }
    }
    
    fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }
}

