package com.codecoy.bijy.owner.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentLoginBinding
import com.codecoy.bijy.databinding.FragmentStartupBinding
import com.codecoy.bijy.utils.Constants.USER_TYPE
import com.codecoy.bijy.utils.ProgressUtil
import com.codecoy.bijy.utils.ProgressUtil.hideProgress
import com.codecoy.bijy.utils.ToastUtils
import com.codecoy.bijy.utils.UserType
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth


class OwnerLogin : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var sp: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater , R.layout.fragment_login, container, false)
        sp = requireContext().getSharedPreferences(getString(R.string.my_preferences), Context.MODE_PRIVATE)

        auth = FirebaseAuth.getInstance()

        binding.tvSignup.setOnClickListener{
            findNavController().navigate(R.id.action_ownerLogin_to_ownerSignUp)
        }
        binding.login.setOnClickListener{
            if (checkValidation()) {
                ProgressUtil.showProgress(requireContext())
                auth.signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString()).addOnCompleteListener{
                 if(it.isSuccessful){
                     hideProgress()
                     sp.edit().putString(USER_TYPE,UserType.Owner.toString()).apply()
                     findNavController().navigate(R.id.action_ownerLogin_to_ownerMainScreen)
                 }else{
                     ToastUtils.showToast(requireContext(), "Fail")
                   hideProgress()
                 }
                }
            }

        }


        binding.textView3.setOnClickListener {

//            auth.sendPasswordResetEmail(email)
//                .addOnCompleteListener(this, OnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG)
//                            .show()
//                    } else {
//                        Toast.makeText(this, "Unable to send reset mail", Toast.LENGTH_LONG)
//                            .show()
//                    }
//                })

        }




        return binding.root
    }

    fun checkValidation(): Boolean {
        var result: Boolean = true

        if (binding.etEmail.text.toString().isEmpty()) {
            binding.etEmail.error = "Email Required!"
            result = false
        }

        if (binding.etPassword.text.toString().isEmpty()) {
            binding.etPassword.error = "Password Required!"
            result = false
        }


        return result
    }

    override fun onResume() {
        super.onResume()
        var user:String = sp.getString(USER_TYPE,"").toString()
        if(user.isNotEmpty() && FirebaseAuth.getInstance().currentUser != null){
            findNavController().navigate(R.id.action_ownerLogin_to_ownerMainScreen)
        }
    }

}