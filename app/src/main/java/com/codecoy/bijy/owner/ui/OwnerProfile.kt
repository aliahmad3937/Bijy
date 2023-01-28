package com.codecoy.bijy.owner.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentOwnerProfileBinding
import com.codecoy.bijy.databinding.FragmentSignUpBinding
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.owner.models.Owner
import com.codecoy.bijy.users.models.User
import com.codecoy.bijy.utils.MapUtils
import com.codecoy.bijy.utils.MyApp
import com.codecoy.bijy.utils.ProgressUtil
import com.codecoy.bijy.utils.ToastUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class OwnerProfile : Fragment() {
    private lateinit var binding: FragmentOwnerProfileBinding
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var uri: Uri? = null
    private lateinit var UID: String

    private var resultLaunche =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                uri = result.data!!.data!!
                binding.profileImage.setImageURI(uri)
                binding.picImage.setText(uri.toString())
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_owner_profile, container, false)
        binding.toolbarProfileOwner.apply {
            back.visibility = View.GONE
            title.text = getString(R.string.profile)
        }



        binding.picImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLaunche.launch(intent)
        }

        binding.toolbarProfileOwner.apply {
            back.visibility = View.GONE
            logout.visibility = View.GONE
            title.text =  getString(R.string.profile)
        }



        auth = FirebaseAuth.getInstance()
        UID = auth.currentUser!!.uid
        val user = auth.currentUser!!




        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        mFirestore = FirebaseFirestore.getInstance()

        readDataFromFirestore()
        binding.update.setOnClickListener {

            if (binding.etPassword.text.toString().length >= 6) {

                ProgressUtil.showProgress(requireContext(), msg = getString(R.string.updating_user))
                val credential = EmailAuthProvider
                    .getCredential(MyApp.owner!!.email, MyApp.owner!!.password)
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
                                                            "owner/profileImages/" + MyApp.owner!!.id
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
                                                        updateDatabase(MyApp.owner!!.shopImageUrl)
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

            } else {
                binding.etPassword.error = "Required atleast 6 characters!"
            }


        }


        return binding.root
    }

    private fun updateViews() {
        Picasso.get().load(MyApp.owner?.shopImageUrl).placeholder(R.drawable.avatar)
            .error(R.drawable.avatar).into(binding.profileImage)
        binding.etManualAddress.setText(MyApp.owner?.address)
        binding.etGpdAddress.setText(MyApp.owner?.gpsAddress)
        binding.etPhoneNumber.setText(MyApp.owner?.phoneNo)
        binding.etNameOfShop.setText(MyApp.owner?.shopName)
        binding.picImage.setText(MyApp.owner?.shopImageUrl)
        binding.etEmail.setText(MyApp.owner?.email)
        binding.etPassword.setText(MyApp.owner?.password)
    }


    fun updateDatabase(url: String) {
        var owner = Owner(
            id = MyApp.owner!!.id,
            shopName = binding.etNameOfShop.text.toString(),
            email = binding.etEmail.text.toString(),
            address = binding.etManualAddress.text.toString(),
            gpsAddress = binding.etGpdAddress.text.toString(),
            phoneNo = binding.etPhoneNumber.text.toString(),
            password = binding.etPassword.text.toString(),
            shopImageUrl = url
        )

        mFirestore.collection("owner").document(owner.id)
            .set(owner)
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




    private fun readDataFromFirestore() {

        mFirestore.collection("owner").document(UID).get().addOnSuccessListener {
            MyApp.owner = it.toObject(Owner::class.java)!!
             updateViews()
        }
    }
}