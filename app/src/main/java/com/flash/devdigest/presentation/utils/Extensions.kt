package com.flash.devdigest.presentation.utils

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView

fun RecyclerView.fastSmoothScrollToTop() {

    val layoutManager = this.layoutManager as? LinearLayoutManager ?: return

    val firstVisible = layoutManager.findFirstVisibleItemPosition()

    // If very far from top, jump closer first
    if (firstVisible > 50) {
        scrollToPosition(50)
    }

    val smoothScroller = object : LinearSmoothScroller(context) {

        override fun getVerticalSnapPreference(): Int = SNAP_TO_START

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return 10f / displayMetrics.densityDpi   // faster scroll
        }
    }

    smoothScroller.targetPosition = 0
    layoutManager.startSmoothScroll(smoothScroller)
}

fun View.showCenteredSnackBar(message: String) {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)

    val textView = snackBar.view.findViewById<MaterialTextView>(
        com.google.android.material.R.id.snackbar_text
    )
    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    textView.gravity = android.view.Gravity.CENTER

    snackBar.show()
}