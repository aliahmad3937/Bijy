package com.codecoy.bijy.users.models

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var profileImageUrl: String = ""
)