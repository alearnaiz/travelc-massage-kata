package com.trc.massage

import com.trc.massage.binding.CancellationPolicy
import com.trc.massage.binding.Massage
import com.trc.massage.model.MyCancellationPolicy
import java.time.LocalDate

object CancellationPoliciesBuilder {

    fun getCancellationPolicies(date: LocalDate, massage: Massage): List<MyCancellationPolicy> {
        val result = mutableListOf<MyCancellationPolicy>()

        try {
            for (cancellationPolicy in massage.cancellationPolicies.sortedBy { it.date }) {
                if (!isPriceValid(massage, cancellationPolicy)) {
                    throw IllegalArgumentException("Price not valid")
                } else if (isDateDuplicated(cancellationPolicy, result)) {
                    setHigherAmount(cancellationPolicy, result)
                    continue
                } else if (cancellationPolicy.date.isBefore(LocalDate.now())) {
                    result.add(MyCancellationPolicy(LocalDate.now(), cancellationPolicy.price.amount, cancellationPolicy.price.currency))
                    continue
                } else if (cancellationPolicy.date.isEqual(date)) {
                    result.add(MyCancellationPolicy(date, massage.price.amount, massage.price.currency))
                    continue
                } else if (cancellationPolicy.date.isAfter(date)) {
                    continue
                } else if (isPriceLessOrEquals(cancellationPolicy, result)) {
                    continue
                }
                result.add(MyCancellationPolicy(cancellationPolicy.date, cancellationPolicy.price.amount, cancellationPolicy.price.currency))
            }
            if (result.isEmpty()) {
                result.add(buildNoRefundable(massage, LocalDate.now()))
            }

            if (result.none { it.amount == massage.price.amount }) {
                result.add(buildNoRefundable(massage, date))
            }

            return result
        } catch (e: IllegalArgumentException) {
            return listOf(buildNoRefundable(massage, LocalDate.now()))
        }
    }

    private fun isPriceLessOrEquals(cancellationPolicy: CancellationPolicy, result: List<MyCancellationPolicy>): Boolean {
        return result.any { cancellationPolicy.price.amount <= it.amount  }
    }

    private fun setHigherAmount(cancellationPolicy: CancellationPolicy, result: MutableList<MyCancellationPolicy>) {
        val date = if (cancellationPolicy.date.isAfter(LocalDate.now())) cancellationPolicy.date else LocalDate.now()
        val myCancellationPolicy = result.first { c -> c.date.isEqual(date) }
        myCancellationPolicy.amount = if (myCancellationPolicy.amount > cancellationPolicy.price.amount) myCancellationPolicy.amount else cancellationPolicy.price.amount
    }

    private fun isDateDuplicated(cancellationPolicy: CancellationPolicy, cancellationPolicies: List<MyCancellationPolicy>): Boolean {
        val date = if (cancellationPolicy.date.isAfter(LocalDate.now())) cancellationPolicy.date else LocalDate.now()
        return cancellationPolicies.any { it.date.isEqual(date) }
    }

    private fun buildNoRefundable(massage: Massage, date: LocalDate) =
            MyCancellationPolicy(date, massage.price.amount, massage.price.currency)

    private fun isPriceValid(massage: Massage, cancellationPolicy: CancellationPolicy): Boolean {
        return cancellationPolicy.price.amount >= 0.0 && massage.price.currency == cancellationPolicy.price.currency && massage.price.amount >= cancellationPolicy.price.amount
    }
}