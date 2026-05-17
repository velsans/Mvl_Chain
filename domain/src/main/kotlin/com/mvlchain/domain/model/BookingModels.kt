package com.mvlchain.domain.model

/**
 * Request payload for creating a booking between two locations.
 */
data class BookingRequest(
    val locationA: MapLocation,
    val locationB: MapLocation,
)

/**
 * Server response for a successful booking (mock or real).
 */
data class BookingResult(
    val id: String?,
    val locationA: MapLocation,
    val locationB: MapLocation,
    val price: Double,
)

/**
 * Historical booking row returned by [BookRepository.getHistory].
 */
data class BookHistoryItem(
    val id: String,
    val locationA: MapLocation,
    val locationB: MapLocation,
    val price: Double,
    val year: Int,
    val month: Int,
)
