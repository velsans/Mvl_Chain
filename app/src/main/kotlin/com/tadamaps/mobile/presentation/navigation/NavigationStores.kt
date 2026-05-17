package com.tadamaps.mobile.presentation.navigation

import com.mvlchain.domain.model.BookHistoryItem
import com.mvlchain.domain.model.BookingRequest
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight in-memory bridge for navigation because booking payloads exceed simple route args.
 */
@Singleton
class BookingNavigator @Inject constructor() {
    private val pending = AtomicReference<BookingRequest?>(null)

    fun enqueue(request: BookingRequest) {
        pending.set(request)
    }

    fun consume(): BookingRequest? = pending.getAndSet(null)
}

/**
 * Optional history restoration payload for map screen hydration.
 */
@Singleton
class MapRestorationStore @Inject constructor() {
    private val pending = AtomicReference<BookHistoryItem?>(null)

    fun enqueue(item: BookHistoryItem) {
        pending.set(item)
    }

    fun consume(): BookHistoryItem? = pending.getAndSet(null)
}
