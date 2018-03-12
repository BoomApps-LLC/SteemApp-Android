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
import android.support.annotation.NonNull
import android.security.KeyPairGeneratorSpec
import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64
import com.boomapps.steemapp.SteemApplication
import java.math.BigInteger
import java.util.*
import javax.security.auth.x500.X500Principal


/**
 * Created by Anatole Salanevich on 02.03.2018.
 */

private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val ALIAS = "SteemAppAlias"

class DeCryptor {

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
    fun decryptData(encryptedData: ByteArray, encryptionIv: ByteArray): String {

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

class EnCryptor {

    lateinit var encryption: ByteArray
    var iv: ByteArray? = null

    @Throws(UnrecoverableEntryException::class, NoSuchAlgorithmException::class, KeyStoreException::class,
            NoSuchProviderException::class, NoSuchPaddingException::class, InvalidKeyException::class,
            IOException::class, InvalidAlgorithmParameterException::class, SignatureException::class,
            BadPaddingException::class, IllegalBlockSizeException::class)
    fun encryptText(textToEncrypt: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(ALIAS))

        iv = cipher.iv

        //return encryption = cipher.doFinal(textToEncrypt.toByteArray(charset("UTF-8")))
        encryption = cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
        return encryption
    }

    @NonNull
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class)
    private fun getSecretKey(alias: String): SecretKey {
        val keyGenerator : KeyGenerator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            keyGenerator.init(KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build())
        } else {
            val start = GregorianCalendar()
            val end = GregorianCalendar()
            end.add(Calendar.YEAR, 30)
            keyGenerator = KeyGenerator.getInstance("RSA", ANDROID_KEY_STORE)
            val spec = KeyPairGeneratorSpec.Builder(SteemApplication.instance)
                    .setAlias(alias)
                    .setSubject(X500Principal("CN=$alias"))
                    .setSerialNumber(BigInteger.valueOf(Math.abs(alias.hashCode()).toLong()))
                    // Date range of validity for the generated pair.
                    .setStartDate(start.time).setEndDate(end.time)
                    .build()
            keyGenerator.init(spec)
        }
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

    fun setProperty(key: String, encryptedValue: String, iv: ByteArray, context: Context) {
        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)

        val settingPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        settingPref.putString(key, encryptedValue)
        settingPref.apply()

        val settingIvPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        settingIvPref.putString("${key}_iv", ivString)
        settingIvPref.apply()
    }

    fun clearAllProperties(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
    }
}