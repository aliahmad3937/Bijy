package com.codecoy.bijy.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavController

import androidx.navigation.fragment.findNavController


import com.codecoy.bijy.R

fun Fragment.showSignUpDialogue(){
    val builder: AlertDialog.Builder = AlertDialog.Builder(this.context)
    builder.setIcon(R.drawable.warning)
    builder.setTitle("SignUp Required!")
    builder.setMessage("Do you want to SignUp ?")
    builder.setCancelable(true)
    builder.setPositiveButton("Yes",
        DialogInterface.OnClickListener { dialogInterface, i ->

            //dont forget to clear any user related data in your preferences

            val deepLink = InternalDeepLink.USER_SIGNUP_SCREEN.toUri()
            findNavController().popBackStack()
            findNavController().navigate(deepLink)
        })
    builder.setNegativeButton("No",
        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
    val alertDialog: AlertDialog = builder.create()
    alertDialog.show()
}