package com.trc.massage.model

import java.time.Duration

data class MyMassage(
       val name: String, val status: String, val amount: Double, val currency: String, val externalReference: String, val duration: Duration, val cancellationPolicy: List<MyCancellationPolicy>
)