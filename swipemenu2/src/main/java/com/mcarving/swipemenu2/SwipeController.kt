package com.mcarving.swipemenu2

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
// from source:
// https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28
// https://github.com/FanFataL/swipe-controller-demo

class SwipeController(
        private val buttonsActions : SwipeControllerActions
) : ItemTouchHelper.Callback() {
    private var buttonInstance: RectF? = null
    private var swipeBack = false
    private var buttonShowedState = ButtonsState.GONE
    private val buttonWidth = 300F

    private var currentItemViewHolder : RecyclerView.ViewHolder? = null

    override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT or RIGHT)
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = buttonShowedState != ButtonsState.GONE
            //swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        var newX = dX

        if (actionState == ACTION_STATE_SWIPE) {
            when(buttonShowedState){
                //ButtonsState.LEFT_VISIBLE -> newX = buttonWidth
                ButtonsState.LEFT_VISIBLE -> newX = Math.max(dX, buttonWidth)
                //ButtonsState.RIGHT_VISIBLE -> newX = -buttonWidth
                ButtonsState.RIGHT_VISIBLE -> newX = Math.min(dX, -buttonWidth)

                else -> setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }

        super.onChildDraw(c, recyclerView, viewHolder, newX, dY, actionState, isCurrentlyActive)

        currentItemViewHolder = viewHolder

        //drawButtons(c, viewHolder)
    }


    private fun setTouchListener(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            currentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL ||
                    event.action == MotionEvent.ACTION_UP

            if(swipeBack){
                if(dX < -buttonWidth) {
                    buttonShowedState = ButtonsState.RIGHT_VISIBLE
                } else if(dX > buttonWidth){
                    buttonShowedState = ButtonsState.LEFT_VISIBLE
                }

                if(buttonShowedState != ButtonsState.GONE) {
                    setTouchDownListener(c, recyclerView, viewHolder, dX, dY,
                            actionState, currentlyActive)
                    setItemsClickable(recyclerView, false)
                }
            }
            false
        }

    }

    private fun setTouchDownListener(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            currentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, currentlyActive)
            }
            false
        }
    }

    private fun setTouchUpListener(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            currentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_UP){
                val temp = buttonInstance
                if(temp != null && temp.contains(event.x, event.y)){
                    when(buttonShowedState){
                        ButtonsState.LEFT_VISIBLE -> {
                            buttonsActions.onLeftClicked(viewHolder.adapterPosition)
                        }
                        ButtonsState.RIGHT_VISIBLE -> {
                            buttonsActions.onRightClicked(viewHolder.adapterPosition)
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }

                buttonShowedState = ButtonsState.GONE
                this@SwipeController.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        0F,
                        dY,
                        actionState,
                        currentlyActive
                )

                recyclerView.setOnTouchListener { v, event ->
                    false
                }

                setItemsClickable(recyclerView, true)
                swipeBack = false


                currentItemViewHolder = null
            }
            false
        }

    }

    private fun setItemsClickable(recyclerView: RecyclerView, isClickable : Boolean){
        for(i in 0 until recyclerView.childCount){
            recyclerView.getChildAt(i).isClickable = isClickable
        }
    }


    fun onDraw(c : Canvas){
        currentItemViewHolder?.apply {
            drawButtons(c, this)
        }
    }

    private fun drawButtons(c: Canvas, viewHolder: RecyclerView.ViewHolder) {
        val buttonWidthWithoutPadding = buttonWidth - 20F
        val corners = 16F

        val itemView = viewHolder.itemView
        val p = Paint()

        val leftButton = RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                (itemView.left + buttonWidthWithoutPadding),
                (itemView.bottom).toFloat()
        )
        p.color = Color.BLUE
        c.drawRoundRect(leftButton, corners, corners, p)
        drawText("Edit", c, leftButton, p)

        val rightButton = RectF(
                itemView.right - buttonWidthWithoutPadding,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
        )
        p.color = Color.RED
        c.drawRoundRect(rightButton, corners, corners, p)
        drawText("DELETE", c, rightButton, p)

        buttonInstance = null
        if(buttonShowedState == ButtonsState.LEFT_VISIBLE){
            buttonInstance = leftButton
        } else if(buttonShowedState == ButtonsState.RIGHT_VISIBLE){
            buttonInstance = rightButton
        }
    }

    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = 60F
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize

        val textWidth = p.measureText(text)
        c.drawText(text,
                button.centerX()-(textWidth/2),
                button.centerY()+(textSize/2),
                p
        )
    }
}

enum class ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}