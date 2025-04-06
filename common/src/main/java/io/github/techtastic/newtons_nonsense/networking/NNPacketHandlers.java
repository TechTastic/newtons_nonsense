package io.github.techtastic.newtons_nonsense.networking;

import dev.architectury.networking.NetworkManager;
import physx.extensions.PxCollectionExt;

public class NNPacketHandlers {
    public static void handleS2CPackets() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, null, (buf, context) -> {

        });
    }

    public static void handelC2SPackets() {

    }
}
