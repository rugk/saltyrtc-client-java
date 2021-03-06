/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.events;

/**
 * The connection is closed.
 */
public class CloseEvent implements Event {

    private final int reason;

    public CloseEvent(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return this.reason;
    }
}
