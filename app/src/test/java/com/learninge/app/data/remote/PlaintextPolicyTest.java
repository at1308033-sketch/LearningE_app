package com.learninge.app.data.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Set;

public class PlaintextPolicyTest {
    private static final Set<String> LAN = Set.of("192.168.1.20");

    @Test
    public void debug_allowsOnlyListedHosts() {
        assertTrue(PlaintextPolicy.isAllowed(true, "10.0.2.2", Set.of()));
        assertTrue(PlaintextPolicy.isAllowed(true, "192.168.1.20", LAN));
        assertFalse(PlaintextPolicy.isAllowed(true, "example.com", LAN));
    }

    @Test
    public void release_neverAllows() {
        assertFalse(PlaintextPolicy.isAllowed(false, "10.0.2.2", LAN));
        assertFalse(PlaintextPolicy.isAllowed(false, "192.168.1.20", LAN));
    }
}
