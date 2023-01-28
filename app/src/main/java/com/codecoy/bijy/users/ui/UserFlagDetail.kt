package com.codecoy.bijy.users.ui

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.codecoy.bijy.R
import com.codecoy.bijy.databinding.FragmentUserFlagDetailBinding
import com.codecoy.bijy.owner.models.Flag
import com.codecoy.bijy.users.adapters.UserStatuses
import com.codecoy.bijy.utils.*
import com.codecoy.bijy.utils.MyApp.Companion.isGuest

import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging

import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.util.*


class UserFlagDetail : Fragment() {

    private lateinit var flag: Flag
    private lateinit var mContext: Context
    private var userStatusList: MutableList<Flag> = arrayListOf()
    private var userReviewList: MutableList<Flag> = arrayListOf()
    private lateinit var binding: FragmentUserFlagDetailBinding

    private lateinit var mFirestore: FirebaseFirestore
    private  var auth: FirebaseAuth? = null
    private  var UID: String? = null
    private lateinit var name: String
    private var flagStatus: String = "Busy"
    private lateinit var adapter: UserStatuses

    val args: UserFlagDetailArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flag = args.flag

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_user_flag_detail, container, false)

        mFirestore = FirebaseFirestore.getInstance()

        if(flag.userType == "User"){

                binding.ownerGroup.visibility = View.GONE
        }else{
            binding.switch1.visibility = View.GONE
          //  binding.textView6.visibility = View.GONE
           // binding.tv3.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
            binding.radioGroup.visibility = View.GONE
            binding.tv2.visibility = View.GONE
            binding.layoutUserStatus.visibility = View.GONE
        }

        if(!isGuest) {
            auth = FirebaseAuth.getInstance()
            UID = auth!!.currentUser!!.uid.toString()
        }



        binding.tvDate.text = flag.date
        binding.tvTime.text = flag.time
        binding.tvStatus.text = "${flag.userName} Created Flag ${flag.flagStatus}"


        binding.toolbarFlagDetailUser.apply {
            title.text = flag.title
            logout.visibility = View.GONE
            back.apply {
                setImageResource(R.drawable.back_arrow)
                setOnClickListener {
                    findNavController().popBackStack()
                }
            }

        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Responds to child RadioButton checked/unchecked
            when (checkedId) {
                R.id.radio_busy -> {
                    flagStatus = "Busy"
                }
                R.id.radio_very_busy -> {
                    flagStatus = "Very Busy"
                }
                R.id.radio_not_busy -> {

                    flagStatus = "Not Busy"
                }

            }
        }



        binding.btnSave.setOnClickListener {
            if(!isGuest) {
                var destLocation = Location("").apply {
                    latitude = flag.latitude
                    longitude = flag.longitude
                }
                var distance = MapUtils.currLocation!!.distanceTo(destLocation)

                if(distance <=  100){
                    val dateObject = Date(System.currentTimeMillis())
                    val calendarInstance = Calendar.getInstance()
                    calendarInstance.time = dateObject
                    val hour = calendarInstance.get(Calendar.HOUR)
                    val minute = calendarInstance.get(Calendar.MINUTE)
                    val ampm = if (calendarInstance.get(Calendar.AM_PM) == 0) "AM " else "PM "
                    val date = calendarInstance.get(Calendar.DATE)
                    val month = calendarInstance.get(Calendar.MONTH)
                    val year = calendarInstance.get(Calendar.YEAR)

                    val randomID = UUID.randomUUID().toString()


                    sendNotification("$name Voted $flagStatus")


                    val flag2 = Flag(
                        userID = UID!!,
                        flagID = randomID,
                        flagStatus = flagStatus.toString(),
                        date = "$date/$month/$year",
                        time = "${if (hour == 0) "12" else {if (hour <= 9) "0$hour" else hour}}:${if (minute <= 9) "0$minute" else minute}",
                        title = name
                    )
                    mFirestore
                        .collection("flags")
                        .document(flag.flagID)
                        .collection("userStatuses")
                        .document(randomID)
                        .set(flag2)
                        .addOnSuccessListener {
                            ToastUtils.showToast(
                                requireContext(),
                                getString(R.string.status_added_successfully)
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "Error writing document", e)
                            ProgressUtil.hideProgress()
                        }
                }else{
                    Toast.makeText(requireContext(),"User Must be in 100 Meter Radius Of This Place!",Toast.LENGTH_LONG).show()
                }

            }else{
                showSignUpDialogue()
            }

        }

        binding.layoutLikes.setOnClickListener {
             if(!isGuest) {
                 binding.likes.setImageResource(R.drawable.like_fill)
                 binding.dislike.setImageResource(R.drawable.dislike)

                 val flag2 = Flag(
                     userID = UID!!,
                     review = "like"
                 )
                 mFirestore
                     .collection("flags")
                     .document(flag.flagID)
                     .collection("reviews")
                     .document(UID!!)
                     .set(flag2)
                     .addOnSuccessListener {
//                            ToastUtils.showToast(
//                                requireContext(),
//                              "liked successfully"
//                            )
                     }
                     .addOnFailureListener { e ->
                         Log.w("TAG", "Error writing document", e)

                     }
             }else{
                 showSignUpDialogue()
             }

        }
        binding.layoutDislike.setOnClickListener {
            if(!isGuest) {
                binding.dislike.setImageResource(R.drawable.dislike_fill)
                binding.likes.setImageResource(R.drawable.like)

                val flag2 = Flag(
                    userID = UID!!,
                    review = "dislike"
                )
                mFirestore
                    .collection("flags")
                    .document(flag.flagID)
                    .collection("reviews")
                    .document(UID!!)
                    .set(flag2)
                    .addOnSuccessListener {
//                            ToastUtils.showToast(
//                                requireContext(),
//                              "disliked successfully"
//                            )
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error writing document", e)

                    }
            }else{
                showSignUpDialogue()
            }

        }

        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isGuest) {
                if (isChecked) {
                    Log.v("TAG7", "token :true")

                    val flag2 = Flag(
                        userID = UID!!,
                        review = "subscribe"
                    )
                    mFirestore
                        .collection("flags")
                        .document(flag.flagID)
                        .collection("subscription")
                        .document(UID!!)
                        .set(flag2)
                        .addOnSuccessListener {
                            subscribeTopic(flag.flagID)
                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "subscription failed", e)
                        }

                } else {
                    Log.v("TAG7", "token :false")


                    mFirestore.collection("flags")
                        .document(flag.flagID)
                        .collection("subscription")
                        .document(UID!!)
                        .delete()
                        .addOnSuccessListener {
                            unSubscribeTopic(flag.flagID)
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "TAG",
                                "Error deleting document :${e.toString()}"
                            )
                        }
                }
            }else{
                binding.switch1.isChecked = false
                showSignUpDialogue()
            }
        }








        adapter = UserStatuses(userStatusList, requireContext())
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.setHasFixedSize(true)
        binding.recycler.adapter = adapter
        updateRecycler()



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        readDataFromFirestore()
    }

    private fun updateRecycler() {
        if (userStatusList.isEmpty()) {
            binding.recycler.visibility = View.GONE
            binding.tvNothing.visibility = View.VISIBLE
        } else {
            binding.recycler.visibility = View.VISIBLE
            binding.tvNothing.visibility = View.GONE
            adapter.updateData(userStatusList)
        }
    }


    private fun readDataFromFirestore() {

        if(!isGuest) {
            mFirestore.collection("user").document(UID!!).get().addOnSuccessListener {
                name = it.get("name").toString()
            }

            mFirestore
                .collection("flags")
                .document(flag.flagID)
                .collection("subscription")
                .whereEqualTo("userID", UID!!)
                .get()
                .addOnSuccessListener {
                    val list = it.toObjects(Flag::class.java)
                    binding.switch1.isChecked = list.isNotEmpty()

                }
        }

        mFirestore.collection("flags")
            .document(flag.flagID)
            .collection("userStatuses")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    // Convert the whole Query Snapshot to a list
                    // of objects directly! No need to fetch each
                    // document.
                    userStatusList = value!!.toObjects(Flag::class.java)
                    updateRecycler()
                }
            })

        mFirestore.collection("flags")
            .document(flag.flagID)
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
            if (this.filter { it.userID == UID }.isEmpty()) {
                binding.likes.setImageResource(R.drawable.like)
            } else {
                binding.likes.setImageResource(R.drawable.like_fill)
            }
        }

        with(userReviewList.filter {
            it.review == "dislike"
        }) {
            binding.tvDislike.text = this.size.toString()
            if (this.filter { it.userID == UID }.isEmpty()) {
                binding.dislike.setImageResource(R.drawable.dislike)
            } else {
                binding.dislike.setImageResource(R.drawable.dislike_fill)
            }
        }

    }

    // TODO: Step 3.3 subscribe to breakfast topic
    private fun subscribeTopic(topic: String) {
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = mContext.getString(R.string.message_subscribed)
                if (!task.isSuccessful) {
                    msg = mContext.getString(R.string.message_subscribe_failed)
                }
              //  ToastUtils.showToast(mContext, msg)
            }
        // [END subscribe_topics]
    }

    // TODO: Step 3.3 subscribe to breakfast topic
    private fun unSubscribeTopic(topic: String) {
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = getString(R.string.subscription_off)
                if (!task.isSuccessful) {
                    msg = "Un subscribe failure"
                }
               // ToastUtils.showToast(requireContext(), msg)
            }
        // [END subscribe_topics]

    }


    fun sendNotification(msg:String) {

        Log.v("TAG1", "sendNotification")
        val mRequestQue = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            json.put("to", "/topics/" + flag.flagID)

               val notificationObj = JSONObject()
            notificationObj.put("flagID", flag.flagID)
            notificationObj.put("userID", flag.userID)
            notificationObj.put("latitude", flag.latitude)
            notificationObj.put("longitude", flag.longitude)
            notificationObj.put("title", flag.title)
            notificationObj.put("address", flag.address)
            notificationObj.put("flagStatus", flag.flagStatus)
            notificationObj.put("userType", flag.userType)
            notificationObj.put("date", flag.date)
            notificationObj.put("time", flag.time)
            notificationObj.put("userName", flag.userName)
            notificationObj.put("flagNote", flag.flagNote)
            notificationObj.put("data", flag.data)
            notificationObj.put("msg", msg)
//            Log.v("TAG5", "sendNotification   mobileno :" +  contactsModel.getMobileNo())
//            Log.v("TAG5", "sendNotification   accountname :" + contactsModel.getAccountName())
            //replace notification with data when went send data
         //   json.put("notification", notificationObj)
            json.put("data", notificationObj)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, URL,
                json,
                Response.Listener { response: JSONObject? ->
                    Log.d(
                        "TAG2",
                        "onResponse: "
                    )
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.d(
                        "TAG2",
                        "onError: " + error.networkResponse
                    )
                }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] =
                        "key=AAAAOKhGulA:APA91bF6xwRuMJKT78lR2QXDQ9BldZfqetl2ZC_CMcoCB1W3HCbTqNTkZ38d8Z3yyUr4SvTJ16zHpEccP0Gqqa46YHNICnkq-y9cOGtY00DNl4_zs-07Wn94boyxm2O9Lmc99GXH0y0f"
                    return header
                }
            }
            mRequestQue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }




}