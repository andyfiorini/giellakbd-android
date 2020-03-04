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
import com.jakewharton.rxbinding3.internal.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable


data class SwipeConf(
        val right: SwipeActionConf? = null,
        val left: SwipeActionConf? = null
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

interface OnSwipeListener {
    fun onSwipeLeft(viewHolder: RecyclerView.ViewHolder)
    fun onSwipeRight(viewHolder: RecyclerView.ViewHolder)

}


class SwipeActionCallback(swipeConf: SwipeConf) : ItemTouchHelper.SimpleCallback(0, swipeConf.swipeDirs.fold(0, { a, b -> a or b })) {

    private lateinit var leftConf: SwipeActionConf
    private lateinit var rightConf: SwipeActionConf

    private var listener: OnSwipeListener? = null

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
                listener?.onSwipeLeft(viewHolder)

            }
            ItemTouchHelper.RIGHT -> {
                listener?.onSwipeRight(viewHolder)
            }
        }
    }

    fun setOnSwipeListener(listener: OnSwipeListener?) {
        this.listener = listener
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView: View = viewHolder.itemView

        Log.d("LeftSwipe", "dX: $dX, dY: $dY actionState: $actionState active: $isCurrentlyActive")
        when {
            dX > 0 -> {
                // Swiping to the right
                val canvasRight = dX.toInt() + itemView.left
                c.clipRect(itemView.left, itemView.top, canvasRight, itemView.bottom)

                // Draw background
                rightConf.background.setBounds(itemView.left, itemView.top, canvasRight, itemView.bottom)
                rightConf.background.draw(c)

                // Draw icon
                val drawable = rightConf.icon
                val bounds = drawableRightSwipeBounds(itemView, drawable)
                val margin: Int = (itemView.height - drawable.intrinsicHeight) / 2
                drawable.bounds = bounds

                // Draw text
                val textY = rightConf.textPaint.yCenteredOn(itemView)
                val textX = bounds.right.toFloat() + margin
                c.drawText(rightConf.text, textX, textY, rightConf.textPaint)

                // Clip based on itemView
                drawable.draw(c)
            }
            dX < 0 -> {
                // Swiping to the left
                val canvasLeft = itemView.right + dX.toInt()
                c.clipRect(canvasLeft, itemView.top, itemView.right, itemView.bottom)

                // Draw background
                leftConf.background.setBounds(canvasLeft, itemView.top, itemView.right, itemView.bottom)
                leftConf.background.draw(c)

                // Draw icon
                val drawable = leftConf.icon
                val bounds = drawableLeftSwipeBounds(itemView, drawable)
                val margin: Int = (itemView.height - drawable.intrinsicHeight) / 2
                drawable.bounds = bounds
                drawable.draw(c)

                // Draw text
                val textY = leftConf.textPaint.yCenteredOn(itemView)
                val textX = drawable.bounds.left - leftConf.textPaint.measureText(leftConf.text) - margin
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

    private fun Paint.yCenteredOn(view: View): Float {
        return view.top + view.height / 2 - (descent() + ascent()) / 2
    }

}

sealed class SwipeEvent {
    data class SwipeLeft(val viewHolder: RecyclerView.ViewHolder) : SwipeEvent()
    data class SwipeRight(val viewHolder: RecyclerView.ViewHolder) : SwipeEvent()
}

fun SwipeActionCallback.swipes(): Observable<SwipeEvent> {
    return ViewClickObservable(this)
}

private class ViewClickObservable(
        private val swipeActionCallback: SwipeActionCallback
) : Observable<SwipeEvent>() {

    override fun subscribeActual(observer: Observer<in SwipeEvent>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(swipeActionCallback, observer)
        observer.onSubscribe(listener)
        swipeActionCallback.setOnSwipeListener(listener)
    }

    private class Listener(
            private val actionCallback: SwipeActionCallback,
            private val observer: Observer<in SwipeEvent>
    ) : MainThreadDisposable(), OnSwipeListener {

        override fun onSwipeLeft(viewHolder: RecyclerView.ViewHolder) {
            if (!isDisposed) {
                observer.onNext(SwipeEvent.SwipeLeft(viewHolder))
            }
        }

        override fun onSwipeRight(viewHolder: RecyclerView.ViewHolder) {
            if (!isDisposed) {
                observer.onNext(SwipeEvent.SwipeRight(viewHolder))
            }
        }

        override fun onDispose() {
            actionCallback.setOnSwipeListener(null)
        }
    }
}

