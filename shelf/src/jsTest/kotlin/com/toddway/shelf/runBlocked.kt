package com.toddway.shelf

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

actual fun runBlocked(block: suspend () -> Unit): dynamic = GlobalScope.async { block() }
