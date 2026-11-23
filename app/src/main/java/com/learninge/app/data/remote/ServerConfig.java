package com.learninge.app.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.learninge.app.BuildConfig;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Địa chỉ máy chủ lấy từ BuildConfig; chỉ bản debug được ghi đè (DEC-023).
 */
public record ServerConfig(String host, int port, boolean isTls) {
    private static final String FILE = "server_config";
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";

    public static ServerConfig load(Context context) {
        String host = BuildConfig.GRPC_HOST;
        int port = BuildConfig.GRPC_PORT;
        if (BuildConfig.DEBUG) {
            SharedPreferences prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
            host = prefs.getString(KEY_HOST, host);
            port = prefs.getInt(KEY_PORT, port);
        }
        return new ServerConfig(host, port, BuildConfig.GRPC_TLS);
    }

    static Set<String> lanHosts() {
        return Arrays.stream(BuildConfig.GRPC_LAN_HOSTS.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Dạng {@code host:port}, hiện ở SCR-35 bản debug.
     */
    public String display() {
        return host + ":" + port;
    }
}
