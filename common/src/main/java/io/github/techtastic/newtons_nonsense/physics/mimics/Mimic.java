package io.github.techtastic.newtons_nonsense.physics.mimics;

import net.minecraft.world.phys.Vec3;
import physx.common.PxTransform;
import physx.physics.PxRigidBody;

public class Mimic {
    public final Vec3 globalPosition;
    public final Vec3 globalRotation;

    public Mimic(PxRigidBody body) {
        PxTransform pose = body.getGlobalPose();
    }
}
