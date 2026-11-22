package com.example.inomasandbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.inomasandbox.`FlexBox-Grid-lab`.FlexBoxLab
import com.example.inomasandbox.`FlexBox-Grid-lab`.GridLab
import com.example.inomasandbox.`FlexBox-Grid-lab`.LayoutLabApp
import com.example.inomasandbox.`FlexBox-Grid-lab`.ResponsiveLab
import com.example.inomasandbox.ui.theme.INomaSandboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            INomaSandboxTheme {
                LayoutLabApp()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun LayoutLabAppPreview() {
    INomaSandboxTheme { LayoutLabApp() }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun FlexBoxLabPreview() {
    INomaSandboxTheme { FlexBoxLab() }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun GridLabPreview() {
    INomaSandboxTheme { GridLab() }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ResponsiveLabPreview() {
    INomaSandboxTheme { ResponsiveLab() }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
@Composable
private fun ResponsiveLabWidePreview() {
    INomaSandboxTheme { ResponsiveLab() }
}
