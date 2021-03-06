/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.signaling.peers;

import org.saltyrtc.client.annotations.NonNull;
import org.saltyrtc.client.signaling.state.InitiatorHandshakeState;

/**
 * Information about the initiator. Used by responder during handshake.
 */
public class Initiator extends Peer {
    private static short ID = 0x01;

    private boolean connected;

    public InitiatorHandshakeState handshakeState;

    public Initiator(byte[] permanentKey) {
        super(Initiator.ID);
        this.permanentKey = permanentKey;
        this.connected = false;
        this.handshakeState = InitiatorHandshakeState.NEW;
    }

    @NonNull
    @Override
    public String getName() {
        return "Initiator";
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
