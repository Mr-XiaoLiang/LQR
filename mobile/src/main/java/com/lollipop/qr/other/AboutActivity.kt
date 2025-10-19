package com.lollipop.qr.other

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.richtext.RichText
import com.lollipop.base.util.versionName
import com.lollipop.insets.LInsets
import com.lollipop.insets.WindowInsetsType
import com.lollipop.insets.applyWindowInsets
import com.lollipop.pigment.Pigment
import com.lollipop.qr.base.ColorModeActivity
import com.lollipop.qr.databinding.ActivityAboutBinding


class AboutActivity : ColorModeActivity() {

    private val binding: ActivityAboutBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.actionBar.applyWindowInsets { view, snapshot, insets ->
            val padding = snapshot.padding.maxOf(
                LInsets.maxOf(
                    insets, WindowInsetsType.SystemBars,
                    WindowInsetsType.DisplayCutout
                )
            )
            view.setPadding(
                padding.left,
                padding.top,
                padding.right,
                0
            )
        }
        binding.contentGroup.applyWindowInsets { view, snapshot, insets ->
            val padding = snapshot.padding.maxOf(
                LInsets.maxOf(
                    insets, WindowInsetsType.SystemBars,
                    WindowInsetsType.DisplayCutout
                )
            )
            view.setPadding(
                padding.left,
                0,
                padding.right,
                padding.bottom
            )
        }
        binding.versionView.text = versionName()
        bindByBack(binding.backButton)

        val richFlow = RichText.startRichFlow()
        val linkColor = Color.BLUE // PigmentWallpaperCenter.pigment?.secondaryColor ?: Color.BLUE
        Icons.entries.forEachIndexed { index, icons ->
            if (index > 0) {
                richFlow.addInfo(", ")
            }
            richFlow.addClickInfo(icons.value, linkColor, icons.url, ::onLinkClick)
        }
        richFlow.into(binding.copyrightView)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        binding.logoImageView.setBackgroundColor(pigment.secondaryColor)
        binding.logoImageView.imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)
        binding.andrewView.setTextColor(pigment.onBackgroundBody)
        binding.lollipopView.setTextColor(pigment.onBackgroundBody)
        binding.versionView.setTextColor(pigment.onBackgroundBody)
    }

    private fun onLinkClick(link: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private enum class Icons(val value: String, val url: String) {

        Material("material", "https://m3.material.io/styles/icons/overview"),
//        Icons8("icons8", "https://icons8.com")

    }

}