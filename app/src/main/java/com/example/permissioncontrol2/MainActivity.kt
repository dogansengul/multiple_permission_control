package com.example.permissioncontrol2

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var cameraResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            permissions.entries.forEach {
                val permission = it.key
                val isGranted = it.value
                if(isGranted){
                    if(permission == Manifest.permission.CAMERA){
                        Toast.makeText(this, "Camera permission is granted.",Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Location permission is granted.",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if(permission == Manifest.permission.CAMERA){
                        Toast.makeText(this, "Camera permission is not granted.",Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Location permission is not granted.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    private var permissionRequestCount = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("Permission Request Count", Context.MODE_PRIVATE)
        permissionRequestCount = sharedPreferences.getInt("Permission Request Count", 0)
        val counter = findViewById<TextView>(R.id.counter)
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                permissionRequestCount++
                sharedPreferences.edit().putInt("Permission Request Count", permissionRequestCount).apply()
                counter.text = sharedPreferences.getInt("Permission Request Count", 0).toString()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationaleDialog()
            } else if (permissionRequestCount > 2 && ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))) {
                showSettingsDialog()
            } else {
                cameraResultLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    private fun showRationaleDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Permission Request")
            .setMessage("You have denied camera or location permissions before. Please grant the permissions.")
            .setPositiveButton("Okay", DialogInterface.OnClickListener {
                    dialog, which -> cameraResultLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)) })
        alertDialog.show()
    }

    private fun showSettingsDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Permission Request")
            .setMessage("You have denied camera or location permissions multiple times. Please go to app settings and grant the permissions.")
            .setPositiveButton("Okay", DialogInterface.OnClickListener {
                    dialog, which -> openAppSettings() })
        //alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}