/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.keystore;

import org.saltyrtc.client.exceptions.CryptoFailedException;
import org.saltyrtc.client.exceptions.InvalidKeyException;
import org.saltyrtc.client.helpers.HexHelper;
import org.saltyrtc.vendor.com.neilalexander.jnacl.NaCl;
import org.slf4j.Logger;

/**
 * Handle encrypting and decrypting messages for the peers.
 *
 * This class uses NaCl asymmetric key encryption.
 */
public class KeyStore {

    // Logger
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger("SaltyRTC.KeyStore");

    // Keys
    private byte[] secretKey = new byte[NaCl.SECRETKEYBYTES];
    private byte[] publicKey = new byte[NaCl.PUBLICKEYBYTES];

    /**
     * Create a new key store.
     */
    public KeyStore() {
        LOG.debug("Generating new key pair");
        NaCl.genkeypair(this.publicKey, this.secretKey);
        LOG.debug("Secret key: " + NaCl.asHex(this.secretKey));
        LOG.debug("Public key: " + NaCl.asHex(this.publicKey));
    }

    /**
     * Create a new key store from an existing secret key.
     * The public key will automatically be derived.
     */
    public KeyStore(byte[] secretKey) {
        LOG.debug("Deriving public key from secret key");
        this.secretKey = secretKey;
        this.publicKey = NaCl.derivePublicKey(secretKey);
        LOG.debug("Secret key: " + NaCl.asHex(this.secretKey));
        LOG.debug("Public key: " + NaCl.asHex(this.publicKey));
    }

    /**
     * Create a new key store from an existing secret key.
     * The public key will automatically be derived.
     */
    public KeyStore(String secretKeyHex) {
        this(HexHelper.hexStringToByteArray(secretKeyHex));
    }

    /**
     * Create a new key store from an existing keypair.
     */
    public KeyStore(byte[] publicKey, byte[] secretKey) {
        LOG.debug("Using existing keypair");
        this.secretKey = secretKey;
        this.publicKey = publicKey;
        LOG.debug("Secret key: " + NaCl.asHex(this.secretKey));
        LOG.debug("Public key: " + NaCl.asHex(this.publicKey));
    }

    /**
     * Create a new key store from an existing keypair.
     */
    public KeyStore(String publicKeyHex, String secretKeyHex) {
        this(HexHelper.hexStringToByteArray(publicKeyHex),
             HexHelper.hexStringToByteArray(secretKeyHex));
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    /**
     * Encrypt data for the peer. Return Box.
     *
     * @param data Bytes to be encrypted.
     * @param nonce The nonce that should be used to encrypt.
     * @param otherKey The public key of the peer.
     * @return The encrypted NaCl box.
     * @throws InvalidKeyException One of the keys was invalid.
     * @throws CryptoFailedException Encryption failed.
     */
    public Box encrypt(byte[] data, byte[] nonce, byte[] otherKey) throws CryptoFailedException, InvalidKeyException {
        // Create NaCl instance
        final NaCl nacl;
        try {
            nacl = new NaCl(this.secretKey, otherKey);
        } catch (Error e) {
            throw new InvalidKeyException(e.toString());
        }

        // Encrypt
        final byte[] encrypted;
        try {
            encrypted = nacl.encrypt(data, nonce);
        } catch (Error e) {
            throw new CryptoFailedException(e.toString());
        }
        if (encrypted == null) {
            throw new CryptoFailedException("Encrypted data is null");
        }

        // Return box
        return new Box(nonce, encrypted);
    }

    /**
     * Decrypt data from the peer. Return contained bytes.
     *
     * @param box NaCl box.
     * @param otherKey The public key of the peer.
     * @return The decrypted data.
     * @throws CryptoFailedException Decryption failed.
     */
    public byte[] decrypt(Box box, byte[] otherKey) throws CryptoFailedException, InvalidKeyException {
        // Create NaCl instance
        final NaCl nacl;
        try {
            nacl = new NaCl(this.secretKey, otherKey);
        } catch (Error e) {
            throw new InvalidKeyException(e.toString());
        }

        final byte[] decrypted;
        try {
            decrypted = nacl.decrypt(box.getData(), box.getNonce());
        } catch (Error e) {
            throw new CryptoFailedException(e.toString());
        }
        if (decrypted == null) {
            throw new CryptoFailedException("Decrypted data is null");
        }

        return decrypted;
    }
}
