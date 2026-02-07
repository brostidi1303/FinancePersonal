package com.example.financeapp

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.financeapp.nav.MainApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Thiết lập immersive 1 lần

        setupEdgeToEdge()
        setContent {
            MainApp(showAds = {})
        }
    }

    private fun setupEdgeToEdge() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // --- THÊM DÒNG NÀY ĐỂ XỬ LÝ CAMERA ĐỤC LỖ ---  cv
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        // ---------------------------------------------

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            // Bạn đã có dòng này, nó rất quan trọng để người dùng có thể vuốt để xem lại các thanh hệ thống
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

