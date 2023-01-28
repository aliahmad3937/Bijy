package com.codecoy.bijy.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.codecoy.bijy.MainActivity
import com.codecoy.bijy.utils.Constants.MY_PERMISSIONS_REQUEST_LOCATION
import com.codecoy.bijy.utils.Constants.REQUEST_GPS
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*


class Permissions {


    companion object {
        var googleApiClient: GoogleApiClient? = null


        fun isGpsEnabled(): Boolean {
            return  MyApp.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun isNetworkEnabled(): Boolean {
            return  MyApp.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        fun checkNetworkPermissions(context: Context): Boolean {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            return true
        }

        fun requestNetworkPermissions(context: Context){
                ActivityCompat.requestPermissions(context as MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION)
        }



        fun hasGPSDevice(): Boolean {
            val providers = MyApp.locationManager.allProviders
            return providers.contains(LocationManager.GPS_PROVIDER)
        }

        fun enableGPS(context: Context) {
            if (googleApiClient == null) {
                googleApiClient = GoogleApiClient.Builder(context).addApi(LocationServices.API)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks{
                        override fun onConnected(bundle: Bundle?) {
                            Log.e("connected", "connected")
                        }

                        override fun onConnectionSuspended(i: Int) {
                            Log.e("suspended", "suspended")
                            googleApiClient!!.connect()
                        }
                    })
                    .addOnConnectionFailedListener { connectionResult ->
                        Log.e("Location error",
                            "Location error " + connectionResult.errorCode)
                    }.build()
                googleApiClient!!.connect()
                val locationRequest: LocationRequest = LocationRequest.create()
                locationRequest.apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    interval = 7 * 1000 //30 * 1000
                    fastestInterval = 5 * 1000 //5 * 1000
                }
                val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                builder.setAlwaysShow(true)
                val result: PendingResult<LocationSettingsResult> =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient!!, builder.build())
                result.setResultCallback { result1 ->
                    Log.e("result", "result")
                    val status: Status = result1.status
                    if (status.statusCode === LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        Log.e("RESOLUTION_REQUIRED", "")
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(context as MainActivity, REQUEST_GPS)

                            // getActivity().finish();
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                            Log.e("error", "error")
                        }
                    }
                }
            }
        }

    }


}