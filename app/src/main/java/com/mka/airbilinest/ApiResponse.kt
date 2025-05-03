data class ApiResponse(
    val status: String,
    val message: String? = null,
    val user_id: Int? = null
) {
    val token: String? = null
    val app_access: Int? = null // Tambahkan field in
}