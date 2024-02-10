package com.trc.massage

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MassageIT {

    private val gateway = MassageGateway()

    private val service = MassageService(gateway)

    @Test
    fun `workflow date nos valid`() {
        val massages = service.getMassages(LocalDate.now().plusDays(1))

        assertTrue(massages.isEmpty())
    }

    @Test
    fun `workflow valid`() {
        val massages = service.getMassages(LocalDate.now().plusDays(45))

        assertTrue(massages.isNotEmpty())
    }
}