package com.mcarving.swipemenu2

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
// from source:
// https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28
// https://github.com/FanFataL/swipe-controller-demo

//modified with drawButtons, two buttons on the right

class SwipeController(
        private val context : Context,
        private val buttonsActions : SwipeControllerActions
) : ItemTouchHelper.Callback() {
    private var buttonEditInstance: RectF? = null
    private var buttonDeleteInstance: RectF? = null
    private var swipeBack = false
    private var buttonShowedState = ButtonsState.GONE
    private val buttonWidth = 300F

    private var currentItemViewHolder : RecyclerView.ViewHolder? = null

    override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT)
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
                ButtonsState.RIGHT_VISIBLE -> newX = Math.min(dX, -buttonWidth*15/19*2)

                else -> setTouchListener(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive)
            }

        }

        super.onChildDraw(c, recyclerView, viewHolder, newX, dY, actionState, isCurrentlyActive)

        currentItemViewHolder = viewHolder
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
                if(dX < -buttonWidth * 15 / 19) {
                    buttonShowedState = ButtonsState.RIGHT_VISIBLE
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
                val editBtn = buttonEditInstance
                val deleteBtn = buttonDeleteInstance
                if(buttonShowedState == ButtonsState.RIGHT_VISIBLE){
                    if(editBtn != null && editBtn.contains(event.x, event.y)){
                        buttonsActions.onEditClicked(viewHolder.adapterPosition)
                    }
                    if(deleteBtn != null && deleteBtn.contains(event.x, event.y)){
                        buttonsActions.onDeleteClicked(viewHolder.adapterPosition)
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

        val rightButtonEdit = RectF(
                itemView.right - buttonWidthWithoutPadding * 15 / 19 * 2,
                itemView.top.toFloat(),
                itemView.right - buttonWidthWithoutPadding * 15 / 19,
                (itemView.bottom).toFloat()
        )
        p.color = Color.BLUE
        c.drawRoundRect(rightButtonEdit, corners, corners, p)
        drawText("Edit", c, rightButtonEdit, p)
        drawIcon(R.drawable.ic_edit, c, rightButtonEdit)

        val rightButtonDelete = RectF(
                itemView.right - buttonWidthWithoutPadding * 15 / 19,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
        )
        p.color = Color.RED
        c.drawRoundRect(rightButtonDelete, corners, corners, p)
        drawText("Remove", c, rightButtonDelete, p)
        drawIcon(R.drawable.ic_delete, c, rightButtonDelete)

        buttonEditInstance = rightButtonEdit
        buttonDeleteInstance = rightButtonDelete
    }

    private fun drawIcon(resId: Int, c: Canvas, rightButtonEdit: RectF) {
        val d = context.resources.getDrawable(resId, null)
        d.bounds=Rect(rightButtonEdit.centerX().toInt() - 30,
                rightButtonEdit.centerY().toInt() - 50,
                rightButtonEdit.centerX().toInt() + 30,
                rightButtonEdit.centerY().toInt() + 10)

        d.draw(c)
    }

    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = 40F
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize

        val textWidth = p.measureText(text)
        c.drawText(text,
                button.centerX()-(textWidth/2),
                button.centerY()+textSize*3/2,
                p
        )
    }
}

enum class ButtonsState {
    GONE,
    RIGHT_VISIBLE
}