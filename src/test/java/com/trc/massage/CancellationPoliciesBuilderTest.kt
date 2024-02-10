package com.trc.massage

import com.trc.massage.binding.CancellationPolicy
import com.trc.massage.binding.Massage
import com.trc.massage.binding.Price
import java.time.LocalDate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

class CancellationPoliciesBuilderTest {

    @Test
    fun `when any cancellation policy price is less than zero should be not refundable from today`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 10.0
                currency = "USD"
            }
            cancellationPolicies = listOf(
                CancellationPolicy().apply {
                    date = massageDate.minusDays(3)
                    price = Price().apply {
                        amount = -0.1
                        currency = "USD"
                    }
                },
                CancellationPolicy().apply {
                    date = massageDate.minusDays(2)
                    price = Price().apply {
                        amount = 5.0
                        currency = "USD"
                    }
                }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(LocalDate.now()))
        assertThat(cancellationPolicies[0].amount, Is(10.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
    }

    @Test
    fun `when any cancellation policy is in different currency than massage currency should be not refundable from today`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 12.0
                currency = "USD"
            }
            cancellationPolicies = listOf(
                CancellationPolicy().apply {
                    date = massageDate.minusDays(5)
                    price = Price().apply {
                        amount = 7.0
                        currency = "USD"
                    }
                },
                CancellationPolicy().apply {
                    date = massageDate.minusDays(3)
                    price = Price().apply {
                        amount = 5.0
                        currency = "EUR"
                    }
                }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(LocalDate.now()))
        assertThat(cancellationPolicies[0].amount, Is(12.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
    }

    @Test
    fun `when any cancellation policy price is higher than massage price should be not refundable from today`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 15.0
                currency = "USD"
            }
            cancellationPolicies = listOf(CancellationPolicy().apply {
                date = massageDate.minusDays(3)
                price = Price().apply {
                    amount = 19.0
                    currency = "USD"
                }
            })
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(LocalDate.now()))
        assertThat(cancellationPolicies[0].amount, Is(15.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
    }

    @Test
    fun `when there are no cancellation policies should be not refundable from today`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 10.0
                currency = "USD"
            }
            cancellationPolicies = listOf()
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(LocalDate.now()))
        assertThat(cancellationPolicies[0].amount, Is(10.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
    }


    @Test
    fun `when cancellation policy is before today should be added but from today`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 10.0
                currency = "USD"
            }
            cancellationPolicies = listOf(
                    CancellationPolicy().apply {
                        date = LocalDate.now().minusDays(1)
                        price = Price().apply {
                            amount = 9.0
                            currency = "USD"
                        }
                    }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(2))
        assertThat(cancellationPolicies[0].date, Is(LocalDate.now()))
        assertThat(cancellationPolicies[0].amount, Is(9.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
        assertThat(cancellationPolicies[1].date, Is(massageDate))
        assertThat(cancellationPolicies[1].amount, Is(10.0))
        assertThat(cancellationPolicies[1].currency, Is("USD"))
    }

    @Test
    fun `when cancellation policy is the massage date should be added but with massage amount`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 20.0
                currency = "EUR"
            }
            cancellationPolicies = listOf(CancellationPolicy().apply {
                date = massageDate
                price = Price().apply {
                    amount = 10.0
                    currency = "EUR"
                }
            })
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(massageDate))
        assertThat(cancellationPolicies[0].amount, Is(20.0))
        assertThat(cancellationPolicies[0].currency, Is("EUR"))
    }

    @Test
    fun `when cancellation policy is after than the massage date should be not refundable from today`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 10.0
                currency = "USD"
            }
            cancellationPolicies = listOf(CancellationPolicy().apply {
                date = massageDate.plusDays(2)
                price = Price().apply {
                    amount = 7.0
                    currency = "USD"
                }
            })
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(LocalDate.now()))
        assertThat(cancellationPolicies[0].amount, Is(10.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
    }

    @Test
    fun `when one cancellation policies has price less than cancellation policy with date before should not be added`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 12.0
                currency = "USD"
            }
            cancellationPolicies = listOf(
                    CancellationPolicy().apply {
                        date = massageDate.minusDays(6)
                        price = Price().apply {
                            amount = 10.0
                            currency = "USD"
                        }
                    },
                    CancellationPolicy().apply {
                        date = massageDate.minusDays(3)
                        price = Price().apply {
                            amount = 6.0
                            currency = "USD"
                        }
                    }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(2))
        assertThat(cancellationPolicies[0].date, Is(massageDate.minusDays(6)))
        assertThat(cancellationPolicies[0].amount, Is(10.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
        assertThat(cancellationPolicies[1].date, Is(massageDate))
        assertThat(cancellationPolicies[1].amount, Is(12.0))
        assertThat(cancellationPolicies[1].currency, Is("USD"))
    }

    @Test
    fun `when two cancellation policies are in the same day should add the higher price`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 10.0
                currency = "USD"
            }
            cancellationPolicies = listOf(
                CancellationPolicy().apply {
                    date = massageDate.minusDays(3)
                    price = Price().apply {
                        amount = 5.0
                        currency = "USD"
                    }
                },
                CancellationPolicy().apply {
                    date = massageDate.minusDays(3)
                    price = Price().apply {
                        amount = 9.0
                        currency = "USD"
                    }
                }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(2))
        assertThat(cancellationPolicies[0].date, Is(massageDate.minusDays(3)))
        assertThat(cancellationPolicies[0].amount, Is(9.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
        assertThat(cancellationPolicies[1].date, Is(massageDate))
        assertThat(cancellationPolicies[1].amount, Is(10.0))
        assertThat(cancellationPolicies[1].currency, Is("USD"))
    }

    @Test
    fun `when cancellation policy amount is equals massage price and before the massage date should not be added cancellation policy the massage date`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 10.0
                currency = "USD"
            }
            cancellationPolicies = listOf(
                    CancellationPolicy().apply {
                        date = massageDate.minusDays(3)
                        price = Price().apply {
                            amount = 10.0
                            currency = "USD"
                        }
                    }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(1))
        assertThat(cancellationPolicies[0].date, Is(massageDate.minusDays(3)))
        assertThat(cancellationPolicies[0].amount, Is(10.0))
        assertThat(cancellationPolicies[0].currency, Is("USD"))
    }

    @Test
    fun `when cancellation policies are valid should be added with no refundable the massage date`() {
        val massageDate = LocalDate.now().plusDays(23)
        val massage = Massage().apply {
            price = Price().apply {
                amount = 45.0
                currency = "EUR"
            }
            cancellationPolicies = listOf(
                    CancellationPolicy().apply {
                        date = massageDate.minusDays(6)
                        price = Price().apply {
                            amount = 20.0
                            currency = "EUR"
                        }
                    },
                    CancellationPolicy().apply {
                        date = massageDate.minusDays(3)
                        price = Price().apply {
                            amount = 30.0
                            currency = "EUR"
                        }
                    }
            )
        }

        val cancellationPolicies = CancellationPoliciesBuilder.getCancellationPolicies(massageDate, massage)

        assertThat(cancellationPolicies, hasSize(3))
        assertThat(cancellationPolicies[0].date, Is(massageDate.minusDays(6)))
        assertThat(cancellationPolicies[0].amount, Is(20.0))
        assertThat(cancellationPolicies[0].currency, Is("EUR"))
        assertThat(cancellationPolicies[1].date, Is(massageDate.minusDays(3)))
        assertThat(cancellationPolicies[1].amount, Is(30.0))
        assertThat(cancellationPolicies[1].currency, Is("EUR"))
        assertThat(cancellationPolicies[2].date, Is(massageDate))
        assertThat(cancellationPolicies[2].amount, Is(45.0))
        assertThat(cancellationPolicies[2].currency, Is("EUR"))
    }

}

