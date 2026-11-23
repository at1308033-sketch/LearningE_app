package com.learninge.app.data.remote;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.UUID;

/**
 * Gắn {@code x-request-id}, {@code x-client-version} và token (nếu có) vào mọi lời gọi. Không ghi log metadata.
 */
final class HeaderInterceptor implements ClientInterceptor {
    private static final Metadata.Key<String> AUTHORIZATION = key("authorization");
    private static final Metadata.Key<String> REQUEST_ID = key("x-request-id");
    private static final Metadata.Key<String> CLIENT_VERSION = key("x-client-version");

    private final int clientVersion;
    private final String token;

    /**
     * @param token null cho RPC công khai.
     */
    HeaderInterceptor(int clientVersion, String token) {
        this.clientVersion = clientVersion;
        this.token = token;
    }

    @Override
    public <Q, R> ClientCall<Q, R> interceptCall(MethodDescriptor<Q, R> method, CallOptions options, Channel next) {
        return new SimpleForwardingClientCall<>(next.newCall(method, options)) {
            @Override
            public void start(Listener<R> listener, Metadata headers) {
                headers.put(REQUEST_ID, UUID.randomUUID().toString());
                headers.put(CLIENT_VERSION, String.valueOf(clientVersion));
                if (token != null) {
                    headers.put(AUTHORIZATION, "Bearer " + token);
                }
                super.start(listener, headers);
            }
        };
    }

    private static Metadata.Key<String> key(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }
}
