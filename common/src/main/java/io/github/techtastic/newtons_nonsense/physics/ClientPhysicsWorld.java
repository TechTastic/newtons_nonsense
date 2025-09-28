package io.github.techtastic.newtons_nonsense.physics;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import io.github.techtastic.newtons_nonsense.physics.client.AbstractPhysicsObjectEffect;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPhysicsWorld {
    private final ClientLevel level;
    private final Map<UUID, AbstractPhysicsObject> previousObjects = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractPhysicsObject> objects = new ConcurrentHashMap<>();

    protected ClientPhysicsWorld(ClientLevel level) {
        this.level = level;
    }

    public void onVisualReload(ClientLevel level) {
        this.objects.keySet().forEach(id -> VisualizationHelper
                .queueAdd(new AbstractPhysicsObjectEffect<>(level, this.objects.get(id), this.previousObjects.getOrDefault(id, null))));
    }

    public void update(AbstractPhysicsObject object) {
        if (this.objects.containsKey(object.getId()))
            this.previousObjects.put(object.getId(), this.objects.get(object.getId()));
        this.objects.put(object.getId(), object);
    }
}
