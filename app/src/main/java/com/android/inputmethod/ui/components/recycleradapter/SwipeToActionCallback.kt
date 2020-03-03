package com.android.inputmethod.ui.components.recycleradapter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


data class SwipeConf(
        val right: SwipeActionConf?,
        val left: SwipeActionConf?
) {
    val swipeDirs: List<Int> = listOfNotNull(
            right?.let { ItemTouchHelper.RIGHT },
            left?.let { ItemTouchHelper.LEFT }
    )

    init {
        require(right != null || left != null) {
            "Swipe Configuration must contain at least one action"
        }
    }
}

data class SwipeActionConf(
        val icon: Drawable,
        val text: String,
        val textPaint: TextPaint,
        val background: ColorDrawable
)

class SwipeActionCallback(swipeConf: SwipeConf) : ItemTouchHelper.SimpleCallback(0, swipeConf.swipeDirs.fold(0, { a, b -> a or b })) {

    private lateinit var leftConf: SwipeActionConf
    private lateinit var rightConf: SwipeActionConf

    init {
        swipeConf.left?.let {
            leftConf = it
        }

        swipeConf.right?.let {
            rightConf = it
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT -> {
                viewHolder.itemId
                viewHolder.adapterPosition
                Log.d("SwipeToActionCallback", "SWIPE LEFT1")

            }
            ItemTouchHelper.RIGHT -> {
                Log.d("SwipeToActionCallback", "SWIPE RIGHT")
            }
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView: View = viewHolder.itemView

        when {
            dX > 0 -> {
                // Swiping to the right
                val drawable = rightConf.icon
                val bounds = drawableRightSwipeBounds(itemView, drawable)
                val margin: Int = (itemView.height - drawable.intrinsicHeight) / 2
                drawable.bounds = bounds

                val canvasRight = dX.toInt() + itemView.left
                rightConf.background.setBounds(itemView.left, itemView.top, canvasRight, itemView.bottom)

                val textY = rightConf.textPaint.yCenteredOn(drawable)
                val textX = bounds.right.toFloat() + margin

                // Clip based on itemView
                c.clipRect(itemView.left, itemView.top, canvasRight, itemView.bottom)
                rightConf.background.draw(c)
                drawable.draw(c)
                c.drawText(rightConf.text, textX, textY, rightConf.textPaint)
            }
            dX < 0 -> {
                // Swiping to the left
                val drawable = leftConf.icon
                val bounds = drawableLeftSwipeBounds(itemView, drawable)
                val margin: Int = (itemView.height - drawable.intrinsicHeight) / 2
                drawable.bounds = bounds

                val canvasLeft = itemView.right + dX.toInt()
                leftConf.background.setBounds(canvasLeft, itemView.top, itemView.right, itemView.bottom)

                val textY = leftConf.textPaint.yCenteredOn(drawable)
                val textX = drawable.bounds.left - leftConf.textPaint.measureText(leftConf.text) - margin

                c.clipRect(canvasLeft, itemView.top, itemView.right, itemView.bottom)
                leftConf.background.draw(c)
                drawable.draw(c)
                c.drawText(leftConf.text, textX, textY, leftConf.textPaint)
            }
            else -> { // view is unSwiped
                c.clipRect(itemView.left, itemView.top, itemView.right, itemView.bottom)
            }
        }
    }


    private fun drawableRightSwipeBounds(itemView: View, drawable: Drawable): Rect {
        val margin: Int = (itemView.height - drawable.intrinsicHeight) / 2
        val top: Int = itemView.top + (itemView.height - drawable.intrinsicHeight) / 2
        val bottom: Int = top + drawable.intrinsicHeight
        val left: Int = itemView.left + margin
        val right: Int = itemView.left + margin + drawable.intrinsicWidth

        return Rect(left, top, right, bottom)
    }

    private fun drawableLeftSwipeBounds(itemView: View, drawable: Drawable): Rect {
        val iconMargin: Int = (itemView.height - drawable.intrinsicHeight) / 2
        val top: Int = itemView.top + (itemView.height - drawable.intrinsicHeight) / 2
        val bottom: Int = top + drawable.intrinsicHeight
        val left: Int = itemView.right - iconMargin - drawable.intrinsicWidth
        val right: Int = itemView.right - iconMargin

        return Rect(left, top, right, bottom)
    }

    private fun Paint.yCenteredOn(drawable: Drawable): Float {
        return drawable.bounds.top + drawable.bounds.height() / 2 - (descent() + ascent()) / 2
    }

}