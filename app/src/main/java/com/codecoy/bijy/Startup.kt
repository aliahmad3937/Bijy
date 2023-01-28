package com.codecoy.bijy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.databinding.FragmentStartupBinding
import com.codecoy.bijy.utils.Constants.USER_TYPE
import com.codecoy.bijy.utils.InternalDeepLink
import com.codecoy.bijy.utils.MyApp
import com.codecoy.bijy.utils.UserType
import com.google.firebase.auth.FirebaseAuth


class Startup : Fragment() {
    private lateinit var binding: FragmentStartupBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var sp: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater , R.layout.fragment_startup, container, false)

        auth = FirebaseAuth.getInstance()
        sp = requireContext().getSharedPreferences(getString(R.string.my_preferences), Context.MODE_PRIVATE)

        binding.btnUser.setOnClickListener{
            findNavController().navigate(R.id.action_startup_to_user)
        }
        binding.btnOwner.setOnClickListener{
            findNavController().navigate(R.id.action_startup_to_owner)
        }


        binding.btnGuess.setOnClickListener{
            MyApp.isGuest = true
            val deepLink = InternalDeepLink.USER_MAIN_SCREEN.toUri()
            findNavController().popBackStack()
            findNavController().navigate(deepLink)

        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.v("TAG8","onResume")
        if(FirebaseAuth.getInstance().currentUser != null){
            var user:String = sp.getString(USER_TYPE,"").toString()

            if(user.isNotEmpty() && user == UserType.User.toString()){
                val deepLink = InternalDeepLink.USER_MAIN_SCREEN.toUri()
                findNavController().popBackStack()
                findNavController().navigate(deepLink)
            }else{
                val deepLink = InternalDeepLink.OWNER_MAIN_SCREEN.toUri()
                findNavController().popBackStack()
                findNavController().navigate(deepLink)

            }

        }
    }







}