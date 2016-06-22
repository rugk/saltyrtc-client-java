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
import org.saltyrtc.client.events.ConnectedEvent;
import org.saltyrtc.client.events.ConnectionClosedEvent;
import org.saltyrtc.client.events.ConnectionErrorEvent;
import org.saltyrtc.client.events.EventHandler;
import org.saltyrtc.client.keystore.KeyStore;
import org.saltyrtc.client.signaling.state.SignalingState;
import org.saltyrtc.client.tests.Config;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConnectionTest {

    static {
        if (Config.VERBOSE) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        }
    }

    @Test
    public void testWsConnect() throws Exception {
        // Create new SSL context
        SSLContext sslContext;

        // If a file called "saltyrtc.jks" exists, we use it
        File kf = new File("saltyrtc.jks");
        if (!Config.IGNORE_JKS && kf.exists() && !kf.isDirectory()) {
            System.err.println("Using saltyrtc.jks as TLS keystore");

            // Initialize KeyStore
            final String password = "saltyrtc";
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(kf), password.toCharArray());

            // Initialize TrustManager
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // Initialize SSLContext
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } else {
            System.err.println("Using default SSLContext");
            sslContext = SSLContext.getDefault();
        }

        // Create SaltyRTC instances for initiator and responder
        final SaltyRTC initiator = new SaltyRTC(
                new KeyStore(), Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext);
        final SaltyRTC responder = new SaltyRTC(
                new KeyStore(), Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext,
                initiator.getPublicPermanentKey(), initiator.getAuthToken());

        // Enable debug mode
        if (Config.VERBOSE) {
            initiator.setDebug(true);
            responder.setDebug(true);
        }

        // Signaling state should still be NEW
        assertEquals(SignalingState.NEW, initiator.getSignalingState());
        assertEquals(SignalingState.NEW, responder.getSignalingState());

        // Create a new executor pool
        ExecutorService threadpool = Executors.newFixedThreadPool(4);

        // Register event handlers
        final Map<String, Boolean> eventsCalled = new HashMap<>();
        eventsCalled.put("initiatorConnected", false); eventsCalled.put("initiatorError", false);
        eventsCalled.put("responderConnected", false); eventsCalled.put("responderError", false);
        initiator.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                eventsCalled.put("initiatorConnected", true); return false;
            }
        });
        initiator.events.connectionError.register(new EventHandler<ConnectionErrorEvent>() {
            @Override
            public boolean handle(ConnectionErrorEvent event) {
                eventsCalled.put("initiatorError", true); return false;
            }
        });
        responder.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                eventsCalled.put("responderConnected", true); return false;
            }
        });
        responder.events.connectionError.register(new EventHandler<ConnectionErrorEvent>() {
            @Override
            public boolean handle(ConnectionErrorEvent event) {
                eventsCalled.put("responderError", true); return false;
            }
        });

        // Connect server
        FutureTask<Void> initiatorConnect = initiator.connect();
        FutureTask<Void> responderConnect = responder.connect();
        System.out.println("Executing future...");
        threadpool.execute(initiatorConnect);
        threadpool.execute(responderConnect);

        // Wait until both are connected
        initiatorConnect.get();
        System.out.println("Initiator connected");
        assertEquals(SignalingState.SERVER_HANDSHAKE, initiator.getSignalingState());
        responderConnect.get();
        System.out.println("Responder connected");
        assertEquals(SignalingState.SERVER_HANDSHAKE, responder.getSignalingState());

        // Test if event handlers were called.
        // As this is only the first stage of the connection process,
        // no ConnectionEvents should be sent out yet.
        assertFalse(eventsCalled.get("initiatorConnected"));
        assertFalse(eventsCalled.get("initiatorError"));
        assertFalse(eventsCalled.get("responderConnected"));
        assertFalse(eventsCalled.get("responderError"));
    }
}
