package com.mka.airbilinest

data class Patient(
    val id: Int, // ID pasien (auto-increment di database)
    val userId: Int, // ID pengguna yang memiliki data pasien ini
    val name: String, // Nama pasien
    val weight: Int, // Berat badan pasien
    val birthDate: String, // Tanggal lahir pasien
    val bilirubinStart: Float, // Kadar bilirubin awal
    val lengthOfPhototherapy: Float, // Durasi fototerapi
    val phototherapyIntensity: Float, // Intensitas fototerapi
    val bilirubinEnd: Float // Kadar bilirubin akhir
)