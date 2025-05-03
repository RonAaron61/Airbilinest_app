package com.mka.airbilinest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patient(
    val id: String,
    val name: String,
    var weight: String,
    val birthDate: String,
    var age: Int,
    var bilirubinStart: String,
    var lengthOfPhototherapy: String,
    var phototherapyIntensity: String,
    var bilirubinEnd: String
) : Parcelable
