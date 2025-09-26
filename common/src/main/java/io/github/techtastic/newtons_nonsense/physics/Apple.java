package io.github.techtastic.newtons_nonsense.physics;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;

public abstract class Apple {
    public abstract Vec3 getPosition();

    public abstract Quaterniondc getRotation();

    public abstract Vec3 getScale();

    public abstract Vec3 getLinearVelocity();

    public abstract Vec3 getAngularVelocity();
}
