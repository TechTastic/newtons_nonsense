package io.github.techtastic.newtons_nonsense.physics.networking;

import dev.architectury.networking.NetworkManager;
import io.github.techtastic.newtons_nonsense.physics.Backend;
import io.github.techtastic.newtons_nonsense.physics.ClientPhysicsWorld;
import io.github.techtastic.newtons_nonsense.physics.networking.payload.PhysicsObjectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class Receivers {
    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PhysicsObjectPayload.TYPE, PhysicsObjectPayload.CODEC, (payload, context) ->
                context.queue(() -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    assert level != null;
                    if (Backend.getOrCreatePhysicsWorld(level) instanceof ClientPhysicsWorld world)
                        world.addPhysicsObject(payload.object());
                })
        );
    }
}
