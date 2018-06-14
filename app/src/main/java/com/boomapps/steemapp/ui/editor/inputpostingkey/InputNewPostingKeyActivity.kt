package com.boomapps.steemapp.ui.editor.inputpostingkey

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.ui.BaseActivity
import com.boomapps.steemapp.ui.help.HelpActivity
import com.boomapps.steemapp.ui.qrreader.PointsOverlayView
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_input_new_key.*

/**
 * Created by vgrechikha on 14.03.2018.
 */
class InputNewPostingKeyActivity : BaseActivity(), QRCodeReaderView.OnQRCodeReadListener {

    private val PERMISSION_REQUEST_CAMERA = 3546

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_new_key)
        aNewKey_qreaderStartButton.setOnClickListener { _ ->
            enableQRreader()
        }

        qrInInfo.setOnClickListener({
            openLocalHelpScreen()
            RepositoryProvider.getPreferencesRepository().setFirstLaunchState(false)
        })
    }

    private fun openLocalHelpScreen() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private fun enableQRreader() {
        if (!hasCameraPermissions()) {
            requestCameraPermission()
            return
        }
        aNewKey_qrdecoderview.visibility = View.VISIBLE
        aNewKey_points_overlay_view.visibility = View.VISIBLE
        aNewKey_qrdecoderview.setOnQRCodeReadListener(this)
        // Use this function to enable/disable decoding
        aNewKey_qrdecoderview.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        aNewKey_qrdecoderview.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        aNewKey_qrdecoderview.setTorchEnabled(false);

        // Use this function to set back camera preview
        aNewKey_qrdecoderview.setBackCamera();

        findViewById<Button>(R.id.aNewKey_buttonReturnResult).setOnClickListener({
            onBackPressed()
        })
        aNewKey_qrdecoderview?.startCamera()
    }


    override fun onQRCodeRead(text: String?, points: Array<PointF>?) {
        aNewKey_signInInputPostingKey?.setText(text);
        var pointsView: PointsOverlayView = aNewKey_points_overlay_view
        pointsView.points = points
    }


    override fun onBackPressed() {
        if (aNewKey_signInInputPostingKey.text.isNotBlank() && aNewKey_signInInputPostingKey.text.length >= 40) {
            val result: Intent = intent
            result.putExtra("POSTING_KEY", aNewKey_signInInputPostingKey.text.toString())
            if (parent == null) {
                setResult(Activity.RESULT_OK, result)
            } else {
                parent.setResult(Activity.RESULT_OK, result)
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }


    private fun hasCameraPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableQRreader()
                } else {
                    Toast.makeText(this@InputNewPostingKeyActivity, "You have to input POSTING KEY manual", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }
}