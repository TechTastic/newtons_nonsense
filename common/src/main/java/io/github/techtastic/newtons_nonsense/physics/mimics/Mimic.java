package io.github.techtastic.newtons_nonsense.physics.mimics;

import io.github.techtastic.newtons_nonsense.util.PhysxUtils;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import physx.common.PxTransform;
import physx.extensions.PxRigidBodyExt;
import physx.physics.PxRigidBody;

public class Mimic {
    private final long id;
    private final String slug;
    private final PxRigidBody body;
    private Vec3 globalPosition;
    private Quaterniondc globalRotation;
    private double mass;

    public Mimic(long id, String slug, PxRigidBody body) {
        this.id = id;
        this.slug = slug;
        this.body = body;

        PxTransform pose = this.body.getGlobalPose();
        this.globalPosition = PhysxUtils.toVec3(pose.getP());
        this.globalRotation = PhysxUtils.toQuat(pose.getQ());
        this.mass = body.getMass();
    }

    public void sync() {
        PxTransform pose = this.body.getGlobalPose();
        this.globalPosition = PhysxUtils.toVec3(pose.getP());
        this.globalRotation = PhysxUtils.toQuat(pose.getQ());
        this.mass = body.getMass();
    }
}
