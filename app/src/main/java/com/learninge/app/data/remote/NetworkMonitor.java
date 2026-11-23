package com.learninge.app.data.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Theo dõi có mạng hay không cho thanh "Đang ngoại tuyến" và đồng bộ khi mạng có lại (NFR-N1, BR-077).
 */
public final class NetworkMonitor {
    private final MutableLiveData<Boolean> online = new MutableLiveData<>();
    private final List<Runnable> onAvailable = new CopyOnWriteArrayList<>();
    private volatile boolean isOnline;

    public NetworkMonitor(Context context) {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        isOnline = manager.getActiveNetwork() != null;
        online.postValue(isOnline);
        manager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                publish(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                publish(false);
            }
        });
    }

    public LiveData<Boolean> online() {
        return online;
    }

    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Gọi lại mỗi khi mạng có lại (trên luồng của hệ thống, việc nặng phải chuyển luồng).
     */
    public void addOnAvailable(Runnable action) {
        onAvailable.add(action);
    }

    private void publish(boolean available) {
        isOnline = available;
        online.postValue(available);
        if (available) {
            onAvailable.forEach(Runnable::run);
        }
    }
}
