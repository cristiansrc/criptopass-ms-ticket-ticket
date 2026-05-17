package com.criptopass.ms.ticket.ticket.config

import com.criptopass.ms.ticket.ticket.domain.service.TicketDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {

    @Bean
    fun ticketDomainService(): TicketDomainService = TicketDomainService()
}
