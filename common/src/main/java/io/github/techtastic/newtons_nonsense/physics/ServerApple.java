package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.util.MCConversions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import physx.physics.PxForceModeEnum;
import physx.physics.PxRigidActor;
import physx.physics.PxRigidDynamic;

public class ServerApple extends Apple {
    private final PxRigidActor actor;

    private ServerApple(PxRigidActor actor) {
        this.actor = actor;
    }

    @Override
    public Vec3 getPosition() {
        return MCConversions.fromPxVec3(this.actor.getGlobalPose().getP());
    }

    @Override
    public Quaterniondc getRotation() {
        return MCConversions.fromPxQuat(this.actor.getGlobalPose().getQ());
    }

    @Override
    public Vec3 getScale() {
        return new Vec3(1, 1, 1);
    }

    @Override
    public Vec3 getLinearVelocity() {
        if (this.actor instanceof PxRigidDynamic dynamic)
            return MCConversions.fromPxVec3(dynamic.getLinearVelocity());
        return new Vec3(0, 0, 0);
    }

    @Override
    public Vec3 getAngularVelocity() {
        if (this.actor instanceof PxRigidDynamic dynamic)
            return MCConversions.fromPxVec3(dynamic.getAngularVelocity());
        return new Vec3(0, 0, 0);
    }

    @Environment(EnvType.SERVER)
    public void applyForceAndTorque(Vec3 force, Vec3 torque) {
        if (this.actor instanceof PxRigidDynamic dynamic) {
            dynamic.addForce(MCConversions.toPxVec3(force), PxForceModeEnum.eIMPULSE, true);
            dynamic.addTorque(MCConversions.toPxVec3(torque), PxForceModeEnum.eIMPULSE, true);
        }
    }
}
