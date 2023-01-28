package com.codecoy.bijy.owner.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentOwnerCreateFlagBinding
import com.codecoy.bijy.databinding.FragmentOwnerFlagStatusBinding
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.utils.Constants
import com.codecoy.bijy.utils.MyApp
import com.codecoy.bijy.utils.ToastUtils
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot


class OwnerFlagStatus : Fragment() {

    private var flag: Flag? = null
    private lateinit var binding: FragmentOwnerFlagStatusBinding
    private lateinit var mFirestore: FirebaseFirestore
    private var userReviewList: MutableList<Flag> = arrayListOf()


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
        binding= DataBindingUtil.inflate(inflater , R.layout.fragment_owner_flag_status, container, false)
        mFirestore = FirebaseFirestore.getInstance()


        binding.toolbarFlagStatusOwner.apply {
            title.text = flag?.title
            logout.visibility = View.GONE
            back.apply {
                setImageResource(R.drawable.back_arrow)
                setOnClickListener {
                    findNavController().popBackStack()
                }
            }
        }

        flag?.let {
            binding.status.text = it.flagStatus
        }

        binding.changeStatus.setOnClickListener{
            var bundle:Bundle = Bundle()
            bundle.putSerializable(Constants.ARG_PARAM1,flag)
         MyApp.checkCreateRequest = Constants.UPDATE_STATUS

            findNavController().navigate(R.id.action_ownerFlagStatus_to_ownerCreateFlag,bundle)
        }

        binding.delete.setOnClickListener{
            promptdeleteConfirmation()
        }




        return binding.root
    }

    private fun promptdeleteConfirmation()  {
        //Use the context of current activity
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.delete)
        builder.setTitle("Delete!")
        builder.setMessage("Are you sure you want to delete this Flag?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->

                //dont forget to clear any user related data in your preferences

                mFirestore.collection("flags").document(flag!!.flagID)
                    .delete()
                    .addOnSuccessListener{
                        ToastUtils.showToast(requireContext(),"Flag deleted Successfully!")
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener{e-> Log.w("TAG","Error deleting document :${e.toString()}")}
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        readDataFromFirestore()
    }
    private fun readDataFromFirestore() {

        mFirestore.collection("flags")
            .document(flag!!.flagID)
            .collection("reviews")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    // Convert the whole Query Snapshot to a list
                    // of objects directly! No need to fetch each
                    // document.
                    userReviewList = value!!.toObjects(Flag::class.java)
                    updateReviews()
                }
            })




    }

    private fun updateReviews() {
        with(userReviewList.filter {
            it.review == "like"
        }) {
            binding.tvLike.text = this.size.toString()
//            if (this.filter { it.userID == UID }.isEmpty()) {
//                binding.likes.setImageResource(com.codecoy.bijy.R.drawable.like)
//            } else {
//                binding.likes.setImageResource(com.codecoy.bijy.R.drawable.like_fill)
//            }
        }

        with(userReviewList.filter {
            it.review == "dislike"
        }) {
            binding.tvDislike.text = this.size.toString()
//            if (this.filter { it.userID == UID }.isEmpty()) {
//                binding.dislike.setImageResource(com.codecoy.bijy.R.drawable.dislike)
//            } else {
//                binding.dislike.setImageResource(com.codecoy.bijy.R.drawable.dislike_fill)
//            }
        }

    }
}