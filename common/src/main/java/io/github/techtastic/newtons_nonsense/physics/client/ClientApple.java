package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import io.github.techtastic.newtons_nonsense.physics.Apple;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;

public abstract class ClientApple extends Apple {
    private final Vec3 position;
    private final Quaterniondc rotation;
    private final Vec3 scale;
    private final Vec3 linearVelocity;
    private final Vec3 angularVelocity;

    private ClientApple(Vec3 position, Quaterniondc rotation, Vec3 scale, Vec3 linearVelocity, Vec3 angularVelocity) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.linearVelocity = linearVelocity;
        this.angularVelocity = angularVelocity;
    }

    @Override
    public Vec3 getPosition() {
        return this.position;
    }

    @Override
    public Quaterniondc getRotation() {
        return this.rotation;
    }

    @Override
    public Vec3 getScale() {
        return this.scale;
    }

    @Override
    public Vec3 getLinearVelocity() {
        return this.linearVelocity;
    }

    @Override
    public Vec3 getAngularVelocity() {
        return this.angularVelocity;
    }

    @Environment(EnvType.CLIENT)
    public abstract void render(DynamicVisual.Context context, Vec3i prevRenderOrigin);
}