package com.blaise.blaise_app.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.health.connect.datatypes.Device
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blaise.blaise_app.R
import com.blaise.blaise_app.ui.bluetoothDevice.adapter.BluetoothDeviceAdapter
import com.blaise.blaise_app.ui.bluetoothDevice.model.BluetoothDeviceModel
import com.blaise.blaise_app.ui.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("DEPRECATION")
class ScanActivity : AppCompatActivity() {

    // Gestion du Bluetooth
// L'Adapter permettant de se connecter
    private var bluetoothAdapter: BluetoothAdapter? = null

    // La connexion actuellement établie
    private var currentBluetoothGatt: BluetoothGatt? = null

    // « Interface système nous permettant de scanner »
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    // Parametrage du scan BLE
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    // On ne retourne que les « Devices » proposant le bon UUID
    private var scanFilters: List<ScanFilter> = arrayListOf(
//  ScanFilter.Builder().setServiceUuid(ParcelUuid(BluetoothLEManager.DEVICE_UUID)).build()
    )

    // Variable de fonctionnement
    private var mScanning = false
    private val handler = Handler(Looper.getMainLooper())

    // DataSource de notre adapter.
    private val bleDevicesFoundList = arrayListOf<Device>()

    private val PERMISSION_REQUEST_LOCATION = 99


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

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

        if(!hasPermission()){
            askForPermission()
        }
        val bouttonRetour: Button = findViewById(R.id.bouttonRetourScan)
        bouttonRetour.setOnClickListener {
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && locationServiceEnabled()) {
                // Permission OK & service de localisation actif => Nous pouvons lancer l'initialisation du BLE.
                // En appelant la méthode setupBLE(), La méthode setupBLE() va initialiser le BluetoothAdapter et lancera le scan.
            } else if (!locationServiceEnabled()) {
                // Inviter à activer la localisation
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                // Permission KO => Gérer le cas.
                // Vous devez ici modifier le code pour gérer le cas d'erreur (permission refusé)
                // Avec par exemple une Dialog

                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.dialogPermissionTittle))
                    .setMessage(resources.getString(R.string.dialogPermissionText))
                    .setNegativeButton(resources.getString(R.string.dialogPermissionDecline)) { dialog, which ->
                        finish()
                    }
                    .setPositiveButton(resources.getString(R.string.dialogPermissionAccept)) { dialog, which ->
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", packageName, null) })
                    }
                    .show()


            }
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_LOCATION)
        }
    }
    private fun locationServiceEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This is Deprecated in API 28
            val mode = Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }


    val registerForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // Le Bluetooth est activé, on lance le scan
            scanLeDevice()
        } else {
            // Bluetooth non activé, vous DEVEZ gérer ce cas autrement qu'avec un Toast.
            Toast.makeText(this, "Bluetooth non activé", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupBLE() {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?)?.let { bluetoothManager ->
            bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null && !bluetoothManager.adapter.isEnabled) {
                registerForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                scanLeDevice()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(scanPeriod: Long = 10000) {
        if (!mScanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

            // On vide la liste qui contient les devices actuellement trouvés
            bleDevicesFoundList.clear()

            // Évite de scanner en double
            mScanning = true

            // On lance une tache qui durera « scanPeriod » à savoir donc de base
            // 10 secondes
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                Toast.makeText(this, getString(R.string.scan_ended), Toast.LENGTH_SHORT).show()
            }, scanPeriod)

            // On lance le scan
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // C'est ici que nous allons créer notre « Device » et l'ajouter dans la dataSource de notre RecyclerView

            // val device = Device(result.device.name, result.device.address, result.device)
            // if (!device.name.isNullOrBlank() && !bleDevicesFoundList.contains(device)) {
            //     bleDevicesFoundList.add(device)
            //     Indique à l'adapter que nous avons ajouté un élément, il va donc se mettre à jour
            //     findViewById<RecyclerView>(R.id.rvDevices).adapter?.notifyItemInserted(bleDevicesFoundList.size - 1)
            // }
        }
    }
    }


