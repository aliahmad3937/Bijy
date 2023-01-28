package com.codecoy.bijy.owner.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.MainActivity
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentOwnerCreateFlagBinding
import com.codecoy.bijy.databinding.FragmentOwnerFlagStatusBinding
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.utils.*
import com.codecoy.bijy.utils.MapUtils.addMarker
import com.codecoy.bijy.utils.MapUtils.addMarkerOnTouchMap
import com.codecoy.bijy.utils.MapUtils.markerPoints
import com.codecoy.bijy.utils.MapUtils.originLatitude
import com.codecoy.bijy.utils.MapUtils.originLongitude
import com.codecoy.bijy.utils.MyApp.Companion.checkCreateRequest
import com.codecoy.bijy.utils.MyApp.Companion.selectedLatitude
import com.codecoy.bijy.utils.ToastUtils.showToast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class OwnerCreateFlag : Fragment(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private lateinit var binding: FragmentOwnerCreateFlagBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mContext: MainActivity
    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLastLocation: Location? = null
    private var mCurrLocationMarker: Marker? = null
    private var flagStatus: String = "Busy"
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var UID: String

    private  var identPath : Uri? = null
    private  var ownerProofPath : Uri? = null


    private var flag: Flag? = null

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                identPath = result.data!!.data!!

            }
        }

    private var resultLauncherProof =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                ownerProofPath = result.data!!.data!!

            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flag = it.getSerializable(Constants.ARG_PARAM1) as Flag

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_owner_create_flag, container, false)

        binding.identification.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(intent)
        }

        binding.ownerProof.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncherProof.launch(intent)
        }

        mFirestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        UID = auth.currentUser!!.uid.toString()


        binding.toolbarFlagOwner.apply {
            title.text = getString(R.string.add_new_flag)
            logout.visibility = View.GONE
            back.visibility = View.GONE
//            back.apply {
//                setImageResource(R.drawable.back_arrow)
//                setOnClickListener {
//                    findNavController().popBackStack()
//                }
//            }

        }





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Permissions.checkNetworkPermissions(mContext))
                Permissions.requestNetworkPermissions(mContext)
        }

        // Obtain the SupportMapFragment and get notified
        // when the map is ready to be used.
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // at last we calling our map fragment to update.
        // getSupportFragmentManager().findFragmentById(R.id.map).getMapAsync(this)
        mapFragment.getMapAsync(this)


        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Responds to child RadioButton checked/unchecked
            when(checkedId){
                R.id.radio_busy -> {
                    flagStatus = "Busy"
                }
                R.id.radio_very_busy -> {
                    flagStatus = "Very Busy"
                }
                R.id.radio_not_busy -> {

                    flagStatus = "Not Busy"
                }

            }
        }

        flag?.let {

            when(it.flagStatus){
                "Busy" -> {
                    binding.radioBusy.isChecked = true
                    flagStatus = "Busy"
                }
                "Very Busy" -> {
                    binding.radioVeryBusy.isChecked = true
                    flagStatus = "Very Busy"
                }
                "NotBusy" -> {
                    binding.radioNotBusy.isChecked = true
                    flagStatus = "Not Busy"
                }
            }

            binding.tvAddress.text = it.title
            binding.etTitleOfFlag.setText(it.title)
            binding.etNoteOfFlag.setText(it.flagNote)
        }

        if (MyApp.checkCreateRequest.equals(Constants.UPDATE_STATUS)) {
            binding.btnSave.text = Constants.UPDATE_STATUS
        } else {
            binding.btnSave.text = "Save"
        }


        binding.btnSave.setOnClickListener {
            if (checkCreateRequest.equals(Constants.UPDATE_STATUS)) {

                if (selectedLatitude != null && !binding.tvAddress.text.toString().isEmpty()) {

                    ProgressUtil.showProgress(
                        requireContext(),
                        msg = getString(R.string.creating_flag)
                    )


                    val flag = Flag(
                        flagID = flag!!.flagID,
                        userID = flag!!.userID,
                        selectedLatitude!!,
                        MyApp.selectedLongitude!!,
                        binding.etTitleOfFlag.text.toString(),
                        binding.tvAddress.text.toString(),
                        flagStatus.toString(),
                        flag!!.userType,
                        date = flag!!.date,
                        time = flag!!.time,
                        flagNote = binding.etNoteOfFlag.text.toString(),
                        data = flag!!.data
                    )


                    mFirestore
                        .collection("flags")
                        .document(flag.flagID)
                        .set(flag)
                        .addOnSuccessListener {
                            showToast(mContext, getString(R.string.flag_updated))
                            MyApp.selectedLatitude = null
                            MyApp.selectedLongitude = null
                            markerPoints.clear()
                            binding.tvAddress.text = ""
                            findNavController().navigate(R.id.action_ownerCreateFlag_to_ownerMainScreen)
                            ProgressUtil.hideProgress()
                        }
                }

            } else {
                Log.v("TAG7","247")

                if (selectedLatitude != null && !binding.tvAddress.text.toString().isEmpty() && !binding.etTitleOfFlag.text.toString().isEmpty()) {
                    // showToast(mContext, "Flag Added successfully!")
                    Log.v("TAG7","251")
                    val dateObject = Date(System.currentTimeMillis())
                    val calendarInstance = Calendar.getInstance()
                    calendarInstance.time = dateObject
                    val hour = calendarInstance.get(Calendar.HOUR)
                    val minute = calendarInstance.get(Calendar.MINUTE)
                    val ampm = if (calendarInstance.get(Calendar.AM_PM) == 0) "AM " else "PM "
                    val date = calendarInstance.get(Calendar.DATE)
                    val month = calendarInstance.get(Calendar.MONTH)
                    val year = calendarInstance.get(Calendar.YEAR)
                    ProgressUtil.showProgress(
                        requireContext(),
                        msg = getString(R.string.creating_flag)
                    )

                    val flagId = UUID.randomUUID().toString()

                    val flag = Flag(
                        flagID = flagId,
                        userID = UID,
                        selectedLatitude!!,
                        MyApp.selectedLongitude!!,
                        binding.etTitleOfFlag.text.toString(),
                        binding.tvAddress.text.toString(),
                        flagStatus.toString(),
                        UserType.Owner.toString(),
                        date = "$date/$month/$year",
                        time = "${if (hour == 0) "12" else {if (hour <= 9) "0$hour" else hour}}:${if (minute <= 9) "0$minute" else minute}",
                        flagNote = binding.etNoteOfFlag.text.toString(),
                        data = ""
                    )


                    mFirestore
                        .collection("flags")
                        .document(flagId)
                        .set(flag)
                        .addOnSuccessListener {
                            showToast(requireContext(), getString(R.string.flag_added))
                            MyApp.selectedLatitude = null
                            MyApp.selectedLongitude = null
                            markerPoints.clear()
                            binding.tvAddress.text = ""
                            findNavController().navigate(R.id.action_ownerCreateFlag_to_ownerMainScreen)
                            ProgressUtil.hideProgress()
                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "Error writing document", e)
                            ProgressUtil.hideProgress()
                        }

                }
                else {
                    Log.v("TAG7","304")
                    if(binding.etTitleOfFlag.text.toString().isEmpty()){
                        binding.etTitleOfFlag.error ="Enter Title of this Flag!"
                    }
                    if(binding.tvAddress.text.toString().isEmpty()){
                        binding.etTitleOfFlag.error ="Please pin location on Map!"
                    }
                }
            }
        }




        return binding.root
    }


    override fun onMapReady(googleMap: GoogleMap) {
//
//        mMap = googleMap!!
//        val originLocation = LatLng(originLatitude, originLongitude)
//        mMap!!.clear()
//        mMap!!.addMarker(MarkerOptions().position(originLocation))
//        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 18F))
//
        Log.v("TAG2", "onMapReady :505")
        //   MyApp.showToast(mContext, "onMapReady")


        mMap = googleMap
        mMap?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isZoomGesturesEnabled = true
            uiSettings.isCompassEnabled = true
        }

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Permissions.checkNetworkPermissions(mContext)) {
                buildGoogleApiClient()
                //     mMap!!.isMyLocationEnabled = true
            } else {
                Permissions.requestNetworkPermissions(mContext)
            }
        } else {
            buildGoogleApiClient()
            // mMap!!.isMyLocationEnabled = true
        }

    }


    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(mContext)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()
        Log.v("TAG2", "buildGoogleApiClient :538")
    }


    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (Permissions.checkNetworkPermissions(mContext)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient!!,
                mLocationRequest!!, this
            )
        } else {
            Permissions.requestNetworkPermissions(mContext)
        }

        Log.v("TAG2", "onConnected :554")
        //   MyApp.showToast(mContext, "onConnected")
    }

    override fun onConnectionSuspended(i: Int) {
        Log.v("TAG2", "onConnectionSuspended :558")
        showToast(mContext, "onConnectionSuspended")

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        showToast(mContext, "onConnectionFailed")
    }


    override fun onLocationChanged(location: Location) {
        Log.v("TAG2", "onLocationChanged :565")
        //   MyApp.showToast(mContext, "Location is changing...")

        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Showing Current Location Marker on Map
        originLatitude = location.latitude
        originLongitude = location.longitude
        val latLng = LatLng(location.latitude, location.longitude)
//        val markerOptions = MarkerOptions()
//        markerOptions.position(latLng)
//        val locationManager =
//            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
//        val provider = locationManager!!.getBestProvider(Criteria(), true)
//        if (!Permissions.checkNetworkPermissions(mContext)) {
//            return
//        }
//        val locations = locationManager.getLastKnownLocation(provider!!)
//        val providerList = locationManager.allProviders
//        if (null != locations && null != providerList && providerList.size > 0) {
//            val longitude = locations.longitude
//            val latitude = locations.latitude
//            val geocoder = Geocoder(
//                mContext,
//                Locale.getDefault()
//            )
//            try {
//                val listAddresses = geocoder.getFromLocation(
//                    latitude,
//                    longitude, 1
//                )
//                if (null != listAddresses && listAddresses.size > 0) {
//                    val state = listAddresses[0].adminArea
//                    val country = listAddresses[0].countryName
//                    val subLocality = listAddresses[0].subLocality
//                    markerOptions.title(
//                        "" + latLng + "," + subLocality + "," + state
//                                + "," + country
//                    )
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//        mCurrLocationMarker = mMap!!.addMarker(markerOptions)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))


            if (flag != null) {
                addMarker(flag!!.latitude, flag!!.longitude, mMap!!, mContext)

                // Adding new item to the ArrayList
                markerPoints.add(LatLng(flag!!.latitude, flag!!.longitude))

                MyApp.selectedLatitude = flag!!.latitude
                MyApp.selectedLongitude = flag!!.longitude
                addMarkerOnTouchMap(mMap!!, binding.tvAddress, mContext)
            } else {
                addMarkerOnTouchMap(mMap!!, binding.tvAddress, mContext)
            }
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient!!,
                    this
                )
            }


    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as MainActivity
    }

    override fun onPause() {
        super.onPause()
        MyApp.checkCreateRequest = "Create"
    }
}