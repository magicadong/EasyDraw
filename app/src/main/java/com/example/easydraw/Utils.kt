package com.example.easydraw

import android.content.Context

//dp -> px
fun dp2px(context: Context,dp:Int):Float{
   return context.resources.displayMetrics.density*dp
}