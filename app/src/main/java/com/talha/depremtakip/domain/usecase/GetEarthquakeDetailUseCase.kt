package com.talha.depremtakip.domain.usecase

import javax.inject.Inject
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow

class GetEarthquakeDetailUseCase @Inject constructor(
    private val repository: EarthquakeRepository
) {
    operator fun invoke(id: String): Flow<Earthquake?> = repository.observeEarthquake(id)
}
