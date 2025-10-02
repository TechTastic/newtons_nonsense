package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import io.github.techtastic.newtons_nonsense.util.Conversions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxRigidDynamic;

import java.util.UUID;

public abstract class AbstractPhysicsObject {
    private UUID id;
    private Vec3 position;
    private Quaterniondc rotation;
    private Vec3 linearVelocity;
    private Vec3 angularVelocity;
    private double mass;

    private PxRigidDynamic body;

    public AbstractPhysicsObject(UUID id, Vec3 position, Quaterniondc rotation, Vec3 linearVelocity, Vec3 angularVelocity, double mass) {
        this.id = id;
        if (Minecraft.getInstance().level != null)
            this.body = Backend.getPhysics().createRigidDynamic(new PxTransform());
        this.setPosition(position);
        this.setRotation(rotation);
        this.setLinearVelocity(linearVelocity);
        this.setAngularVelocity(angularVelocity);
        this.setMass(mass);
    }

    public AbstractPhysicsObject(UUID id) {
        this(id, Vec3.ZERO, new Quaterniond(), Vec3.ZERO, Vec3.ZERO, 0.0);
    }

    public AbstractPhysicsObject(CompoundTag nbt) {
        this(nbt.getUUID("id"));
        deserializeNBT(nbt);
    }

    public UUID getId() {
        return this.id;
    }

    public Vec3 getPosition() {
        if (this.body == null)
            return this.position;
        else
            return Conversions.fromPxVec(this.body.getGlobalPose().getP());
    }

    public void setPosition(Vec3 position) {
        this.position = position;
        if (this.body != null) {
            PxVec3 pos = this.body.getGlobalPose().getP();
            pos.setX((float) position.x);
            pos.setY((float) position.y);
            pos.setZ((float) position.z);
            this.body.getGlobalPose().setP(pos);
        }
    }

    public Quaterniondc getRotation() {
        if (this.body == null)
            return this.rotation;
        else
            return Conversions.fromPxQuat(this.body.getGlobalPose().getQ());
    }

    public void setRotation(Quaterniondc rotation) {
        this.rotation = rotation;
        if (this.body != null) {
            PxQuat quat = this.body.getGlobalPose().getQ();
            quat.setX((float) rotation.x());
            quat.setY((float) rotation.y());
            quat.setZ((float) rotation.z());
            quat.setW((float) rotation.w());
            this.body.getGlobalPose().setQ(quat);
        }
    }

    public Vec3 getLinearVelocity() {
        if (this.body == null)
            return this.linearVelocity;
        else
            return Conversions.fromPxVec(this.body.getLinearVelocity());
    }

    public void setLinearVelocity(Vec3 linearVelocity) {
        this.linearVelocity = linearVelocity;
        if (this.body != null) {
            PxVec3 vel = this.body.getLinearVelocity();
            vel.setX((float) linearVelocity.x);
            vel.setY((float) linearVelocity.y);
            vel.setZ((float) linearVelocity.z);
            this.body.setLinearVelocity(vel, true);
        }
    }

    public Vec3 getAngularVelocity() {
        if (this.body == null)
            return this.angularVelocity;
        else
            return Conversions.fromPxVec(this.body.getAngularVelocity());
    }

    public void setAngularVelocity(Vec3 angularVelocity) {
        this.angularVelocity = angularVelocity;
        if (this.body != null) {
            PxVec3 vel = this.body.getAngularVelocity();
            vel.setX((float) angularVelocity.x);
            vel.setY((float) angularVelocity.y);
            vel.setZ((float) angularVelocity.z);
            this.body.setAngularVelocity(vel, true);
        }
    }

    public double getMass() {
        if (this.body == null)
            return this.mass;
        else
            return this.body.getMass();
    }

    public void setMass(double mass) {
        this.mass = mass;
        if (this.body != null)
            this.body.setMass((float) mass);
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("id", id);

        if (this.getPosition() instanceof Vec3 pos) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", pos.x);
            tag.putDouble("y", pos.y);
            tag.putDouble("z", pos.z);
            nbt.put("pos", tag);
        }
        if (this.getRotation() instanceof Quaterniond quat) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", quat.x());
            tag.putDouble("y", quat.y());
            tag.putDouble("z", quat.z());
            tag.putDouble("w", quat.w());
            nbt.put("rot", tag);
        }
        if (this.getLinearVelocity() instanceof Vec3 linear) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", linear.x);
            tag.putDouble("y", linear.y);
            tag.putDouble("z", linear.z);
            nbt.put("linearVel", tag);
        }
        if (this.getAngularVelocity() instanceof Vec3 angular) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", angular.x);
            tag.putDouble("y", angular.y);
            tag.putDouble("z", angular.z);
            nbt.put("angularVel", tag);
        }
        nbt.putDouble("mass", mass);

        // Let subclasses add their own data
        serializeAdditionalNBT(nbt);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.id = nbt.getUUID("id");
        if (nbt.contains("pos", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("pos");
            this.setPosition(new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")));
        }
        if (nbt.contains("rot", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("rot");
            this.setRotation(new Quaterniond(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getDouble("w")));
        }
        if (nbt.contains("linearVel", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("linearVel");
            this.setLinearVelocity(new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")));
        }
        if (nbt.contains("angularVel", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("angularVel");
            this.setAngularVelocity(new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")));
        }
        this.setMass(nbt.getDouble("mass"));

        deserializeAdditionalNBT(nbt);
    }

    @NotNull
    protected abstract CollisionShapeBuilder gatherCollisionShapes(@NotNull CollisionShapeBuilder builder, @NotNull RegistryAccess access);

    protected abstract void serializeAdditionalNBT(CompoundTag nbt);

    protected abstract void deserializeAdditionalNBT(CompoundTag nbt);

    public abstract PhysicsObjectType<? extends AbstractPhysicsObject> getType();

    public abstract AABB getBoundingBox();

    public void setPhysXBody(PxRigidDynamic body) {
        this.body = body;
    }

    public PxRigidDynamic getPhysXBody() {
        return this.body;
    }
}
