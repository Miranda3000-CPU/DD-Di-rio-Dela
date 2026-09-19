package com.example.backup

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {

    const val HEADER_PLAIN = "DDBACKUP_V1_PLAIN\n"
    const val HEADER_ENC = "DDBACKUP_V1_ENC\n"

    private const val ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val TAG_LENGTH_BITS = 128

    fun isEncrypted(bytes: ByteArray): Boolean {
        val headerBytes = HEADER_ENC.toByteArray(Charsets.UTF_8)
        if (bytes.size < headerBytes.size) return false
        for (i in headerBytes.indices) {
            if (bytes[i] != headerBytes[i]) return false
        }
        return true
    }

    fun isPlain(bytes: ByteArray): Boolean {
        val headerBytes = HEADER_PLAIN.toByteArray(Charsets.UTF_8)
        if (bytes.size < headerBytes.size) return false
        for (i in headerBytes.indices) {
            if (bytes[i] != headerBytes[i]) return false
        }
        return true
    }

    fun encrypt(jsonString: String, password: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTES).apply { SecureRandom().nextBytes(this) }

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val plaintextBytes = jsonString.toByteArray(Charsets.UTF_8)
        val ciphertext = cipher.doFinal(plaintextBytes)

        // Combine salt (16) + iv (12) + ciphertext
        val combined = ByteArray(salt.size + iv.size + ciphertext.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(ciphertext, 0, combined, salt.size + iv.size, ciphertext.size)

        val base64Payload = Base64.encodeToString(combined, Base64.NO_WRAP)
        val fullOutput = HEADER_ENC + base64Payload
        return fullOutput.toByteArray(Charsets.UTF_8)
    }

    fun decrypt(bytes: ByteArray, password: String): String {
        val fullString = String(bytes, Charsets.UTF_8)
        if (!fullString.startsWith(HEADER_ENC)) {
            throw IllegalArgumentException("Arquivo não possui cabeçalho de backup criptografado.")
        }

        val base64Payload = fullString.substring(HEADER_ENC.length).trim()
        val combined = Base64.decode(base64Payload, Base64.NO_WRAP)

        if (combined.size < SALT_LENGTH_BYTES + IV_LENGTH_BYTES) {
            throw IllegalArgumentException("Dados de backup criptografados corrompidos ou incompletos.")
        }

        val salt = ByteArray(SALT_LENGTH_BYTES)
        System.arraycopy(combined, 0, salt, 0, SALT_LENGTH_BYTES)

        val iv = ByteArray(IV_LENGTH_BYTES)
        System.arraycopy(combined, SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES)

        val ciphertextOffset = SALT_LENGTH_BYTES + IV_LENGTH_BYTES
        val ciphertextSize = combined.size - ciphertextOffset
        val ciphertext = ByteArray(ciphertextSize)
        System.arraycopy(combined, ciphertextOffset, ciphertext, 0, ciphertextSize)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun packPlain(jsonString: String): ByteArray {
        val fullOutput = HEADER_PLAIN + jsonString
        return fullOutput.toByteArray(Charsets.UTF_8)
    }

    fun unpackPlain(bytes: ByteArray): String {
        val fullString = String(bytes, Charsets.UTF_8)
        return if (fullString.startsWith(HEADER_PLAIN)) {
            fullString.substring(HEADER_PLAIN.length).trim()
        } else {
            // Also accept pure JSON for compatibility
            fullString.trim()
        }
    }
}
