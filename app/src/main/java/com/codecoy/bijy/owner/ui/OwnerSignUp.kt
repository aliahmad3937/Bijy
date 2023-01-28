package com.codecoy.bijy.owner.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentSignUpBinding
import com.codecoy.bijy.owner.models.Owner
import com.codecoy.bijy.utils.ProgressUtil.hideProgress
import com.codecoy.bijy.utils.ProgressUtil.showProgress
import com.codecoy.bijy.utils.ToastUtils
import com.codecoy.bijy.utils.ToastUtils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


class OwnerSignUp : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var firebaseStore : FirebaseStorage
    private lateinit var storageReference  : StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var filePath : Uri

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                filePath = result.data!!.data!!
               // binding.imageView.setImageURI(filePath)
                binding.picImage.setText(filePath.toString())
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_ownerSignUp_to_ownerLogin)
        }

        binding.picImage.setOnClickListener {
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
                  showProgress(requireContext(), msg = getString(R.string.registered))

                     //   showToast(requireContext(),"Image Uploaded Successfully!")
                        auth.createUserWithEmailAndPassword(
                            binding.etEmail.text.toString(),
                            binding.etPassword.text.toString()
                        )
                            .addOnSuccessListener { task ->
                                val filepath = storageReference?.child(
                                    "owner/profileImages/" + task.user!!.uid
                                )
                                val uploadTask = filepath?.putFile(filePath!!)?.apply {
                                    addOnSuccessListener {

                                        filepath.downloadUrl.addOnSuccessListener { uri ->
                                            // enter data in database
                                            var owner = Owner(
                                                id = task.user!!.uid,
                                                shopName = binding.etNameOfShop.text.toString(),
                                                email = binding.etEmail.text.toString(),
                                                address = binding.etManualAddress.text.toString(),
                                                gpsAddress = binding.etGpdAddress.text.toString(),
                                                phoneNo = binding.etPhoneNumber.text.toString(),
                                                password = binding.etPassword.text.toString(),
                                                shopImageUrl = uri.toString()
                                            )

                                            mFirestore.collection("owner").document(owner.id)
                                                .set(owner)
                                                .addOnSuccessListener {
                                                    showToast(
                                                        requireContext(),
                                                        "Registered successfully!"
                                                    )
                                                    findNavController().navigate(R.id.action_ownerSignUp_to_ownerLogin)
                                                    Log.d(
                                                        "TAG",
                                                        "DocumentSnapshot successfully written!"
                                                    )
                                                    hideProgress()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.w("TAG", "Error writing document", e)
                                                    hideProgress()
                                                }
                                        }



                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                            showToast(requireContext(), exception.localizedMessage.toString())
                            hideProgress()
                        }

                }



            }


        return binding.root
    }

    fun checkValidation(): Boolean {
        var result: Boolean = true
        if (binding.etNameOfShop.text.toString().isEmpty()) {
            binding.etNameOfShop.error = "Name of Shop Required!"
            result = false
        }
        if (binding.etEmail.text.toString().isEmpty()) {
            binding.etEmail.error = "Email Required!"
            result = false
        }
        if (binding.etManualAddress.text.toString()
                .isEmpty() && binding.etGpdAddress.text.toString().isEmpty()
        ) {
            ToastUtils.showToast(requireContext(), "Enter either GPS OR Manual Address OR both!")
            binding.etGpdAddress.error = "GPS Address Required!"
            result = false
        }
        if (binding.etPhoneNumber.text.toString().isEmpty()) {
            binding.etPhoneNumber.error = "Phone Number Required!"
            result = false
        }
        if (binding.etPassword.text.toString().isEmpty()) {
            binding.etPassword.error = "Password Required!"
            result = false
        }
        if (binding.picImage.text.toString().isEmpty()) {
            binding.picImage.error = "Shop image Required!"
            result = false
        }

        return result
    }

}