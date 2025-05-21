package com.geobus.marrakech.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modèle représentant une estimation de temps de trajet
 */
@Parcelize
data class TimeEstimation(
    val stopId: Long,
    val stopName: String,
    val distance: Double,
    val walkingTimeMinutes: Double
) : Parcelable