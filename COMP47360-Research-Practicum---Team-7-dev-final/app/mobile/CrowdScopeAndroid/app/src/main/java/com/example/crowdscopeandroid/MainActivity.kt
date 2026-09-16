// Primary entry point and defines the main activities of the mobile app

package com.example.crowdscopeandroid

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()


            window.decorView.post {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                rootView?.setOnTouchListener        { v, event ->
                    Log.d("ComposeViewTouchListener", "Root ComposeView Touch Event: Action=${event.action}, X=${event.x}, Y=${event.y}, View=${v.javaClass.simpleName}")
                    false
                }
            }

            // Wrap AppNavHost in a clickable Box to test root Compose touch events
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent) // Ensure it's transparent
                    .clickable {
                        Log.d("RootComposeClick", "Root Compose Box clicked!")
                    }
            ) {
                // Your AppNavHost handles all navigation, Scaffold, and state hoisting.
                AppNavHost(navController = navController)
            }
        }
    }
}
