/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.tests.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.saltyrtc.client.SaltyRTC;
import org.saltyrtc.client.events.ConnectedEvent;
import org.saltyrtc.client.events.ConnectionClosedEvent;
import org.saltyrtc.client.events.ConnectionErrorEvent;
import org.saltyrtc.client.events.EventHandler;
import org.saltyrtc.client.keystore.KeyStore;
import org.saltyrtc.client.signaling.state.SignalingState;
import org.saltyrtc.client.tests.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConnectionTest {

    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        if (Config.DEBUG) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        }
    }

    private SaltyRTC initiator;
    private SaltyRTC responder;
    private Map<String, Boolean> eventsCalled;

    @Before
    public void setUp() throws Exception {
        // Get SSL context
        final SSLContext sslContext = SSLContextHelper.getSSLContext();

        // Create SaltyRTC instances for initiator and responder
        initiator = new SaltyRTC(
            new KeyStore(), Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext);
        responder = new SaltyRTC(
            new KeyStore(), Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext,
            initiator.getPublicPermanentKey(), initiator.getAuthToken());

        // Enable verbose debug mode
        if (Config.VERBOSE) {
            initiator.setDebug(true);
            responder.setDebug(true);
        }

        // Initiate event registry
        eventsCalled = new HashMap<>();
        final String[] events = new String[] { "Connected", "Error", "Closed" };
        for (String event : events) {
            eventsCalled.put("initiator" + event, false);
            eventsCalled.put("responder" + event, false);
        }

        // Register event handlers
        initiator.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                eventsCalled.put("initiatorConnected", true);
                return false;
            }
        });
        initiator.events.connectionError.register(new EventHandler<ConnectionErrorEvent>() {
            @Override
            public boolean handle(ConnectionErrorEvent event) {
                eventsCalled.put("initiatorError", true);
                return false;
            }
        });
        initiator.events.connectionClosed.register(new EventHandler<ConnectionClosedEvent>() {
            @Override
            public boolean handle(ConnectionClosedEvent event) {
                eventsCalled.put("initiatorClosed", true);
                return false;
            }
        });
        responder.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                eventsCalled.put("responderConnected", true);
                return false;
            }
        });
        responder.events.connectionError.register(new EventHandler<ConnectionErrorEvent>() {
            @Override
            public boolean handle(ConnectionErrorEvent event) {
                eventsCalled.put("responderError", true);
                return false;
            }
        });
        responder.events.connectionClosed.register(new EventHandler<ConnectionClosedEvent>() {
            @Override
            public boolean handle(ConnectionClosedEvent event) {
                eventsCalled.put("responderClosed", true);
                return false;
            }
        });
    }

    @Test
    public void testHandshakeInitiatorFirst() throws Exception {
        // Signaling state should still be NEW
        assertEquals(SignalingState.NEW, initiator.getSignalingState());
        assertEquals(SignalingState.NEW, responder.getSignalingState());

        // Latches to test connection state
        final CountDownLatch connectedPeers = new CountDownLatch(2);

        // Register onConnect handler
        initiator.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                connectedPeers.countDown();
                return true;
            }
        });
        responder.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                connectedPeers.countDown();
                return true;
            }
        });

        // Connect server
        initiator.connect();
        Thread.sleep(1000);
        responder.connect();

        // Wait for full handshake
        final boolean bothConnected = connectedPeers.await(4, TimeUnit.SECONDS);
        assertTrue(bothConnected);
        assertFalse(eventsCalled.get("initiatorError"));
        assertFalse(eventsCalled.get("responderError"));

        // Signaling state should be OPEN
        assertEquals(SignalingState.OPEN, initiator.getSignalingState());
        assertEquals(SignalingState.OPEN, responder.getSignalingState());

        // Disconnect
        initiator.disconnect();
        responder.disconnect();

        // Await close events
        Thread.sleep(300);
        assertTrue(eventsCalled.get("initiatorClosed"));
        assertTrue(eventsCalled.get("responderClosed"));
        assertFalse(eventsCalled.get("initiatorError"));
        assertFalse(eventsCalled.get("responderError"));

        // Signaling state should be CLOSED
        assertEquals(SignalingState.CLOSED, initiator.getSignalingState());
        assertEquals(SignalingState.CLOSED, responder.getSignalingState());
    }

    @Test
    public void testHandshakeResponderFirst() throws Exception {
        // Signaling state should still be NEW
        assertEquals(SignalingState.NEW, initiator.getSignalingState());
        assertEquals(SignalingState.NEW, responder.getSignalingState());

        // Latches to test connection state
        final CountDownLatch connectedPeers = new CountDownLatch(2);

        // Register onConnect handler
        responder.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                connectedPeers.countDown();
                return true;
            }
        });
        initiator.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                connectedPeers.countDown();
                return true;
            }
        });

        // Connect server
        responder.connect();
        Thread.sleep(1000);
        initiator.connect();

        // Wait for full handshake
        final boolean bothConnected = connectedPeers.await(4, TimeUnit.SECONDS);
        assertTrue(bothConnected);
        assertFalse(eventsCalled.get("responderError"));
        assertFalse(eventsCalled.get("initiatorError"));

        // Signaling state should be OPEN
        assertEquals(SignalingState.OPEN, responder.getSignalingState());
        assertEquals(SignalingState.OPEN, initiator.getSignalingState());

        // Disconnect
        responder.disconnect();
        initiator.disconnect();

        // Await close events
        Thread.sleep(300);
        assertTrue(eventsCalled.get("responderClosed"));
        assertTrue(eventsCalled.get("initiatorClosed"));
        assertFalse(eventsCalled.get("responderError"));
        assertFalse(eventsCalled.get("initiatorError"));

        // Signaling state should be CLOSED
        assertEquals(SignalingState.CLOSED, responder.getSignalingState());
        assertEquals(SignalingState.CLOSED, initiator.getSignalingState());
    }

    @Test
    public void testConnectionSpeed() throws Exception {
        // Max 1s for handshake
        final int MAX_DURATION = 1000;

        // Latches to test connection state
        final CountDownLatch connectedPeers = new CountDownLatch(2);
        responder.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                connectedPeers.countDown();
                return true;
            }
        });
        initiator.events.connected.register(new EventHandler<ConnectedEvent>() {
            @Override
            public boolean handle(ConnectedEvent event) {
                connectedPeers.countDown();
                return true;
            }
        });

        // Connect server
        final long startTime = System.nanoTime();
        initiator.connect();
        responder.connect();

        // Wait for full handshake
        final boolean bothConnected = connectedPeers.await(2 * MAX_DURATION, TimeUnit.MILLISECONDS);
        final long endTime = System.nanoTime();
        assertTrue(bothConnected);
        assertFalse(eventsCalled.get("responderError"));
        assertFalse(eventsCalled.get("initiatorError"));
        long durationMs = (endTime - startTime) / 1000 / 1000;
        System.out.println("Full handshake took " + durationMs + " milliseconds");

        // Disconnect
        responder.disconnect();
        initiator.disconnect();

        assertTrue("Duration time (" + durationMs + "ms) should be less than " + MAX_DURATION + "ms",
                   durationMs < MAX_DURATION);
    }

    // Note: Unfortunately right now we cannot test the handover outside of Android,
    // as the libjingle peerconnection only works on the Android platform.

    @After
    public void tearDown() {
        initiator.disconnect();
        responder.disconnect();
    }

}
