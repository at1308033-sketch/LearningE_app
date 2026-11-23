package com.learninge.app.data.remote;

import java.util.Set;

/**
 * Chặn gRPC không mã hóa bằng mã (NFR-S12): {@code grpc-okhttp} không tuân {@code network_security_config}.
 */
final class PlaintextPolicy {
    private static final Set<String> EMULATOR_HOSTS = Set.of("10.0.2.2", "localhost", "127.0.0.1");

    private PlaintextPolicy() {
    }

    static boolean isAllowed(boolean isDebugBuild, String host, Set<String> declaredLanHosts) {
        return isDebugBuild && (EMULATOR_HOSTS.contains(host) || declaredLanHosts.contains(host));
    }
}
