package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.repository.LocationPreferenceRepository
import kotlinx.coroutines.flow.Flow

class SaveNicknameUseCase(
    private val preferences: LocationPreferenceRepository,
) {
    suspend operator fun invoke(slot: LocationSlot, nickname: String?) {
        preferences.saveNickname(slot, nickname?.trim()?.takeIf { it.isNotEmpty() })
    }
}

class ObserveNicknameUseCase(
    private val preferences: LocationPreferenceRepository,
) {
    operator fun invoke(slot: LocationSlot): Flow<String?> = preferences.nicknameFlow(slot)
}
