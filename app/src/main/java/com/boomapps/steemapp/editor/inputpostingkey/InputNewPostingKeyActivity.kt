package com.boomapps.steemapp.editor.inputpostingkey

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.boomapps.steemapp.BaseActivity
import com.boomapps.steemapp.R
import com.boomapps.steemapp.barcode.PointsOverlayView
import com.boomapps.steemapp.help.HelpActivity
import com.boomapps.steemapp.repository.RepositoryProvider
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_input_new_key.*
import kotlinx.android.synthetic.main.activity_signin.*

/**
 * Created by vgrechikha on 14.03.2018.
 */
class InputNewPostingKeyActivity : BaseActivity(), QRCodeReaderView.OnQRCodeReadListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_new_key)
        aNewKey_qreaderStartButton.setOnClickListener { _ ->
            enableQRreader()
        }

        qrInInfo.setOnClickListener({
            openLocalHelpScreen()
            RepositoryProvider.instance.getSharedRepository().setFirstLaunchState(false)
        })
    }

    private fun openLocalHelpScreen() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private fun enableQRreader() {
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
}