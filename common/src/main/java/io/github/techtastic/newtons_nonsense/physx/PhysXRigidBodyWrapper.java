package io.github.techtastic.newtons_nonsense.physx;

import io.github.techtastic.newtons_nonsense.physics.Backend;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.lwjgl.system.MemoryStack;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxForceModeEnum;
import physx.physics.PxRigidDynamic;
import physx.physics.PxScene;
import physx.physics.PxShape;

import java.util.UUID;

public class PhysXRigidBodyWrapper {
    private final UUID id;
    private final PxRigidDynamic actor;

    public PhysXRigidBodyWrapper(UUID id, PxRigidDynamic actor) {
        this.id = id;
        this.actor = actor;
    }

    public PhysXRigidBodyWrapper(UUID id, PxScene scene, PxTransform transform, PxShape[] shapes) {
        this(id, Backend.getPhysics().createRigidDynamic(transform));

        for (PxShape shape : shapes) {
            this.actor.attachShape(shape);
        }

        scene.addActor(this.actor);
    }

    public UUID getId() {
        return this.id;
    }

    public Vec3 getPosition() {
        PxVec3 vec = this.actor.getGlobalPose().getP();
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public void setPosition(Vec3 position) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 vec = PxVec3.createAt(mem, MemoryStack::nmalloc, (float)position.x, (float)position.y, (float)position.z);
            this.actor.getGlobalPose().setP(vec);
        }
    }

    public Quaterniondc getRotation() {
        PxQuat quat = this.actor.getGlobalPose().getQ();
        return new Quaterniond(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public void setRotation(Quaterniondc rotation) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxQuat quat = PxQuat.createAt(mem, MemoryStack::nmalloc, (float)rotation.x(), (float)rotation.y(), (float)rotation.z(), (float)rotation.w());
            this.actor.getGlobalPose().setQ(quat);
        }
    }

    public Vec3 getLinearVelocity() {
        PxVec3 vec = this.actor.getLinearVelocity();
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public void setLinearVelocity(Vec3 linearVelocity) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 vec = PxVec3.createAt(mem, MemoryStack::nmalloc, (float)linearVelocity.x, (float)linearVelocity.y, (float)linearVelocity.z);
            this.actor.setLinearVelocity(vec, true);
        }
    }

    public Vec3 getAngularVelocity() {
        PxVec3 vec = this.actor.getAngularVelocity();
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public void setAngularVelocity(Vec3 angularVelocity) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 vec = PxVec3.createAt(mem, MemoryStack::nmalloc, (float)angularVelocity.x, (float)angularVelocity.y, (float)angularVelocity.z);
            this.actor.setAngularVelocity(vec, true);
        }
    }

    public double getMass() {
        return this.actor.getMass();
    }

    public void setMass(double mass) {
        this.actor.setMass((float)mass);
    }

    public void applyForce(Vec3 force) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 vec = PxVec3.createAt(mem, MemoryStack::nmalloc, (float)force.x, (float)force.y, (float)force.z);
            this.actor.addForce(vec, PxForceModeEnum.eFORCE, true);
        }
    }

    public void applyImpulse(Vec3 impulse) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 vec = PxVec3.createAt(mem, MemoryStack::nmalloc, (float)impulse.x, (float)impulse.y, (float)impulse.z);
            this.actor.addForce(vec, PxForceModeEnum.eIMPULSE, true);
        }
    }

    public void cleanup() {
        this.actor.release();
    }
}
