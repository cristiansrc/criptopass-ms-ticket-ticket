package com.criptopass.ms.ticket.ticket.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    @JsonProperty("total_elements")
    val totalElements: Long,
    @JsonProperty("total_pages")
    val totalPages: Int,
)
