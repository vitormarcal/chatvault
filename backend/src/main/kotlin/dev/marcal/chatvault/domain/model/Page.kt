package dev.marcal.chatvault.domain.model

data class Page<T>(
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
    val items: Int,
    val data: List<T>,
) {
    fun hasNext(): Boolean {
        if (totalPages == 0) return false
        if (totalPages == page) return false
        if (page < 1) return false
        return page < totalPages
    }
}
