package com.example.easydraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DrawView: View {
    //画笔颜色
    private var mColor = Color.BLACK
    //画笔粗细
    private var strokeSize = 10f
    //画笔
    private val mPaint = Paint().apply {
        color = mColor
        strokeWidth = strokeSize
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    //路径
    private var mPath: Path? = null
    //保存所有的路径
    private val pathsList = mutableListOf<CustomPath>()

    constructor(context: Context):super(context){}
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs){}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //绘制之前的所有路径
        if(pathsList.size > 0){
            pathsList.forEach {
                mPaint.color = it.color
                mPaint.strokeWidth = it.size
                canvas?.drawPath(it.path,mPaint)
            }
        }

        //绘制当前这条路径
        if (mPath != null) {
            mPaint.color = mColor
            mPaint.strokeWidth = strokeSize
            canvas?.drawPath(mPath!!, mPaint)
        }
    }

    //记录画的轨迹
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                //创建新的路径
                mPath = Path()
                //当前触摸点就是这个路径的起始点
                mPath?.moveTo(event?.x,event?.y)
            }
            MotionEvent.ACTION_MOVE ->{
                mPath?.lineTo(event?.x,event?.y)
                //重新绘制界面
                invalidate()
            }
            MotionEvent.ACTION_UP ->{
                //保存当前路径
                pathsList.add(CustomPath(mPath!!,mColor,strokeSize))
                mPath = null
            }
        }
        return true
    }
    //画笔颜色
    fun changeColor(colorString: String){
        mColor = Color.parseColor(colorString)
    }
    //画笔粗细
    fun changeStrokeSize(size:Float){
        strokeSize = size
    }
    //撤销操作
    fun undo(){
        if (pathsList.size > 0){
            pathsList.removeLast()
            invalidate()
        }
    }
    //橡皮擦
    fun erase(){
        mColor = Color.WHITE
    }
}