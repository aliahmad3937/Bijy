package com.codecoy.bijy.owner.models

import android.os.Parcel
import android.os.Parcelable
import com.codecoy.bijy.utils.Status
import com.codecoy.bijy.utils.UserType
import java.io.Serializable

class Flag() :Serializable {

    var flagID:String  = ""
    var userID:String  = ""
    var userName:String  = ""
    var latitude: Double  =0.0
    var longitude: Double =0.0
    var title: String = ""
    var flagNote: String = ""
    var address: String = ""
    var review: String = ""
    var flagStatus: String = ""
    var userType: String = ""
    var date: String = ""
    var time: String = ""
    var data:String = ""




    constructor(
        flagID:String,
        userID:String,
        latitude: Double,
        longitude: Double,
        title: String,
        address:String,
        flagStatus: String,
        userType: String,
        date:String,
        time:String,
        flagNote:String,
        data:String
    ) :this(){
        this.flagID = flagID
        this.userID = userID
        this.latitude = latitude
        this.longitude = longitude
        this.title = title
        this.flagStatus = flagStatus
        this.userType = userType
        this.date = date
        this.time = time
        this.address = address
        this.flagNote = flagNote
        this.date = data
    }

    constructor(
        flagID:String,
        userID:String,
        userName:String,
        latitude: Double,
        longitude: Double,
        title: String,
        address: String,
        flagStatus: String,
        userType: String,
        date:String,
        time:String,
        flagNote:String,
        data:String
    ) :this(){
        this.flagID = flagID
        this.userID = userID
        this.userName = userName
        this.latitude = latitude
        this.longitude = longitude
        this.title = title
        this.flagStatus = flagStatus
        this.userType = userType
        this.date = date
        this.time = time
        this.address = address
        this.flagNote = flagNote
        this.date = data
    }


    constructor(
        flagID:String,
        userID:String,
        latitude: Double,
        longitude: Double,
        title: String,
        address:String,
        flagStatus: String,
        userType: String,
        date:String,
        time:String

    ) :this(){
        this.flagID = flagID
        this.userID = userID
        this.latitude = latitude
        this.longitude = longitude
        this.title = title
        this.flagStatus = flagStatus
        this.userType = userType
        this.date = date
        this.time = time
        this.address = address

    }

    constructor(
        userID:String,
        flagID:String,
        flagStatus: String,
        date:String,
        time:String,
        title: String
    ) :this(){
        this.userID = userID
        this.flagID = flagID
        this.flagStatus = flagStatus
        this.date = date
        this.time = time
        this.title = title
    }

    constructor(
        userID:String,
        review: String
    ) :this(){
        this.userID = userID
        this.review = review
    }


}
