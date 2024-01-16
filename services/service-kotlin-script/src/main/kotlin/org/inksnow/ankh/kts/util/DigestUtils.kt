package org.inksnow.ankh.kts.util

import java.security.MessageDigest
import java.util.*

object DigestUtils {
    @JvmStatic
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    @JvmStatic
    fun sha256(text: String): String {
        val input = (text as java.lang.String).bytes
        val output = MessageDigest.getInstance("SHA-256").digest(input)
        return encoder.encodeToString(output)
    }
}