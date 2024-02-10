package com.trc.massage.model

import java.time.LocalDate

data class MyCancellationPolicy(val date: LocalDate, var amount: Double, val currency: String)