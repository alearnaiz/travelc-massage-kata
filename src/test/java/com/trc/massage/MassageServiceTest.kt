package com.trc.massage

import com.trc.massage.binding.CancellationPolicy
import com.trc.massage.binding.Massage
import com.trc.massage.binding.Price
import com.trc.massage.binding.Response
import com.trc.massage.model.MyMassage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.time.Duration
import java.time.LocalDate
import org.hamcrest.CoreMatchers.`is` as Is

class MassageServiceTest {

    private val gateway = mock(MassageGateway::class.java)

    private val massageService = MassageService(gateway)

    @Test
    fun `when response has error should return empty`() {
        Mockito.`when`(gateway.getMassages(LocalDate.now())).thenReturn(Response().apply {
            error = "La antelación mínima para reservar es de 15 días"
            massages = listOf(
                Massage().apply {
                    status = "AVAILABLE"
                    code = "1"
                    name = "Masaje sueco"
                    price = Price().apply {
                        amount = 25.0
                        currency = "EUR"
                    }
                    cancellationPolicies = listOf(CancellationPolicy().apply {
                        date = LocalDate.now().minusDays(3)
                        price = Price().apply {
                            amount = 15.0
                            currency = "EUR"
                        }
                    })
                })
        })

        val massages = massageService.getMassages(LocalDate.now())

        assertThat(massages, hasSize(0))
    }

    @Test
    fun `when massage price is zero os less should not be added`() {
        Mockito.`when`(gateway.getMassages(LocalDate.now())).thenReturn(Response().apply {
            massages = listOf(Massage().apply {
                price = Price().apply {
                    amount = 0.0
                    currency = "EUR"
                }
                status = "AVAILABLE"
            })
        })

        val massages = massageService.getMassages(LocalDate.now())

        assertThat(massages, hasSize(0))
    }

    @Test
    fun `when massage status is not valid should not be added`() {
        Mockito.`when`(gateway.getMassages(LocalDate.now())).thenReturn(Response().apply {
            massages = listOf(Massage().apply {
                price = Price().apply {
                    amount = 2.2
                    currency = "EUR"
                }
                status = "UNAVAILABLE"
            })
        })

        val massages = massageService.getMassages(LocalDate.now())

        assertThat(massages, hasSize(0))
    }

    @Test
    fun `when massage has status available or on request should be added`() {
        val massageDate = LocalDate.now().plusDays(30)
        Mockito.`when`(gateway.getMassages(massageDate)).thenReturn(Response().apply {
            massages = listOf(
                Massage().apply {
                    status = "AVAILABLE"
                    code = "1"
                    name = "Masaje sueco"
                    price = Price().apply {
                        amount = 25.0
                        currency = "EUR"
                    }
                    cancellationPolicies = listOf(
                        CancellationPolicy().apply {
                            date = massageDate.minusDays(10)
                            price = Price().apply {
                                amount = 15.0
                                currency = "EUR"
                            }
                        },
                        CancellationPolicy().apply {
                            date = massageDate.minusDays(5)
                            price = Price().apply {
                                amount = 20.0
                                currency = "EUR"
                            }
                        }
                    )
                    duration = Duration.ofMinutes(20)
                },
                Massage().apply {
                    status = "ON_REQUEST"
                    code = "2"
                    name = "Masaje japones"
                    price = Price().apply {
                        amount = 23.0
                        currency = "EUR"
                    }
                    cancellationPolicies = listOf(CancellationPolicy().apply {
                        date = massageDate.minusDays(3)
                        price = Price().apply {
                            amount = 12.0
                            currency = "EUR"
                        }
                    })
                    duration = Duration.ofMinutes(30)
                }
            )
        })

        val massages = massageService.getMassages(massageDate)

        assertThat(massages, hasSize(2))
        assertThat(massages[0].status, Is("AVAILABLE"))
        assertThat(massages[0].name, Is("Masaje sueco"))
        assertThat(massages[0].amount, Is(25.0))
        assertThat(massages[0].currency, Is("EUR"))
        assertThat(massages[0].externalReference, Is("1"))
        assertThat(massages[0].cancellationPolicies, hasSize(3))
        assertThat(massages[0].getCancellationPoliciesFormatted(), hasSize(4))
        assertThat(massages[0].getCancellationPoliciesFormatted()[0], Is("Sin gastos hasta el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(11))}."))
        assertThat(massages[0].getCancellationPoliciesFormatted()[1], Is("Entre el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(10))} y el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(6))}: 15.0 EUR."))
        assertThat(massages[0].getCancellationPoliciesFormatted()[2], Is("Entre el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(5))} y el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(1))}: 20.0 EUR."))
        assertThat(massages[0].getCancellationPoliciesFormatted()[3], Is("Desde el ${MyMassage.getCancellationPolicyFormatted(massageDate)}: no reembolsable."))
        assertThat(massages[0].duration, Is(Duration.ofMinutes(20)))
        assertThat(massages[1].status, Is("ON_REQUEST"))
        assertThat(massages[1].name, Is("Masaje japones"))
        assertThat(massages[1].amount, Is(23.0))
        assertThat(massages[1].currency, Is("EUR"))
        assertThat(massages[1].externalReference, Is("2"))
        assertThat(massages[1].cancellationPolicies, hasSize(2))
        assertThat(massages[1].getCancellationPoliciesFormatted(), hasSize(3))
        assertThat(massages[1].getCancellationPoliciesFormatted()[0], Is("Sin gastos hasta el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(4))}."))
        assertThat(massages[1].getCancellationPoliciesFormatted()[1], Is("Entre el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(3))} y el ${MyMassage.getCancellationPolicyFormatted(massageDate.minusDays(1))}: 12.0 EUR."))
        assertThat(massages[1].getCancellationPoliciesFormatted()[2], Is("Desde el ${MyMassage.getCancellationPolicyFormatted(massageDate)}: no reembolsable."))
        assertThat(massages[1].duration, Is(Duration.ofMinutes(30)))
    }
}