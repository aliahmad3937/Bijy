package com.codecoy.bijy.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SavedPreference {

    const val EMAIL= "email"
    const val USERNAME="username"
    const val TOKEN="devicetoken"

    private  fun getSharedPreference(ctx: Context?): SharedPreferences? {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    private fun  editor(context: Context, key:String, value: String){
        getSharedPreference(
            context
        )?.edit()?.putString(key,value)?.apply()
    }

    fun getEmail(context: Context)= getSharedPreference(
        context
    )?.getString(EMAIL,"")

    fun setEmail(context: Context, email: String){
        editor(
            context = context,
            key =  EMAIL,
            value = email
        )
    }

    fun setUsername(context: Context, username:String){
        editor(
           context =  context,
           key =  USERNAME,
           value =  username
        )
    }
    fun setToken(context: Context, token:String){
        editor(
           context =  context,
           key =  TOKEN,
           value =  token
        )
    }

    fun getUsername(context: Context) = getSharedPreference(
        context
    )?.getString(USERNAME,"")

}