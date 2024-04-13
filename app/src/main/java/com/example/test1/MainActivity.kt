package com.example.test1

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.direct_share.DirectNetShare
import com.example.test1.ui.theme.Test1Theme


class MainActivity : ComponentActivity() {

    private var isCheck : Boolean = false
    private lateinit var infoTv: TextView
    private val rootManager = RootManager()
    private val groupCreatedListener = DirectNetShare.GroupCreatedListener { ssid, password -> infoTv.text = String.format("SSID : %s\nPassword : %s", ssid, password) }
    private lateinit var share: DirectNetShare
    @RequiresApi(Build.VERSION_CODES.O)
    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        isCheck = isChecked
        if (isChecked) {
            checkWifiAndStart()
        } else {
            stopShare()
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val REQUEST_CODE_PERMISSIONS = 100;

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CHANGE_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permissions
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE_PERMISSIONS
            )
        }
        setContent {
            // Create a vertical linear layout
            // Create a vertical linear layout
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL

            val switchButton = Switch(this)
            switchButton.text = "Switch is OFF"
            switchButton.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                        val REQUEST_LOCATION_PERMISSION = 1001
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
                    }

                    switchButton.text = "Switch is ON"
                    checkWifiAndStart()
                } else {
                    switchButton.text = "Switch is OFF"
                    stopShare()
                }
            }

            val textView = TextView(this)
            textView.text = "Hello, World!"
            infoTv = textView
            layout.addView(switchButton)
            layout.addView(textView)

            setContentView(layout)
        }

//        infoTv = findViewById(R.id.info)
//        var switch : Switch = findViewById(R.id.wifi_switch)
//        switch.setOnCheckedChangeListener(onCheckedChangeListener)


    }

    override fun onDestroy() {
        super.onDestroy()
        stopShare()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkWifiAndStart() {
        if (!Utils.isWifiEnabled(applicationContext)) {
            registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                        val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
                        if (!noConnectivity) {
                            startShare()
                            unregisterReceiver(this)
                        }
                    }
                }
            }, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), RECEIVER_NOT_EXPORTED)
            Utils.enableWifi(applicationContext)
        } else {
            startShare()
        }
    }

    private fun startShare() {
        share = DirectNetShare(this@MainActivity, groupCreatedListener)
        share.start()
//        lifecycleScope.launch {
//            val success = rootManager.dhcpSetup()
//            Log.d("MainActivity", "DHCP setup successful? $success")
//        }
    }

    private fun stopShare() {
        share.stop()
        infoTv.text = ""
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Test1Theme {
        Greeting("Android")
    }

}