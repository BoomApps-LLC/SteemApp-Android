package com.boomapps.steemapp.ui.qrreader

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.help.HelpActivity
import com.boomapps.steemapp.repository.RepositoryProvider
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_input_new_key.*


/**
 * Created by Vitali Grechikha on 07.02.2018.
 */
class BarcodeReadActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {


    private var resultTextView: TextView? = null
    private var qrCodeReaderView: QRCodeReaderView? = null

    private var pointsOverlayView: PointsOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_barcode)
        pointsOverlayView = findViewById<PointsOverlayView>(R.id.points_overlay_view)
        resultTextView = findViewById<TextView>(R.id.qrReadResult)
        qrCodeReaderView = findViewById<QRCodeReaderView>(R.id.qrdecoderview);
        qrCodeReaderView?.setOnQRCodeReadListener(this)

        // Use this function to enable/disable decoding
        qrCodeReaderView?.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView?.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrCodeReaderView?.setTorchEnabled(true);

        // Use this function to set front camera preview
//        qrCodeReaderView?.setFrontCamera();

        // Use this function to set back camera preview
        qrCodeReaderView?.setBackCamera();

        findViewById<Button>(R.id.buttonReturnResult).setOnClickListener({
            onBackPressed()
        })

        qrInInfo.setOnClickListener({
            openLocalHelpScreen()
            RepositoryProvider.instance.getSharedRepository().setFirstLaunchState(false)
        })
    }

    private fun openLocalHelpScreen() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    override fun onBackPressed() {
        if (resultTextView!!.text.isNotBlank()) {
            val result: Intent = intent
            result.putExtra("POSTING_KEY", resultTextView?.text?.toString())
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


    override fun onResume() {
        super.onResume()
        qrCodeReaderView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        qrCodeReaderView?.stopCamera()
    }


    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    override fun onQRCodeRead(text: String?, points: Array<PointF>?) {
        resultTextView?.setText(text);
        pointsOverlayView?.points = points
    }
}