/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.nonce;

/**
 * A SaltyRTC CSN pair.
 */
public class CombinedSequencePair {
    private CombinedSequence ours;
    private Long theirs = null;

    public CombinedSequencePair() {
        this.ours = new CombinedSequence();
    }

    public CombinedSequencePair(Long theirs) {
        this();
        this.setTheirs(theirs);
    }

    public CombinedSequencePair(CombinedSequence ours, Long theirs) {
        this.ours = ours;
        this.theirs = theirs;
    }

    public CombinedSequence getOurs() {
        return ours;
    }

    public boolean hasTheirs() {
        return this.theirs != null;
    }

    public Long getTheirs() {
        return theirs;
    }

    public void setTheirs(Long theirs) {
        this.theirs = theirs;
    }
}