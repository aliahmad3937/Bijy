package com.codecoy.bijy.users.ui


import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.MainActivity
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentUserMainScreenBinding
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.users.models.User
import com.codecoy.bijy.utils.*
import com.codecoy.bijy.utils.MapUtils.addCircle
import com.codecoy.bijy.utils.MapUtils.addMarker
import com.codecoy.bijy.utils.MapUtils.latLng
import com.codecoy.bijy.utils.MyApp.Companion.isGuest
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
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar


class UserMainScreen : Fragment(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener , GoogleMap.OnMarkerClickListener , GoogleMap.OnInfoWindowClickListener {


    private lateinit var binding: FragmentUserMainScreenBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mContext: MainActivity
    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLastLocation: Location? = null
    private var mCurrLocationMarker: Marker? = null
    private var flagStatus: Status = Status.Busy
    private lateinit var mFirestore: FirebaseFirestore
    private  var auth: FirebaseAuth? = null
    private  var UID: String? = null

    private lateinit var sp: SharedPreferences
    private var flagList:MutableList<Flag>? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_user_main_screen, container, false)
        sp = requireContext().getSharedPreferences(
            getString(R.string.my_preferences),
            Context.MODE_PRIVATE
        )

        MyApp.homeFragment = this
        MyApp.locationManager =
            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

     //   mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        mFirestore = FirebaseFirestore.getInstance()

        if(!isGuest) {
            auth = FirebaseAuth.getInstance()
            UID = auth!!.currentUser!!.uid.toString()
        }

        // Discrete SeekBar

        binding.seekBar.progress = 50
        binding.tvStart.text = "${binding.seekBar.progress}"

        binding.seekBar.setOnProgressChangeListener(object :
            DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(
                seekBar: DiscreteSeekBar?,
                value: Int,
                fromUser: Boolean
            ) {
                Log.v("TAG4", "onProgressChanged :$value")
                addCircle(mMap!!, latLng, value, binding.tvStart)
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                Log.v("TAG4", "onStartTrackingTouch : ${seekBar!!.progress}")
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                Log.v("TAG4", "onStopTrackingTouch : ${seekBar!!.progress}")
                updateFlags()
            }


        })


        binding.toolbarUserMain.apply {
            title.text = getString(R.string.home)
           logout.visibility = View.GONE

            back.apply {
                setImageResource(R.drawable.ic_menu)
                setOnClickListener {
                    //    open drawer
                    toggleLeftDrawer()
                }
            }

        }

        binding.leftDrawerMenu.apply {
            this.findViewById<ImageView>(R.id.close_drawer).setOnClickListener{
                toggleLeftDrawer()
            }

            this.findViewById<TextView>(R.id.home).setOnClickListener{
                findNavController().navigate(R.id.userMainScreen)
            }

            this.findViewById<TextView>(R.id.profile).setOnClickListener{
                if(!isGuest) findNavController().navigate(R.id.userProfile) else showSignUpDialogue()
            }


            this.findViewById<TextView>(R.id.rate).setOnClickListener{
                rateUs()
            }


            this.findViewById<TextView>(R.id.shareUs).setOnClickListener{
              shareApp()
            }

            this.findViewById<TextView>(R.id.btn_logout).setOnClickListener{
                if(!isGuest)  promptLogoutConfirmation() else showSignUpDialogue()
            }
        }

