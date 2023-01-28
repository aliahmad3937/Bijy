package com.codecoy.bijy.users.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentUserSignUpBinding
import com.codecoy.bijy.users.models.User
import com.codecoy.bijy.utils.ProgressUtil
import com.codecoy.bijy.utils.ToastUtils
import com.codecoy.bijy.utils.ToastUtils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


class UserSignUp : Fragment() {
    private lateinit var binding: FragmentUserSignUpBinding

    private lateinit var firebaseStore : FirebaseStorage
    private lateinit var storageReference  : StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var filePath : Uri? = null

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                filePath = result.data!!.data!!
                binding.profileImage.setImageURI(filePath)

            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_sign_up, container, false)

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_userSignUp_to_userLogin)
        }

        binding.profileImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(intent)
        }

        auth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        mFirestore = FirebaseFirestore.getInstance()

        binding.signup.setOnClickListener {
            if (checkValidation()) {
                ProgressUtil.showProgress(requireContext(), msg = getString(R.string.registered))
                        //   showToast(requireContext(),"Image Uploaded Successfully!")
                        auth.createUserWithEmailAndPassword(
                            binding.etEmail.text.toString(),
                            binding.etPassword.text.toString()
                        )
                            .addOnSuccessListener { task ->

                                val filepath = storageReference.child(
                                    "user/profileImages/" + task.user!!.uid
                                )
                                val uploadTask = filepath?.putFile(filePath!!)?.apply {
                                    addOnSuccessListener {

                                        filepath.downloadUrl.addOnSuccessListener { uri ->
                                            // enter data in database
                                            var user = User(
                                                id = task.user!!.uid,
                                                name = binding.etConfirmPassword.text.toString(),
                                                email = binding.etEmail.text.toString(),
                                                password = binding.etPassword.text.toString(),
                                                profileImageUrl = uri.toString()
                                            )

                                            mFirestore.collection("user").document(user.id)
                                                .set(user)
                                                .addOnSuccessListener {
                                                    ToastUtils.showToast(
                                                        requireContext(),
                                                        "Registered successfully!"
                                                    )
                                                    findNavController().navigate(R.id.action_userSignUp_to_userLogin)
                                                    Log.d(
                                                        "TAG",
                                                        "DocumentSnapshot successfully written!"
                                                    )
                                                    ProgressUtil.hideProgress()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.w("TAG", "Error writing document", e)
                                                    ProgressUtil.hideProgress()
                                                }
                                        }

                                    }
                                    addOnFailureListener {
                                        showToast(
                                            requireContext(),
                                            "${it.localizedMessage}")
                                        ProgressUtil.hideProgress()
                                    }
                                }
                            }
                            .addOnFailureListener{
                        showToast(
                            requireContext(),
                            "${it.localizedMessage}"
                        )
                                ProgressUtil.hideProgress()

                    }

            }

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
        if (binding.etConfirmPassword.text.toString().isEmpty()) {
            binding.etConfirmPassword.error = "Username Required!"
            result = false
        }



        if(filePath == null){
            showToast(requireContext(), getString(R.string.upload_profile_image))
            result = false
        }


        return result
    }

}