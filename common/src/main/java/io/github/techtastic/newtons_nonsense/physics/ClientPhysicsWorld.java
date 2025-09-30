package io.github.techtastic.newtons_nonsense.physics;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import io.github.techtastic.newtons_nonsense.physics.client.AbstractPhysicsObjectEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPhysicsWorld extends PhysicsWorld<ClientLevel> {
    private final Map<UUID, AbstractPhysicsObject> previousObjects = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractPhysicsObject> objects = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractPhysicsObjectEffect> effects = new ConcurrentHashMap<>();

    protected ClientPhysicsWorld(ClientLevel level) {
        super(level);
    }

    public void onVisualReload() {
        this.objects.keySet().forEach(id -> {
            if (getPhysicsObject(id) instanceof AbstractPhysicsObject object) {
                VisualizationHelper.queueAdd(this.effects.computeIfAbsent(object.getId(), uuid ->
                        new AbstractPhysicsObjectEffect(this.getLevel(), uuid)));
            }
        });
    }

    @Override
    public Map<UUID, AbstractPhysicsObject> getAllPhysicsObjects() {
        return Map.copyOf(this.objects);
    }

    @Override
    public AbstractPhysicsObject getPhysicsObject(UUID id) {
        return this.objects.getOrDefault(id, null);
    }

    @Nullable
    public AbstractPhysicsObject getPreviousPhysicsObject(UUID id) {
        return this.previousObjects.getOrDefault(id, null);
    }

    @Override
    public void addPhysicsObject(AbstractPhysicsObject object) {
        this.objects.computeIfPresent(object.getId(), (id, old) -> {
            this.previousObjects.computeIfPresent(id, (ignored, prev) -> {
                prev.deserializeNBT(old.serializeNBT());
                return prev;
            });
            this.previousObjects.computeIfAbsent(id, (ignored) -> object.getType().fromTag(old.serializeNBT()));
            return object;
        });
        this.objects.putIfAbsent(object.getId(), object);

        this.effects.computeIfPresent(object.getId(), (id, effect) -> {
            VisualizationHelper.queueRemove(effect);
            AbstractPhysicsObjectEffect newEffect = new AbstractPhysicsObjectEffect(this.getLevel(), id);
            VisualizationHelper.queueAdd(newEffect);
            return newEffect;
        });
        this.effects.putIfAbsent(object.getId(), new AbstractPhysicsObjectEffect(this.getLevel(), object.getId()));
    }

    @Override
    public void removePhysicsObject(AbstractPhysicsObject object) {
        this.effects.computeIfPresent(object.getId(), (id, effect) -> {
            VisualizationHelper.queueRemove(effect);
            return null;
        });
        this.effects.remove(object.getId());
        this.previousObjects.remove(object.getId());
        this.objects.remove(object.getId());
    }
}
