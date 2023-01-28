package com.codecoy.bijy.owner.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecoy.bijy.MainActivity
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentOwnerMainScreenBinding
import com.codecoy.bijy.owner.adapters.OwnerFlagHistory
import com.codecoy.bijy.owner.callBacks.OwnerFlagCallBack
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.owner.models.Owner
import com.codecoy.bijy.users.models.User
import com.codecoy.bijy.utils.*
import com.codecoy.bijy.utils.Constants.REQUEST_GPS
import com.codecoy.bijy.utils.ToastUtils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class OwnerMainScreen : Fragment(), OwnerFlagCallBack {
    private lateinit var binding: FragmentOwnerMainScreenBinding
    private lateinit var adapter: OwnerFlagHistory
    private lateinit var mContext: Context

    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var UID: String
    private lateinit var sp: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_owner_main_screen, container, false)
        sp = requireContext().getSharedPreferences(
            getString(R.string.my_preferences),
            Context.MODE_PRIVATE
        )

        MyApp.homeFragment = this
        mFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        auth = FirebaseAuth.getInstance()
        UID = auth.currentUser!!.uid.toString()
        MyApp.locationManager =
            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

//
//        binding.createFlag.setOnClickListener {
//            onMobileGps(mContext)
//
//        }


        binding.toolbarOwnerMain.apply {
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
                findNavController().navigate(R.id.ownerMainScreen)
            }

            this.findViewById<TextView>(R.id.profile).setOnClickListener{
               findNavController().navigate(R.id.ownerProfile)
            }


            this.findViewById<TextView>(R.id.rate).setOnClickListener{
                rateUs()
            }


            this.findViewById<TextView>(R.id.shareUs).setOnClickListener{
                shareApp()
            }

            this.findViewById<TextView>(R.id.btn_logout).setOnClickListener{
                promptLogoutConfirmation()
            }
        }


        adapter = OwnerFlagHistory(MyApp.flagList, requireContext(), this)
        binding.recyclerFlagOwner.layoutManager = LinearLayoutManager(mContext)
        binding.recyclerFlagOwner.setHasFixedSize(true)
        binding.recyclerFlagOwner.adapter = adapter

        if (MyApp.flagList.isEmpty()) {
            binding.recyclerFlagOwner.visibility = View.GONE
            binding.tvNothing.visibility = View.VISIBLE
        } else {
            binding.recyclerFlagOwner.visibility = View.VISIBLE
            binding.tvNothing.visibility = View.GONE
        }


        MyApp.flag.observe(viewLifecycleOwner) {
            if (!MyApp.flagList.contains(it)) {
                MyApp.flagList.add(it)
                adapter.notifyDataSetChanged()
            }
            if (MyApp.flagList.isEmpty()) {
                binding.recyclerFlagOwner.visibility = View.GONE
                binding.tvNothing.visibility = View.VISIBLE
            } else {
                binding.recyclerFlagOwner.visibility = View.VISIBLE
                binding.tvNothing.visibility = View.GONE
            }
        }




        return binding.root
    }

    private fun promptLogoutConfirmation() {
        //Use the context of current activity
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.logout_2)
        builder.setTitle("Logout!")
        builder.setMessage("Are you sure you want to logout ?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->

                //dont forget to clear any user related data in your preferences

                auth.signOut()
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
                        findNavController().navigate(R.id.action_ownerMainScreen_to_ownerCreateFlag)
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

    override fun onAttach(context: Context) {
        mContext = context as MainActivity

        super.onAttach(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.v("TAG1", "Fragment")
        when (requestCode) {
            REQUEST_GPS -> when (resultCode) {
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

    override fun onFlagItemClick(flag: Flag) {
        var bundle: Bundle = Bundle()
        bundle.putSerializable(Constants.ARG_PARAM1, flag)


        findNavController().navigate(R.id.action_ownerMainScreen_to_ownerFlagStatus, bundle)
    }


    override fun onResume() {
        super.onResume()
        readDataFromFirestore()
    }


    private fun readDataFromFirestore() {


        mFirestore.collection("flags")
            .whereEqualTo("userType", "Owner")
            .whereEqualTo("userID", UID)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    // Convert the whole Query Snapshot to a list
                    // of objects directly! No need to fetch each
                    // document.
                    var list: List<Flag> = ArrayList<Flag>()
                    if (value != null) {
                         list = value!!.toObjects(Flag::class.java)
                         }
                        if (list.isEmpty()) {
                            binding.recyclerFlagOwner.visibility = View.GONE
                            binding.tvNothing.visibility = View.VISIBLE
                        } else {
                            binding.recyclerFlagOwner.visibility = View.VISIBLE
                            binding.tvNothing.visibility = View.GONE
                        }

                        adapter.updateData(list as MutableList<Flag>)
                    }

            })


        mFirestore.collection("owner").document(UID).get().addOnSuccessListener {
            MyApp.owner = it.toObject(Owner::class.java)!!

            Picasso.get().load(MyApp.owner!!.shopImageUrl).placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(binding.leftDrawerMenu.findViewById<CircleImageView>(R.id.profile_image))
            binding.leftDrawerMenu.findViewById<TextView>(R.id.user_name).text =
                MyApp.owner!!.shopName

        }

    }

    private fun toggleLeftDrawer() {
        if (binding.drawerLayout.isDrawerOpen(binding.leftDrawerMenu)) {
            binding.drawerLayout.closeDrawer(binding.leftDrawerMenu)
        } else {
            binding.drawerLayout.openDrawer(binding.leftDrawerMenu)
        }
    }


    private fun shareApp() {
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


    private fun rateUs() {
        // val uri: Uri = Uri.parse("market://details?id=${mContext.packageName}")
        val uri: Uri =
            Uri.parse("https://play.google.com/store/apps/details?id=${mContext.packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${mContext.packageName}")
                )
            )
        }
    }


}