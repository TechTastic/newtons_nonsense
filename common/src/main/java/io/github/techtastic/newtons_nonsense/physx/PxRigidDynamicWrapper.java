package io.github.techtastic.newtons_nonsense.physx;

import io.github.techtastic.newtons_nonsense.physics.Apple;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.lwjgl.system.MemoryStack;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxForceModeEnum;
import physx.physics.PxRigidDynamic;

public abstract class PxRigidDynamicWrapper {
    private final PxRigidDynamic rigidBody;
    private final Apple apple;

    protected PxRigidDynamicWrapper(PxRigidDynamic rigidBody, Apple apple) {
        this.rigidBody = rigidBody;
        this.apple = apple;
    }

    public void setPosition(Vec3 pos) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxVec3 position = PxVec3.createAt(stack, MemoryStack::nmalloc,
                    (float)pos.x, (float)pos.y, (float)pos.z);
            PxTransform transform = this.rigidBody.getGlobalPose();
            transform.setP(position);
            this.rigidBody.setGlobalPose(transform, false);
        }
    }

    public void setRotation(Quaterniondc rot) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxQuat rotation = PxQuat.createAt(stack, MemoryStack::nmalloc,
                    (float)rot.x(), (float)rot.y(), (float)rot.z(), (float)rot.w());
            PxTransform transform = this.rigidBody.getGlobalPose();
            transform.setQ(rotation);
            this.rigidBody.setGlobalPose(transform, false);
        }
    }

    public void setLinearVelocity(Vec3 vel) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxVec3 velocity = PxVec3.createAt(stack, MemoryStack::nmalloc,
                    (float)vel.x, (float)vel.y, (float)vel.z);
            this.rigidBody.setLinearVelocity(velocity);
        }
    }

    public void applyForce(Vec3 force) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxVec3 f = PxVec3.createAt(stack, MemoryStack::nmalloc,
                    (float)force.x, (float)force.y, (float)force.z);
            this.rigidBody.addForce(f, PxForceModeEnum.eFORCE, true);
        }
    }

    public void applyImpulse(Vec3 impulse) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxVec3 i = PxVec3.createAt(stack, MemoryStack::nmalloc,
                    (float)impulse.x, (float)impulse.y, (float)impulse.z);
            this.rigidBody.addForce(i, PxForceModeEnum.eIMPULSE, true);
        }
    }

    public void setMass(float mass) {
        this.rigidBody.setMass(mass);
    }

    public void setStatic(boolean isStatic) {
        // In PhysX, you need to recreate the actor to change between static/dynamic
        // This is a simplified approach - in practice you might want to cache and recreate
    }

    public Vec3 getPosition() {
        PxTransform transform = this.rigidBody.getGlobalPose();
        PxVec3 pos = transform.getP();
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public Quaterniondc getRotation() {
        PxTransform transform = rigidBody.getGlobalPose();
        PxQuat rot = transform.getQ();
        return new Quaterniond(rot.getX(), rot.getY(), rot.getZ(), rot.getW());
    }

    public Vec3 getLinearVelocity() {
        PxVec3 vel = this.rigidBody.getLinearVelocity();
        return new Vec3(vel.getX(), vel.getY(), vel.getZ());
    }

    public Vec3 getAngularVelocity() {
        PxVec3 angVel = this.rigidBody.getAngularVelocity();
        return new Vec3(angVel.getX(), angVel.getY(), angVel.getZ());
    }

    public void syncFromPhysX() {
        this.apple.updateFromPhysX();
    }

    public PxRigidDynamic getRigidBody() { return rigidBody; }
    public Apple getPhysicsObject() { return apple; }

    public void destroy() {
        if (rigidBody != null) this.rigidBody.release();
    }
}
