package com.example.composelayoutlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.composelayoutlab.flexgridlab.FlexBoxLab
import com.example.composelayoutlab.flexgridlab.GridLab
import com.example.composelayoutlab.flexgridlab.LayoutLabApp
import com.example.composelayoutlab.flexgridlab.ResponsiveLab
import com.example.composelayoutlab.ui.theme.ComposeLayoutLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeLayoutLabTheme {
                LayoutLabApp()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun LayoutLabAppPreview() {
    ComposeLayoutLabTheme { LayoutLabApp() }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun FlexBoxLabPreview() {
    ComposeLayoutLabTheme { FlexBoxLab() }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun GridLabPreview() {
    ComposeLayoutLabTheme { GridLab() }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ResponsiveLabPreview() {
    ComposeLayoutLabTheme { ResponsiveLab() }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
@Composable
private fun ResponsiveLabWidePreview() {
    ComposeLayoutLabTheme { ResponsiveLab() }
}
