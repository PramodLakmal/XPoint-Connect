/**
 * SessionEncryption.kt
 *
 * Purpose: Simple encryption utility for operator session credentials
 * Author: XPoint Connect Development Team
 * Date: December 2024
 *
 * Description: Provides basic encryption/decryption for storing operator
 * credentials securely in SQLite database. Uses Android's built-in
 * Base64 encoding with simple XOR cipher for basic security.
 *
 * Note: This is a basic implementation for development. In production,
 * consider using Android Keystore or more robust encryption methods.
 */
package com.xpoint.connect.utils

import android.util.Base64
import java.security.MessageDigest

object SessionEncryption {
    
    private const val SECRET_KEY = "XPointConnectOperatorSession2024"
    
    /**
     * Encrypts a string using simple XOR cipher with Base64 encoding
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        
        try {
            val key = generateKey()
            val encrypted = ByteArray(plainText.length)
            val plainBytes = plainText.toByteArray()
            
            for (i in plainBytes.indices) {
                encrypted[i] = (plainBytes[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            return plainText // Fallback to plain text if encryption fails
        }
    }
    
    /**
     * Decrypts a string using simple XOR cipher with Base64 decoding
     */
    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        
        try {
            val key = generateKey()
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val decrypted = ByteArray(encryptedBytes.size)
            
            for (i in encryptedBytes.indices) {
                decrypted[i] = (encryptedBytes[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            
            return String(decrypted)
        } catch (e: Exception) {
            return encryptedText // Fallback to encrypted text if decryption fails
        }
    }
    
    /**
     * Generates a key from the secret key using SHA-256
     */
    private fun generateKey(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(SECRET_KEY.toByteArray())
    }
    
    /**
     * Creates a hash for password verification without storing plain text
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }
    
    /**
     * Verifies a password against its hash
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}