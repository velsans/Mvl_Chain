package com.tadamaps.mobile.presentation.navigation

object Routes {
    const val Map = "map"
    const val LocationDetail = "location_detail"
    const val Booking = "booking"
    const val History = "history"

    fun locationDetail(slot: String) = "$LocationDetail/$slot"
}
