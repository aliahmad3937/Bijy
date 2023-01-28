package com.codecoy.bijy.utils

object InternalDeepLink {
    const val DOMAIN = "bijy://"

    const val OWNER_MAIN_SCREEN = "${DOMAIN}owner-main-screen"
    const val USER_MAIN_SCREEN = "${DOMAIN}user-main-screen"
    const val USER_SIGNUP_SCREEN = "${DOMAIN}user-signup-screen"


    fun makeCustomDeepLink(id: String): String {
        return "${DOMAIN}customDeepLink?id=${id}"
    }
}