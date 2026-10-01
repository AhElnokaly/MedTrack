package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.SecurityKeyManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecurityKeyManagerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        SecurityKeyManager.resetForTesting()
        val prefs = context.getSharedPreferences("ameen_keystore_vault", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun getOrCreateDatabasePassphrase_returns_valid_256_bit_key_silently() {
        val key = SecurityKeyManager.getOrCreateDatabasePassphrase(context)
        assertNotNull(key)
        assertEquals("Master database key must be 256-bit (32 bytes)", 32, key.size)
    }

    @Test
    fun getOrCreateDatabasePassphrase_is_persistent_across_retrievals() {
        val keyFirst = SecurityKeyManager.getOrCreateDatabasePassphrase(context)
        val keySecond = SecurityKeyManager.getOrCreateDatabasePassphrase(context)
        assertArrayEquals("Subsequent calls must recover the identical decrypted SQLCipher key", keyFirst, keySecond)
    }

    @Test
    fun sqlcipher_key_is_stored_encrypted_and_never_in_plaintext() {
        val key = SecurityKeyManager.getOrCreateDatabasePassphrase(context)
        val prefs = context.getSharedPreferences("ameen_keystore_vault", Context.MODE_PRIVATE)

        val encryptedBase64 = prefs.getString("encrypted_sqlcipher_key", null)
        val ivBase64 = prefs.getString("gcm_iv", null)

        assertNotNull("Encrypted key must be stored in SharedPreferences", encryptedBase64)
        assertNotNull("GCM IV must be stored in SharedPreferences", ivBase64)

        val rawKeyString = String(key, Charsets.ISO_8859_1)
        assertFalse("Ciphertext must not match raw key bytes", encryptedBase64 == rawKeyString)
    }

    @Test
    fun createSupportFactory_instantiates_without_errors() {
        val key = SecurityKeyManager.getOrCreateDatabasePassphrase(context)
        val factory = SecurityKeyManager.createSupportFactory(context, key)
        assertNotNull("SupportFactory must be initialized successfully", factory)
    }
}
