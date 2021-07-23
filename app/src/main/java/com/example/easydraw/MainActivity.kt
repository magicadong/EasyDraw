package com.example.easydraw

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    //画笔尺寸视图是否打开
    private var brushContainerIsOpen = false
    //记录颜色的视图状态
    private var colorIsOpen = false
    //动画如果没结束 按钮不能点
    private var colorAnimationIsFininshed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushBtnEvent()
        brushContainerSizeBtnEvent()
        floatingActionBtnEvent()
        colorBtnEvent()
        eraserBtnEvent()
        undoBtnEvent()
        saveBtnEvent()
    }

    //下载分享
    private fun saveBtnEvent(){
        saveImageBtn.setOnClickListener {
            //将DrawView上绘制的内容转化成一张图片 Bitmap
            val bitmap = convertViewToBitmap(drawView)
            //将图片保存到本地
            saveToAlbum(bitmap)
        }
    }
    //图片写入相册
    private fun saveToAlbum(bitmap: Bitmap){
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED){
            //保存
            lifecycleScope.launch {
                val uri = saveImage(bitmap)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM,uri)
                    type = "image/jpg"
                }
                startActivity(Intent.createChooser(shareIntent,"share"))
            }
        }else{
            requestPermission()
        }
    }
    //保存图片
    private suspend fun saveImage(bitmap: Bitmap):Uri{
        var result = false
        var uri:Uri? = null
        withContext(Dispatchers.IO){
            //contentResolver
            val contents = ContentValues().apply {
                put(MediaStore.Images.ImageColumns.DISPLAY_NAME,"abc.jpg")
                put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/jpg")
                put(MediaStore.Images.ImageColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
            }
            val imgUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contents)
            if (imgUri != null) {
                val fos = contentResolver.openOutputStream(imgUri)
                result = bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                uri = imgUri
            }
        }
        if (result) {
            Toast.makeText(this, "保存成功",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "保存失败",Toast.LENGTH_SHORT).show()
        }
        return uri!!
    }
    //权限申请
    private fun requestPermission(){
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
    }

    //将view 上绘制的内容转化成一张图片 Bitmap
    private fun convertViewToBitmap(view: View):Bitmap{
        val bitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) //底色 背景色
        view.draw(canvas)
        return bitmap
    }

    //撤销事件
    private fun undoBtnEvent(){
        undoImageBtn.setOnClickListener {
            drawView.undo()
        }
    }

    //橡皮擦事件
    private fun eraserBtnEvent(){
        eraserImageBtn.setOnClickListener {
            drawView.erase()
        }
    }

    //颜色视图事件
    private fun colorBtnEvent(){
        arrayOf(blueBtn,pinkBtn,purpleBtn,orangeBtn).forEach {
            it.setOnClickListener { _ ->
                //mContainer.setBackgroundColor(Color.parseColor(it.tag as String))
                drawView.changeColor(it.tag as String)
            }
        }
    }
    //悬浮按钮事件 展开或者关闭
    private fun floatingActionBtnEvent(){
        floatingActionButton.setOnClickListener {
            if (colorAnimationIsFininshed){
                var space = if (colorIsOpen){//关闭
                    dp2px(this,70)
                }else {//打开
                    -dp2px(this, 70)
                }
                val colorBtns = arrayOf(blueBtn,pinkBtn,purpleBtn,orangeBtn)
                colorBtns.forEach {
                    val index = colorBtns.indexOf(it)
                    it.animate().translationYBy((index+1)*space)
                        .setDuration(300)
                        .setInterpolator(BounceInterpolator())
                        .setListener(object : Animator.AnimatorListener{
                            override fun onAnimationStart(animation: Animator?) {
                                colorAnimationIsFininshed = false
                            }
                            override fun onAnimationEnd(animation: Animator?) {
                                colorAnimationIsFininshed = true
                            }
                            override fun onAnimationCancel(animation: Animator?) {

                            }
                            override fun onAnimationRepeat(animation: Animator?) {

                            }
                        })
                        .start()
                }
                colorIsOpen = !colorIsOpen
            }
        }
    }
    //画笔视图上每个尺寸视图事件
    private fun brushContainerSizeBtnEvent(){
        arrayOf(size1Btn,size2Btn,size3Btn,size4Btn).forEach {
            //添加点击事件
            it.setOnClickListener {_ ->
                //隐藏容器
                showOrHideBrushContainer(false)
                //更改画笔粗细
                drawView.changeStrokeSize((it.tag as String).toFloat())
            }
        }
    }
    //画笔按钮事件
    private fun brushBtnEvent(){
        brushImageBtn.setOnClickListener {
            showOrHideBrushContainer(!brushContainerIsOpen)
        }
    }
    private fun showOrHideBrushContainer(shouldOpen:Boolean){
        var starty = 0f
        var endy = 1f
        if (shouldOpen){
            //显示控件
            starty = 1f
            endy = 0f
            brushSizeContainer.visibility = View.VISIBLE
        }else{
            brushSizeContainer.visibility = View.GONE
        }
        //创建出现动画
        val anim = TranslateAnimation(
            Animation.ABSOLUTE,0f,
            Animation.ABSOLUTE,0f,
            Animation.RELATIVE_TO_PARENT,starty,
            Animation.RELATIVE_TO_SELF,endy
        ).apply {
            duration = 300
            fillAfter = true
            interpolator = BounceInterpolator()
        }
        //添加动画
        brushSizeContainer.startAnimation(anim)
        //更改状态
        brushContainerIsOpen = !brushContainerIsOpen
    }

}