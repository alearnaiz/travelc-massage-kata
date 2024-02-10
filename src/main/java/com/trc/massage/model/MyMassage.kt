package com.trc.massage.model

import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MyMassage(
       val name: String, val status: String, val amount: Double, val currency: String, val externalReference: String, val duration: Duration, val cancellationPolicies: List<MyCancellationPolicy>
) {
    fun getCancellationPoliciesFormatted(): List<String> {
        val cancellationPolicies = cancellationPolicies.sortedBy { it.date }
        val result = mutableListOf<String>()
        val date = cancellationPolicies[0].date
        if (date != LocalDate.now()) {
            result.add("Sin gastos hasta el ${getCancellationPolicyFormatted(date.minusDays(1))}.")
        }
        cancellationPolicies.forEachIndexed { index, cancellationPolicy ->
            run {
                if (index != 0) {
                    result.add("Entre el ${getCancellationPolicyFormatted(cancellationPolicies[index-1].date)} y el ${getCancellationPolicyFormatted(cancellationPolicies[index].date.minusDays(1))}: ${cancellationPolicies[index-1].amount} ${cancellationPolicies[index-1].currency}.")
                }
                if (index == cancellationPolicies.size - 1) {
                    result.add("Desde el ${getCancellationPolicyFormatted(cancellationPolicy.date)}: no reembolsable.")
                }
            }
        }
        return result
    }

    companion object {
        fun getCancellationPolicyFormatted(date: LocalDate): String {
            return date.format(DateTimeFormatter.ofPattern("dd MM yyyy"))
        }
    }
}