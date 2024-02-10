package com.trc.massage

import com.trc.massage.binding.Massage
import com.trc.massage.model.MyMassage
import java.time.LocalDate

class MassageService(private var gateway: MassageGateway) {

    fun getMassages(date: LocalDate): List<MyMassage> {
        val response = gateway.getMassages(date)
        val massages = mutableListOf<MyMassage>()

        if (!response.error.isNullOrBlank()) {
            // LOGGER de error
            return massages
        }

        for (massage in response.massages) {
            if (!isValidMassage(massage)) {
                continue
            }
            massages.add(MyMassage(massage.name, massage.status, massage.price.amount, massage.price.currency, massage.code, massage.duration, CancellationPoliciesBuilder.getCancellationPolicies(date, massage)))
        }

        return massages
    }

    private fun isValidMassage(massage: Massage): Boolean {
        if (massage.price.amount <= 0.0) {
            return false
        }
        return massage.status == "AVAILABLE" || massage.status == "ON_REQUEST"
    }
}