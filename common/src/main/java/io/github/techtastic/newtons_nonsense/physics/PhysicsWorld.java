package io.github.techtastic.newtons_nonsense.physics;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public abstract class PhysicsWorld<T extends Level> {
    private final T level;

    protected PhysicsWorld(T level) {
        this.level = level;
    }

    public T getLevel() {
        return this.level;
    }

    public abstract Map<UUID, AbstractPhysicsObject> getAllPhysicsObjects();

    public abstract @Nullable AbstractPhysicsObject getPhysicsObject(UUID uuid);

    public abstract void addPhysicsObject(AbstractPhysicsObject object);

    public abstract void removePhysicsObject(AbstractPhysicsObject object);
}
