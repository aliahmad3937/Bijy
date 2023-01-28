package com.codecoy.bijy

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.ActivityMainBinding
import com.codecoy.bijy.utils.*
import com.codecoy.bijy.utils.Constants.REQUEST_GPS
import com.codecoy.bijy.utils.MyApp.Companion.isGuest
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceOnClickListener

class MainActivity : AppCompatActivity() {
    // Initialize variables
    lateinit var binding: ActivityMainBinding
    var navController: NavController? = null
    private lateinit var sp: SharedPreferences
    private lateinit var user: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assign variable
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        sp = getSharedPreferences(getString(R.string.my_preferences), Context.MODE_PRIVATE)

        Log.v("TAG8","onCreate")
        if(intent.hasExtra("from")){
            Log.v("TAG8","onCreate Activity:${intent.getStringExtra("from")}")
        }else{
            Log.v("TAG8","onCreate Activity null")
        }



        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        navController = navHostFragment!!.navController

        addingTabsInBottomBar()


        navController!!.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.startup
                || destination.id == R.id.ownerLogin
                || destination.id == R.id.ownerSignUp
                || destination.id == R.id.userLogin
                || destination.id == R.id.userSignUp

            ) {
                updateStatusBarColor2("#2B2B52")
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                updateStatusBarColor2("#4c4c87")
                binding.bottomNavigationView.visibility = View.VISIBLE

            }


        }


    }


    // change action bar color with text color black
    fun updateStatusBarColor(color: String?) { // Color must be in hexadecimal fromat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.parseColor(color)
        }
    }


    // change action bar color with text color white
    fun updateStatusBarColor2(color: String?) { // Color must be in hexadecimal fromat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
            window.statusBarColor = Color.parseColor(color)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.v("TAG1", "Main Activity")
        if (requestCode == REQUEST_GPS) {
            MyApp.homeFragment.onActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.v("TAG1","Permission result  activity")
        MyApp.homeFragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addingTabsInBottomBar() {

        binding.bottomNavigationView.addSpaceItem(
            SpaceItem(getString(R.string.create_flag),
            R.drawable.add)
        )
        binding.bottomNavigationView.addSpaceItem(
            SpaceItem(getString(R.string.profile),
            R.drawable.person)
        )

        binding.bottomNavigationView.showIconOnly()
       binding.bottomNavigationView.setCentreButtonSelectable(true)
        binding.bottomNavigationView.setCentreButtonSelected()
        binding.bottomNavigationView.setActiveCentreButtonBackgroundColor(R.color.purple_700)








        binding.bottomNavigationView.setSpaceOnClickListener(object : SpaceOnClickListener {
            override fun onCentreButtonClick() {
                if(!isGuest && user == UserType.Owner.toString()) {

                    navController!!.navigate(R.id.ownerMainScreen)
                    binding.bottomNavigationView.setCentreButtonSelectable(false)
//                binding.bottomNavigationView.setCentreButtonSelected()
                }else{
                    navController!!.navigate(R.id.userMainScreen)
                    binding.bottomNavigationView.setCentreButtonSelectable(false)
                }


                //  Toast.makeText(this@MainActivity, "onCentreButtonClick", Toast.LENGTH_SHORT).show()
            }

            override fun onItemClick(itemIndex: Int, itemName: String) {
                when (itemIndex) {
                    0 -> if(!isGuest){
                        user = sp.getString(Constants.USER_TYPE,"").toString()
                        if(user == UserType.User.toString()){
                            navController!!.navigate(R.id.userCreateFlag)
                        }else{
                            navController!!.navigate(R.id.ownerCreateFlag)
                        }
                    } else { showSignUpDialogue() }


               1 ->  if(!isGuest){
                   user = sp.getString(Constants.USER_TYPE,"").toString()
                    if(user == UserType.User.toString()){
                        navController!!.navigate(R.id.userProfile)
                    }else{
                        navController!!.navigate(R.id.ownerProfile)
                    }
                } else { showSignUpDialogue() }
            }

                //  Toast.makeText(this@MainActivity, "$itemIndex $itemName", Toast.LENGTH_SHORT).show()
            }

            override fun onItemReselected(itemIndex: Int, itemName: String) {
                // Toast.makeText(this@MainActivity, "$itemIndex $itemName", Toast.LENGTH_SHORT).show()
                when (itemIndex) {
                    0 -> if(!isGuest){
                        user = sp.getString(Constants.USER_TYPE,"").toString()
                        if(user == UserType.User.toString()){
                            navController!!.navigate(R.id.userCreateFlag)
                        }else{
                            navController!!.navigate(R.id.ownerCreateFlag)
                        }
                    } else { showSignUpDialogue() }


                    1 ->  if(!isGuest){
                        user = sp.getString(Constants.USER_TYPE,"").toString()
                        if(user == UserType.User.toString()){
                            navController!!.navigate(R.id.userProfile)
                        }else{
                            navController!!.navigate(R.id.ownerProfile)
                        }
                    } else { showSignUpDialogue() }
                }

            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        binding.bottomNavigationView.onSaveInstanceState(outState);
    }

    fun showSignUpDialogue(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.warning)
        builder.setTitle("SignUp Required!")
        builder.setMessage("Do you want to SignUp ?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->

                //dont forget to clear any user related data in your preferences

                val deepLink = InternalDeepLink.USER_SIGNUP_SCREEN.toUri()
              navController!!.popBackStack()
               navController!!.navigate(deepLink)
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onResume() {
        Log.v("TAG8","onResume Activity:")
        user  = sp.getString(Constants.USER_TYPE,"").toString()
            Log.v("TAG8","onResume Activity${intent.getStringExtra("from")}")


        super.onResume()
    }


    override fun onNewIntent(intent: Intent?) {
        Log.v("TAG8","onNewIntent")
        super.onNewIntent(intent)

    }



}