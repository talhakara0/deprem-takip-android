package com.talha.depremtakip.domain.time

import java.time.Instant

fun interface Clock {
    fun now(): Instant
}

class SystemClock : Clock {
    override fun now(): Instant = Instant.now()
}
