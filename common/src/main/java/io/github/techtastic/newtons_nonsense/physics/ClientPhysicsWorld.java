package io.github.techtastic.newtons_nonsense.physics;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import io.github.techtastic.newtons_nonsense.physics.client.AbstractPhysicsObjectEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPhysicsWorld {
    private final ClientLevel level;
    private final Map<UUID, AbstractPhysicsObject> previousObjects = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractPhysicsObject> objects = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractPhysicsObjectEffect> effects = new ConcurrentHashMap<>();

    protected ClientPhysicsWorld(ClientLevel level) {
        this.level = level;
    }

    public void onVisualReload(ClientLevel level) {
        this.objects.keySet().forEach(id -> {
            if (getPhysicsObject(id) instanceof AbstractPhysicsObject object) {
                VisualizationHelper.queueAdd(this.effects.computeIfAbsent(object.getId(), uuid ->
                        new AbstractPhysicsObjectEffect(level, uuid)));
            }
        });
    }

    @Nullable
    public AbstractPhysicsObject getPhysicsObject(UUID id) {
        return this.objects.getOrDefault(id, null);
    }

    @Nullable
    public AbstractPhysicsObject getPreviousPhysicsObject(UUID id) {
        return this.previousObjects.getOrDefault(id, null);
    }

    public void update(AbstractPhysicsObject object) {
        this.objects.computeIfPresent(object.getId(), (id, old) -> {
            this.previousObjects.computeIfPresent(id, (ignored, prev) -> {
                prev.deserializeNBT(old.serializeNBT());
                return prev;
            });
            this.previousObjects.computeIfAbsent(id, (ignored) -> object.getType().create(old.serializeNBT()));
            return object;
        });
        this.objects.putIfAbsent(object.getId(), object);

        this.effects.computeIfPresent(object.getId(), (id, effect) -> {
            VisualizationHelper.queueRemove(effect);
            AbstractPhysicsObjectEffect newEffect = new AbstractPhysicsObjectEffect(level, id);
            VisualizationHelper.queueAdd(newEffect);
            return newEffect;
        });
        this.effects.putIfAbsent(object.getId(), new AbstractPhysicsObjectEffect(level, object.getId()));
    }
}
