package com.android.inputmethod.ui.components.recycleradapter

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.internal.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class SwipeCallback(private val swipeLayouts: Map<SwipeDirection, Int>) : ItemTouchHelper.SimpleCallback(0, swipeLayouts.swipeDirs()) {
    constructor(vararg swipeLayouts: Pair<SwipeDirection, Int>) : this(swipeLayouts.toMap())

    private var listener: OnSwipeListener? = null

    private lateinit var swipeViewMap: Map<SwipeDirection, View>

    init {
        require(swipeLayouts.isNotEmpty()) {
            "Swipe bindings must contain at least one"
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView: View = viewHolder.itemView

        if (!::swipeViewMap.isInitialized) {
            swipeViewMap = swipeLayouts.map { (direction, layout) ->
                direction to inflateLayout(itemView.context, layout)
            }.toMap()
        }

        when {
            // canvas, swipeView, width, height, dY, dX
            dX > 0 -> {
                // Swipe to right
                val swipeView = swipeViewMap.getValue(SwipeDirection.RIGHT)
                val width = min(dX.toInt(), itemView.width)
                renderSwipeView(swipeView, c, width, itemView.height, itemView.left.toFloat(), itemView.top.toFloat())
            }
            dY > 0 -> {
                // Swipe to down
                val swipeView = swipeViewMap.getValue(SwipeDirection.DOWN)
                val height = min(dY.toInt(), itemView.height)
                renderSwipeView(swipeView, c, itemView.width, height, itemView.left.toFloat(), itemView.top.toFloat())
            }
            dX < 0 -> {
                // Swipe to left
                val swipeView = swipeViewMap.getValue(SwipeDirection.LEFT)
                val width = min(abs(dX.toInt()), itemView.width)
                val transX = max(itemView.right + dX, itemView.left.toFloat())
                renderSwipeView(swipeView, c, width, itemView.height, transX, itemView.top.toFloat())
            }
            dY < 0 -> {
                // Swipe to up
                val swipeView = swipeViewMap.getValue(SwipeDirection.UP)
                val height = min(abs(dY.toInt()), itemView.height)
                val transY = max(itemView.bottom.toFloat() + dY, itemView.top.toFloat())
                renderSwipeView(swipeView, c, itemView.width, height, itemView.left.toFloat(), transY)
            }
            else -> {
                // view is reset
                c.clipRect(itemView.left, itemView.top, itemView.right, itemView.bottom)
            }
        }
    }

    private fun renderSwipeView(view: View, c: Canvas, width: Int, height: Int, transX: Float, transY: Float) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, width, height)

        c.save()
        c.translate(transX, transY)
        view.draw(c)
        c.restore()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener?.onSwipe(viewHolder, SwipeDirection.valueOf(direction))
    }

    fun setOnSwipeListener(listener: OnSwipeListener?) {
        this.listener = listener
    }

    private fun inflateLayout(context: Context, @LayoutRes layout: Int): View {
        val li: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return li.inflate(layout, null)
    }
}

fun Map<SwipeDirection, Int>.swipeDirs(): Int = keys.map { it.swipeDir }.fold(0, { a, b -> a or b })

interface OnSwipeListener {
    fun onSwipe(viewHolder: RecyclerView.ViewHolder, direction: SwipeDirection)
}

enum class SwipeDirection(val swipeDir: Int) {
    UP(ItemTouchHelper.UP),
    DOWN(ItemTouchHelper.DOWN),
    LEFT(ItemTouchHelper.LEFT),
    RIGHT(ItemTouchHelper.RIGHT);

    companion object {
        fun valueOf(direction: Int): SwipeDirection = when (direction) {
            ItemTouchHelper.LEFT -> LEFT
            ItemTouchHelper.UP -> UP
            ItemTouchHelper.RIGHT -> RIGHT
            ItemTouchHelper.DOWN -> DOWN
            else -> throw IllegalArgumentException("Swipe direction '$this' not supported")
        }
    }
}

fun SwipeCallback.attachTo(recyclerView: RecyclerView) {
    val ith = ItemTouchHelper(this)
    ith.attachToRecyclerView(recyclerView)
}

data class SwipeEvent(val viewHolder: RecyclerView.ViewHolder, val direction: SwipeDirection)

fun SwipeCallback.swipes(): Observable<SwipeEvent> {
    return ViewClickObservable(this)
}

private class ViewClickObservable(
        private val swipeCallback: SwipeCallback
) : Observable<SwipeEvent>() {

    override fun subscribeActual(observer: Observer<in SwipeEvent>) {
        if (!checkMainThread(observer)) {
            return
        }
        val listener = Listener(swipeCallback, observer)
        observer.onSubscribe(listener)
        swipeCallback.setOnSwipeListener(listener)
    }

    private class Listener(
            private val callback: SwipeCallback,
            private val observer: Observer<in SwipeEvent>
    ) : MainThreadDisposable(), OnSwipeListener {


        override fun onSwipe(viewHolder: RecyclerView.ViewHolder, direction: SwipeDirection) {
            if (!isDisposed) {
                observer.onNext(SwipeEvent(viewHolder, direction))
            }
        }

        override fun onDispose() {
            callback.setOnSwipeListener(null)
        }
    }
}

