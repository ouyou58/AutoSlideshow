package com.example.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var imageUris = arrayListOf<Uri>()
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    private var mTimerSec = 0
    private var next = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            getContentsInfo()
        }



        next_button.setOnClickListener{
            next += 1
            if(next < imageUris.size) {
                imageView.setImageURI(imageUris[next])
            }else if(next == imageUris.size) {
                next  = 0
                imageView.setImageURI(imageUris[next])
            }

        }

        previous_button.setOnClickListener{
            next -= 1
            if(next >= 0) {
                imageView.setImageURI(imageUris[next])
            }else if (next < 0){
                next = imageUris.size-1
                imageView.setImageURI(imageUris[next])

            }
        }


        start_button.setOnClickListener {
            if(start_button.text.toString() == "再生") {
                start_button.text = "停止"
                next_button.isEnabled = false
                previous_button.isEnabled = false
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 1
                        mHandler.post {
                            if(mTimerSec < imageUris.size){
                                imageView.setImageURI(imageUris[mTimerSec])
                            }else if(mTimerSec == imageUris.size) {
                                mTimerSec = 0
                                imageView.setImageURI(imageUris[0])
                            }
                        }
                    }
                }, 2000, 2000)

            }else if(start_button.text.toString() == "停止") {
                start_button.text = "再生"
                next_button.isEnabled = true
                previous_button.isEnabled = true
                mTimer!!.cancel()
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor.moveToFirst()) {
            do {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUris.add(imageUri)
                Log.d("ANDROID", "URI : " + imageUri.toString())
            } while (cursor.moveToNext())
        }
        cursor.close()
        imageView.setImageURI(imageUris[next])
    }
}
