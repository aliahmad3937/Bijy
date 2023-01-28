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
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentUserProfileBinding
import com.codecoy.bijy.owner.models.Owner
import com.codecoy.bijy.users.models.User
import com.codecoy.bijy.utils.MyApp
import com.codecoy.bijy.utils.ProgressUtil
import com.codecoy.bijy.utils.ToastUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class UserProfile : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var firebaseStore : FirebaseStorage
    private lateinit var storageReference  : StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var uri : Uri? = null

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                uri = result.data!!.data!!
                binding.profileImage.setImageURI(uri)


            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false)

        Picasso.get().load(MyApp.user.profileImageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(binding.profileImage)
        binding.etUserName.setText(MyApp.user.name)
        binding.etEmail.setText(MyApp.user.email)
        binding.etPassword.setText(MyApp.user.password)


        binding.profileImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(intent)
        }


        binding.toolbarProfileUser.apply {
            back.visibility = View.GONE
            logout.visibility = View.GONE
            title.text =  getString(R.string.profile)
        }

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser!!



        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        mFirestore = FirebaseFirestore.getInstance()





        binding.update.setOnClickListener {


            if(binding.etPassword.text.toString().length >= 6) {
                ProgressUtil.showProgress(requireContext(), msg = getString(R.string.updating_user))
                val credential = EmailAuthProvider
                    .getCredential(MyApp.user.email, MyApp.user.password)
// Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            user.updateEmail(binding.etEmail.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        user.updatePassword(binding.etPassword.text.toString())
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                    if (uri != null) {
                                                        val filepath = storageReference?.child(
                                                            "user/profileImages/" + MyApp.user.id
                                                        )
                                                        val uploadTask =
                                                            filepath?.putFile(uri!!)?.apply {
                                                                addOnSuccessListener {

                                                                    filepath.downloadUrl.addOnSuccessListener { uri ->
                                                                        // enter data in database
                                                                        updateDatabase(uri.toString())
                                                                    }

                                                                }
                                                            }
                                                    } else {
                                                        updateDatabase(MyApp.user.profileImageUrl)
                                                    }

                                                } else {
                                                    ProgressUtil.hideProgress()
                                                    ToastUtils.showToast(
                                                        requireContext(),
                                                        "password failure"
                                                    )
                                                    Log.v(
                                                        "TAG9",
                                                        "password error :${task.exception.toString()}"
                                                    )
                                                }
                                            }
                                    } else {
                                        ProgressUtil.hideProgress()
                                        ToastUtils.showToast(
                                            requireContext(),
                                            "email failure :${task.exception}"
                                        )
                                        Log.v("TAG9", "email error :${task.exception.toString()}")
                                    }
                                }
                        } else {
                            ProgressUtil.hideProgress()
                            binding.etEmail.error = "Email Already taken use Another!!"
                            Log.v(
                                "TAG9",
                                "reauthentication error :${it.exception.toString()}"
                            )
                        }
                    }

            }else{
                binding.etPassword.error = "Required atleast 6 characters!"
            }




        }
//

        return binding.root
    }



    fun updateDatabase(url:String){
        var user = User(
            id = MyApp.user.id,
            name = binding.etUserName.text.toString(),
            email = binding.etEmail.text.toString(),
            password = binding.etPassword.text.toString(),
            profileImageUrl = url
        )

        mFirestore.collection("user").document(user.id)
            .set(user)
            .addOnSuccessListener {
                ToastUtils.showToast(
                    requireContext(),
                    "Updated successfully!"
                )

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