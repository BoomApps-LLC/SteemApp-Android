/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.utils

import android.os.Build
import java.io.IOException
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import androidx.annotation.NonNull
import android.security.KeyPairGeneratorSpec
import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import android.util.Base64
import com.boomapps.steemapp.SteemApplication
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import kotlin.collections.ArrayList


/**
 * Created by Anatole Salanevich on 02.03.2018.
 */

private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val ALIAS = "SteemAppAlias"


class Crypto {

    private var oldCrypto: OldCrypto? = null
    private var deCryptor: DeCryptor? = null
    private var enCryptor: EnCryptor? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            enCryptor = EnCryptor()
        } else {
            oldCrypto = OldCrypto(SteemApplication.instance)
        }
    }

    fun setIvForDecryptor(iv: ByteArray?) {
        if (iv != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                deCryptor = DeCryptor(iv)
            }
        }
    }

    fun getIvFromEncryptor(): ByteArray? {
        return if (enCryptor == null) {
            null
        } else enCryptor!!.iv!!
    }

    fun encrypt(text: String): ByteArray {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            enCryptor!!.encrypt(text)
        } else {
            oldCrypto!!.encrypt(text)
        }
    }

    fun decrypt(encryptedText: ByteArray): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deCryptor!!.decrypt(encryptedText)
        } else {
            oldCrypto!!.decrypt(encryptedText)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private class DeCryptor(private val encryptionIv: ByteArray) {

    private lateinit var keyStore: KeyStore

    init {
        initKeyStore()
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    private fun initKeyStore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
    }

    @Throws(UnrecoverableEntryException::class, NoSuchAlgorithmException::class, KeyStoreException::class,
            NoSuchProviderException::class, NoSuchPaddingException::class, InvalidKeyException::class, IOException::class,
            BadPaddingException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class)
    fun decrypt(encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(ALIAS), spec)

        return String(cipher.doFinal(encryptedData), Charset.defaultCharset())
    }

    @Throws(NoSuchAlgorithmException::class, UnrecoverableEntryException::class, KeyStoreException::class)
    private fun getSecretKey(alias: String): SecretKey {
        return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
    }

}

@RequiresApi(Build.VERSION_CODES.M)
class EnCryptor {

    lateinit var encryption: ByteArray
    var iv: ByteArray? = null

    @Throws(UnrecoverableEntryException::class, NoSuchAlgorithmException::class, KeyStoreException::class,
            NoSuchProviderException::class, NoSuchPaddingException::class, InvalidKeyException::class,
            IOException::class, InvalidAlgorithmParameterException::class, SignatureException::class,
            BadPaddingException::class, IllegalBlockSizeException::class)
    fun encrypt(textToEncrypt: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(ALIAS))

        iv = cipher.iv

        encryption = cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
        return encryption
    }

    @NonNull
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class)
    private fun getSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
        return keyGenerator.generateKey()
    }

}

class EncryptedInfo {
    var data: String? = null
    var iv: ByteArray? = null
}

object SettingsRepository {
    fun getProperty(key: String, context: Context): EncryptedInfo {
        val info = EncryptedInfo()

        info.data = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, null)

        val iv = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("${key}_iv", null) ?: return info

        info.iv = Base64.decode(iv, Base64.DEFAULT)

        return info
    }

    fun setProperty(key: String, encryptedValue: String, iv: ByteArray?, context: Context) {
        val settingPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        settingPref.putString(key, encryptedValue)
        settingPref.apply()

        if (iv != null) {
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            val settingIvPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
            settingIvPref.putString("${key}_iv", ivString)
            settingIvPref.apply()
        }
    }

    fun clearAllProperties(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
    }
}

@Deprecated("For Android less API 23")
private class OldCrypto(private var context: Context) {

    private val ENCRYPTED_KEY = "encrypted_key"
    private val RSA_MODE = "RSA/ECB/PKCS1Padding"
    private val AES_MODE = "AES/ECB/PKCS7Padding"
    private var keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)

    init {
        keyStore.load(null)
        if (PreferenceManager.getDefaultSharedPreferences(SteemApplication.instance)
                        .getString(ENCRYPTED_KEY, null) == null) {
            generateKeyPairs(ALIAS)
            generateAndStoreAesKey(ALIAS)
        }
    }

    fun encrypt(textToEncrypt: String): ByteArray {
        val c = Cipher.getInstance(AES_MODE, "BC")
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(context))
        return c.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
    }

    fun decrypt(encrypted: ByteArray): String {
        val c = Cipher.getInstance(AES_MODE, "BC")
        c.init(Cipher.DECRYPT_MODE, getSecretKey(context))
        val decodedBytes = c.doFinal(encrypted)
        return decodedBytes.toString(Charsets.UTF_8)
    }

    private fun getSecretKey(context: Context): Key {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        var encryptedKeyB64: String? = pref.getString(ENCRYPTED_KEY, null)
        if (encryptedKeyB64 == null) {
            encryptedKeyB64 = generateAndStoreAesKey(ALIAS)
        }
        val encryptedKey = Base64.decode(encryptedKeyB64, Base64.DEFAULT)
        val key = rsaDecrypt(encryptedKey, ALIAS)
        return SecretKeySpec(key, "AES")
    }

    private fun rsaDecrypt(encrypted: ByteArray, alias: String): ByteArray {
        val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        val outputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
        outputCipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
        val values = ArrayList<Byte>()
        var cipherInputStream: CipherInputStream? = null
        try {
            cipherInputStream = CipherInputStream(ByteArrayInputStream(encrypted), outputCipher)
            var nextByte = cipherInputStream.read()
            while (nextByte != -1) {
                values.add(nextByte.toByte())
                nextByte = cipherInputStream.read()
            }
        } catch (e: Exception) {
            throw e
        } finally {
            cipherInputStream?.close()
        }
        val bytes = ByteArray(values.size)
        for (i in bytes.indices) {
            bytes[i] = values[i]
        }
        return bytes
    }

    private fun generateAndStoreAesKey(alias: String): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(SteemApplication.instance)
        var encryptedKey64 = pref.getString(ENCRYPTED_KEY, null)
        if (encryptedKey64 == null) {
            val key = ByteArray(16)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(key)
            val encryptedKey = rsaEncrypt(key, alias)
            encryptedKey64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
            val edit = pref.edit()
            edit.putString(ENCRYPTED_KEY, encryptedKey64)
            edit.apply()
        }
        return encryptedKey64
    }

    private fun rsaEncrypt(secret: ByteArray, alias: String): ByteArray {
        val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        val inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)
        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
        cipherOutputStream.write(secret)
        cipherOutputStream.close()
        return outputStream.toByteArray()
    }

    private fun generateKeyPairs(alias: String): KeyPair {
        val start = GregorianCalendar()
        val end = GregorianCalendar()
        end.add(Calendar.YEAR, 30)
        val keyGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE)
        val spec = KeyPairGeneratorSpec.Builder(SteemApplication.instance)
                .setAlias(alias)
                .setSubject(X500Principal("CN=$alias"))
                .setSerialNumber(BigInteger.valueOf(Math.abs(alias.hashCode()).toLong()))
                // Date range of validity for the generated pair.
                .setStartDate(start.time).setEndDate(end.time)
                .build()
        keyGenerator.initialize(spec)
        return keyGenerator.generateKeyPair()
    }

}