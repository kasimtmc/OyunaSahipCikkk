package com.kasimtmc.oyunasahipcik


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import com.kasimtmc.oyunasahipcik.databinding.ActivityEditBinding
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random
import kotlin.random.nextInt


class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    var dx = 0f
    var dy = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityEditBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view= binding.root
        setContentView(view)

        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val imageSuffix= intent.getStringExtra("imagepath")
        val myFile= "${root}/OyunaSahipCik/${imageSuffix}"
        val imageUri: Uri= Uri.parse(myFile)
        println(myFile)
        binding.imageView.setImageURI(imageUri)
        binding.mhrView.setImageResource(R.drawable.tercih)

        binding.mhrView.setOnTouchListener(setMyViewListener())

        val random= Random.nextInt(0..360)
        binding.mhrView.rotation= random.toFloat()

        val seekBar= binding.seekBar
        val viewToResize= binding.mhrView
        seekBar.progress= 50

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                viewToResize.layoutParams.width = progress*2
                viewToResize.layoutParams.height = progress*2
                viewToResize.requestLayout()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // dokunma başlayınca
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // dokunma bitince
            }
        })

        binding.yesButton.setOnClickListener {
            binding.mhrView.setImageResource(R.drawable.evet)
        }
        binding.chsButton.setOnClickListener {
            binding.mhrView.setImageResource(R.drawable.tercih)
        }

        binding.shareButton.setOnClickListener {
            var file= File(myFile)

            try {
                val fileOutputStream= FileOutputStream(file)
                val bitmapImage= binding.imageLayout.drawToBitmap()
                bitmapImage.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Toast.makeText(this,"Dosyanız 'Pictures/OyunaSahipCik' konumuna kaydedildi!",Toast.LENGTH_SHORT).show()
            val uri= FileProvider.getUriForFile(this, "com.kasimtmc.oyunasahipcik.fileprovider", file)
            val intent= Intent(Intent.ACTION_SEND)
            intent.type= "image/jpg"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Paylaş"))
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setMyViewListener(): View.OnTouchListener {
        return View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = view.x - event.rawX
                    dx = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> view.animate()
                    .x(event.rawX + dx)
                    .y(event.rawY + dy)
                    .setDuration(0)
                    .start()
            }
            true
        }
    }

}