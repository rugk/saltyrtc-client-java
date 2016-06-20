/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.tests.integration;

import org.junit.Test;
import org.saltyrtc.client.SaltyRTC;
import org.saltyrtc.client.keystore.KeyStore;
import org.saltyrtc.client.signaling.state.SignalingState;
import org.saltyrtc.client.tests.Config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.net.ssl.SSLContext;

import static junit.framework.TestCase.assertEquals;

public class ConnectionTest {

    @Test
    public void testWsConnect() throws Exception {
        // Create new SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);

        // Create SaltyRTC instances for initiator and responder
        final SaltyRTC initiator = new SaltyRTC(
                new KeyStore(), Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext);
        final SaltyRTC responder = new SaltyRTC(
                new KeyStore(), Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext,
                initiator.getPublicPermanentKey(), initiator.getAuthToken());

        // Enable debug mode
        initiator.setDebug(true);
        responder.setDebug(true);

        // Signaling state should still be NEW
        assertEquals(SignalingState.NEW, initiator.getSignalingState());
        assertEquals(SignalingState.NEW, responder.getSignalingState());

        // Create a new executor pool
        ExecutorService threadpool = Executors.newFixedThreadPool(4);

        // Connect initiator to server
        FutureTask<Void> connectFuture = initiator.connect();
        System.out.println("Executing future...");
        threadpool.execute(connectFuture);
        System.out.println("Executed...");

        // Wait until initiator is connected
        connectFuture.get();
        System.out.println("Connected!");
    }
}