//        binding.createFlag.setOnClickListener {
//            if(Permissions.isGpsEnabled() && Permissions.checkNetworkPermissions(mContext)){
//                findNavController().navigate(R.id.action_userMainScreen_to_userCreateFlag)
//            }else{
//                onMobileGps(mContext)
//            }
//
//        }
//




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









        return binding.root
    }



    private fun toggleLeftDrawer() {
        if (binding.drawerLayout.isDrawerOpen(binding.leftDrawerMenu)) {
            binding.drawerLayout.closeDrawer(binding.leftDrawerMenu)
        } else {
            binding.drawerLayout.openDrawer(binding.leftDrawerMenu)
        }
    }


    private fun promptLogoutConfirmation()  {
        //Use the context of current activity
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.logout_2)
        builder.setTitle("Logout!")
        builder.setMessage("Are you sure you want to logout ?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->

                //dont forget to clear any user related data in your preferences

                auth?.signOut()
                sp.edit().clear().apply()
                findNavController().navigate(R.id.action_global_startup)
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }



    private fun onMobileGps(context: Context) {
        // check whether device is GPS supported or not?
        if (Permissions.hasGPSDevice()) {
            // Supported

            // check whether is GPS on or not?
            if (Permissions.isGpsEnabled()) {
                // on

                // check whether  runtime permissions is enable on not?
                if (Permissions.checkNetworkPermissions(context)) {
                    // Enable
                    try {

                    } catch (e: Exception) {
                    }
                } else {
                    // not enable
                    // request permission now
                    Permissions.requestNetworkPermissions(context)
                }
            } else {
                // off
                showToast(context, "Gps not enabled")
                // send request to turn on Location(GPS)
                Permissions.enableGPS(context)
            }
        } else {
            // not Supported
            showToast(context, "Gps not Supported")
            //  finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.v("TAG1", "Fragment")
        when (requestCode) {
            Constants.REQUEST_GPS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.v("TAG1", "GPS on")

                    try {

                        onMobileGps(mContext)

                    } catch (e: Exception) {
                        Log.d(
                            "TAG1",
                            "" +
                                    "onActivityResult Error:${e.localizedMessage}"
                        )
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Log.v("TAG1", "GPS OFF")
                    Permissions.googleApiClient = null
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v("TAG1", "Permission result  fragment")

        when (requestCode) {
            Constants.MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.size > 0
                    && grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {
                    if (Permissions.checkNetworkPermissions(mContext)) {

//                        if (mGoogleApiClient == null) {
//                            buildGoogleApiClient()
//                        }
//                        mMap!!.isMyLocationEnabled = true

                        onMobileGps(mContext)
                        Log.v("TAG2", "onRequestPermissionsResult granted 196")
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "permission denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }


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
      //  mMap!!.setOnMarkerClickListener(this)
        mMap!!.setOnInfoWindowClickListener(this)
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
                mMap!!.isMyLocationEnabled = false
            } else {
                Permissions.requestNetworkPermissions(mContext)
            }
        } else {
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = false
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
        ToastUtils.showToast(mContext, "onConnectionSuspended")

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        ToastUtils.showToast(mContext, "onConnectionFailed")
    }


    override fun onLocationChanged(location: Location) {
        MapUtils.currLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Showing Current Location Marker on Map
        MapUtils.originLatitude = location.latitude
        MapUtils.originLongitude = location.longitude
        MapUtils.latLng = LatLng(location.latitude, location.longitude)

            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(8f))

            addCircle(mMap!!, latLng, binding.seekBar.progress, binding.tvStart)

            if(flagList != null){
                updateFlags()
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


    override fun onResume() {
        super.onResume()
        onMobileGps(requireContext())
        readDataFromFirestore()
    }


    private fun readDataFromFirestore() {

        mFirestore.collection("flags")
            .addSnapshotListener(object :EventListener<QuerySnapshot>{
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    // Convert the whole Query Snapshot to a list
                    // of objects directly! No need to fetch each
                    // document.

                    Log.v("TAG2","DATA :${value.toString()}")
                 flagList = value?.toObjects(Flag::class.java)
                if(MapUtils.currLocation != null)
                    updateFlags()
                }
            })

            auth?.let {
            mFirestore.collection("user").document(UID!!).get().addOnSuccessListener {
                MyApp.user = it.toObject(User::class.java)!!

                Picasso.get().load(MyApp.user.profileImageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(binding.leftDrawerMenu.findViewById<CircleImageView>(R.id.profile_image))
                binding.leftDrawerMenu.findViewById<TextView>(R.id.user_name).text = MyApp.user.name


            }

        }




     }

   fun updateFlags(){
       if(flagList != null) {
           for ((index, flag) in flagList!!.withIndex()) {
               var destLocation = Location("").apply {
                   latitude = flag.latitude
                   longitude = flag.longitude
               }
               var distance = MapUtils.currLocation!!.distanceTo(destLocation) / 1000

               if (distance <= binding.seekBar.progress) {
                   addMarker(flag.latitude, flag.longitude, mMap!!, requireContext(), index , flag.title , flag.flagNote)
                   Log.v("TAG6", "distancve :$distance")
               }
           }
       }
   }



    override fun onMarkerClick(marker: Marker): Boolean {
//        var bundle:Bundle = Bundle()
//        bundle.putSerializable(Constants.ARG_PARAM1, flagList?.get(marker.tag.toString().toInt()))
//
//        findNavController().navigate(R.id.action_userMainScreen_to_userFlagDetail,bundle)

        var flag:Flag = flagList?.get(marker.tag.toString().toInt())!!
        val action = UserMainScreenDirections.actionUserMainScreenToUserFlagDetail(flag)
        findNavController().navigate(action)


        return false
    }


    override fun onInfoWindowClick(marker: Marker) {
      //  ToastUtils.showToast(requireContext(), "info window click ${marker.tag.toString()}")
        var flag:Flag = flagList?.get(marker.tag.toString().toInt())!!
        val action = UserMainScreenDirections.actionUserMainScreenToUserFlagDetail(flag)
        findNavController().navigate(action)
    }


    private fun shareApp(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                "${shareMessage}https://play.google.com/store/apps/details?id=${mContext.packageName}".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: java.lang.Exception) {
            //e.toString();
        }
    }


    private fun rateUs(){
       // val uri: Uri = Uri.parse("market://details?id=${mContext.packageName}")
        val uri: Uri = Uri.parse("https://play.google.com/store/apps/details?id=${mContext.packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${mContext.packageName}")))
        }
    }


}