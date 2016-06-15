/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.nonce;

import com.neilalexander.jnacl.NaCl;

abstract class Nonce {

    protected byte[] cookie;
    protected int overflow;
    protected long sequence;

    public static int COOKIE_LENGTH = 16;
    public static int TOTAL_LENGTH = NaCl.NONCEBYTES;

    /**
     * A cookie should be 16 bytes long.
     */
    protected void validateCookie(byte[] cookie) {
        if (cookie.length != COOKIE_LENGTH) {
            throw new IllegalArgumentException("cookie must be " + COOKIE_LENGTH + " bytes long");
        }
    }

    /**
     * An overflow number should be an uint16.
     */
    protected void validateOverflow(int overflow) {
        if (overflow < 0 || overflow >= (1 << 16)) {
            throw new IllegalArgumentException("overflow must be between 0 and 2**16-1");
        }
    }

    /**
     * A sequence should be an uint32.
     */
    protected void validateSequence(long sequence) {
        if (sequence < 0 || sequence >= (1L << 32)) {
            throw new IllegalArgumentException("sequence must be between 0 and 2**32-1");
        }
    }

    public byte[] getCookie() {
        return this.cookie;
    }

    /**
     * Return the overflow number.
     */
    public int getOverflow() {
        return this.overflow;
    }

    /**
     * Return the sequence number.
     */
    public long getSequence() {
        return this.sequence;
    }

    /**
     * Return the combined sequence number.
     */
    public long getCombinedSequence() {
        long combined = (long)this.overflow << 32 | this.sequence;
        assert combined >= 0 && combined < (1L << 48); // Sanity check
        return combined;
    }

}