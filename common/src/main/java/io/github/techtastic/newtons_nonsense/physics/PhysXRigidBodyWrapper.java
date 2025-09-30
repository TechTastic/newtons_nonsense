package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.lwjgl.system.MemoryStack;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxForceModeEnum;
import physx.physics.PxRigidDynamic;
import physx.physics.PxShape;

import java.util.UUID;

public class PhysXRigidBodyWrapper {
    private final UUID id;
    private final PxRigidDynamic actor = Backend.getPhysics().createRigidDynamic(new PxTransform());

    public PhysXRigidBodyWrapper(UUID id, PxShape[] shapes) {
        this.id = id;
        this.addShapes(shapes);
    }

    public PhysXRigidBodyWrapper(UUID id) {
        this(id, new PxShape[0]);
    }

    protected PxRigidDynamic getActor() {
        return this.actor;
    }

    protected void addShapes(PxShape[] shapes) {
        for (PxShape shape : shapes) {
            this.actor.attachShape(shape);
        }
    }

    public UUID getId() {
        return this.id;
    }

    public Vec3 getPosition() {
        PxVec3 vec = this.actor.getGlobalPose().getP();
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public void setPosition(Vec3 position) {
        PxVec3 vec = this.actor.getGlobalPose().getP();
        vec.setX((float)position.x);
        vec.setY((float)position.y);
        vec.setZ((float)position.z);
        this.actor.getGlobalPose().setP(vec);
    }

    public Quaterniondc getRotation() {
        PxQuat quat = this.actor.getGlobalPose().getQ();
        return new Quaterniond(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public void setRotation(Quaterniondc rotation) {
        PxQuat quat = this.actor.getGlobalPose().getQ();
        quat.setX((float)rotation.x());
        quat.setY((float)rotation.y());
        quat.setZ((float)rotation.z());
        quat.setW((float)rotation.w());
        this.actor.getGlobalPose().setQ(quat);
    }

    public Vec3 getLinearVelocity() {
        PxVec3 vec = this.actor.getLinearVelocity();
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public void setLinearVelocity(Vec3 linearVelocity) {
        PxVec3 vec = this.actor.getLinearVelocity();
        vec.setX((float)linearVelocity.x);
        vec.setY((float)linearVelocity.y);
        vec.setZ((float)linearVelocity.z);
        this.actor.setLinearVelocity(vec, true);
    }

    public Vec3 getAngularVelocity() {
        PxVec3 vec = this.actor.getAngularVelocity();
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public void setAngularVelocity(Vec3 angularVelocity) {
        PxVec3 vec = this.actor.getAngularVelocity();
        vec.setX((float)angularVelocity.x);
        vec.setY((float)angularVelocity.y);
        vec.setZ((float)angularVelocity.z);
        this.actor.setAngularVelocity(vec, true);
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
