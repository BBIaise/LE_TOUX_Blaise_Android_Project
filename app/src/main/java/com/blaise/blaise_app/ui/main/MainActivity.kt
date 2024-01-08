package com.blaise.blaise_app.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.blaise.blaise_app.R
import com.blaise.blaise_app.ui.scan.ScanActivity
import com.blaise.blaise_app.ui.settings.SettingsActivity


class MainActivity : AppCompatActivity() {

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView_DroiteLicorneFete = findViewById<ImageView>(R.id.imageView_DroiteLicorneFete)
        val imageView_DroiteLicornePleurante = findViewById<ImageView>(R.id.imageView_DroiteLicornePleurante)
        val imageView_DroiteLicorneBavante = findViewById<ImageView>(R.id.imageView_DroiteLicorneBavante)
        val imageView_GaucheLicorneBanger = findViewById<ImageView>(R.id.imageView_GaucheLicorneBanger)
        val imageView_GaucheLicorneAlcoolique = findViewById<ImageView>(R.id.imageView_GaucheLicorneAlcoolique)
        val imageView_GaucheLicorneInterrogé = findViewById<ImageView>(R.id.imageView_GaucheLicorneInterrogé)

        imageView_DroiteLicorneBavante.setOnClickListener{
            if(imageView_DroiteLicorneBavante.visibility == View.VISIBLE){
                imageView_DroiteLicorneBavante.visibility = View.INVISIBLE
                imageView_DroiteLicorneFete.visibility = View.VISIBLE
            }
            else if(imageView_DroiteLicorneFete.visibility == View.VISIBLE){
                imageView_DroiteLicorneFete.visibility = View.INVISIBLE
                imageView_DroiteLicornePleurante.visibility = View.VISIBLE
            }
            else if(imageView_DroiteLicornePleurante.visibility == View.VISIBLE){
                imageView_DroiteLicornePleurante.visibility = View.INVISIBLE
                imageView_DroiteLicorneBavante.visibility = View.VISIBLE
            }
        }

        imageView_GaucheLicorneBanger.setOnClickListener{
            if(imageView_GaucheLicorneBanger.visibility == View.VISIBLE){
                imageView_GaucheLicorneBanger.visibility = View.INVISIBLE
                imageView_GaucheLicorneAlcoolique.visibility = View.VISIBLE
            }
            else if(imageView_GaucheLicorneAlcoolique.visibility == View.VISIBLE){
                imageView_GaucheLicorneAlcoolique.visibility = View.INVISIBLE
                imageView_GaucheLicorneInterrogé.visibility = View.VISIBLE
            }
            else if(imageView_GaucheLicorneInterrogé.visibility == View.VISIBLE){
                imageView_GaucheLicorneInterrogé.visibility = View.INVISIBLE
                imageView_GaucheLicorneBanger.visibility = View.VISIBLE
            }
        }

        val buttonScan: Button = findViewById(R.id.buttonScan)
        buttonScan.setOnClickListener {
            startBluetoothScanActivity()
        }

        val bouttonSettings: ImageButton = findViewById(R.id.bouttonSettings)
        bouttonSettings.setOnClickListener {
            startSettingsActivity()
        }
    }

    private fun startBluetoothScanActivity() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
