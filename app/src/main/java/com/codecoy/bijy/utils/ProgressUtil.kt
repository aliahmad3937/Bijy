package com.codecoy.bijy.utils

import android.content.Context
import com.kaopiz.kprogresshud.KProgressHUD

object ProgressUtil {
    private var kProgress:KProgressHUD? = null
    fun showProgress(context:Context,msg:String="Please wait"){
        kProgress =  KProgressHUD.create(context)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel(msg)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
            .show();
    }


    fun hideProgress(){
        kProgress?.dismiss()
        kProgress = null
    }


}