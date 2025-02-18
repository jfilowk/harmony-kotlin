package com.harmony.kotlin.data.validator.vastra.strategies.timestamp

import com.harmony.kotlin.common.date.Millis
import com.harmony.kotlin.common.date.Seconds
import com.harmony.kotlin.data.validator.vastra.strategies.ValidationStrategy
import com.harmony.kotlin.data.validator.vastra.strategies.ValidationStrategyDataSource
import com.harmony.kotlin.data.validator.vastra.strategies.ValidationStrategyResult
import kotlinx.datetime.Clock

interface TimestampValidationEntity : ValidationStrategyDataSource {
  val lastUpdatedAt: Millis
  val expireIn: Seconds
}

class TimestampValidationStrategy : ValidationStrategy {
  companion object {
    const val MILLIS_IN_SECOND = 1_000
  }

  override fun <T : ValidationStrategyDataSource> isValid(t: T): ValidationStrategyResult {
    when (t) {
      is TimestampValidationEntity -> {
        val now = Clock.System.now().toEpochMilliseconds()

        val lastUpdate = t.lastUpdatedAt

        val diff = now - lastUpdate
        val difSeconds = diff / MILLIS_IN_SECOND

        val isValid = difSeconds < t.expireIn

        return if (isValid)
          ValidationStrategyResult.VALID
        else
          ValidationStrategyResult.INVALID
      }
      else -> {
        throw IllegalArgumentException("object != TimestampValidationStrategyDataSource")
      }
    }
  }
}
