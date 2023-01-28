package com.codecoy.bijy.utils

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.owner.models.Owner
import com.codecoy.bijy.owner.ui.OwnerMainScreen
import com.codecoy.bijy.users.models.User


class MyApp : Application() {



    lateinit var apiKey:String


    // lateinit var repositry: MainRepository
    companion object {

        @JvmStatic
        lateinit var homeFragment: Fragment

        @JvmStatic
        var isGuest: Boolean = false

        @JvmStatic
         var owner: Owner? = null

        @JvmStatic
        lateinit var user: User

        @JvmStatic
         var checkCreateRequest: String = "Create"

        @JvmStatic
        lateinit var locationManager: LocationManager

        @JvmStatic
         var selectedLatitude: Double? = null

        @JvmStatic
         var selectedLongitude: Double? = null

        @JvmStatic
        var flag = MutableLiveData<Flag>()

        @JvmStatic
        var flagList: MutableList<Flag> = arrayListOf()



    }

    override fun onCreate() {
        super.onCreate()

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = this.packageManager
            .getApplicationInfo(
                applicationContext.packageName,
                PackageManager.GET_META_DATA
            )
        apiKey= ai.metaData["com.google.android.geo.API_KEY"].toString()
    }




}