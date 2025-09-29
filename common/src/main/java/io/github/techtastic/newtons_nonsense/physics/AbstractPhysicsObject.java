package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.physx.PhysXRigidBodyWrapper;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

import java.util.UUID;

public abstract class AbstractPhysicsObject {
    protected UUID id;
    protected Vec3 position;
    protected Quaterniondc rotation;
    protected Vec3 linearVelocity;
    protected Vec3 angularVelocity;
    protected double mass;

    private PhysXRigidBodyWrapper physxHandle;

    public AbstractPhysicsObject() {
        this.id = UUID.randomUUID();
        this.position = Vec3.ZERO;
        this.rotation = new Quaterniond();
        this.linearVelocity = Vec3.ZERO;
        this.angularVelocity = Vec3.ZERO;
        this.mass = 1.0;
    }

    public AbstractPhysicsObject(CompoundTag nbt) {
        deserializeNBT(nbt);
    }

    public UUID getId() { return id; }

    public Vec3 getPosition() { return position; }

    @Environment(EnvType.SERVER)
    public void setPosition(Vec3 position) {
        this.position = position;
        if (physxHandle != null) {
            physxHandle.setPosition(position);
        }
    }

    public Quaterniondc getRotation() { return rotation; }

    @Environment(EnvType.SERVER)
    public void setRotation(Quaterniondc rotation) {
        this.rotation = rotation;
        if (physxHandle != null) {
            physxHandle.setRotation(rotation);
        }
    }

    public Vec3 getLinearVelocity() { return linearVelocity; }

    @Environment(EnvType.SERVER)
    public void setLinearVelocity(Vec3 linearVelocity) {
        this.linearVelocity = linearVelocity;
        if (physxHandle != null) {
            physxHandle.setLinearVelocity(linearVelocity);
        }
    }

    public Vec3 getAngularVelocity() { return angularVelocity; }

    @Environment(EnvType.SERVER)
    public void setAngularVelocity(Vec3 angularVelocity) {
        this.angularVelocity = angularVelocity;
        if (physxHandle != null) {
            physxHandle.setAngularVelocity(angularVelocity);
        }
    }

    public double getMass() { return mass; }

    @Environment(EnvType.SERVER)
    public void setMass(double mass) {
        this.mass = mass;
        if (physxHandle != null) {
            physxHandle.setMass(mass);
        }
    }

    @Environment(EnvType.SERVER)
    public void applyForce(Vec3 force) {
        if (physxHandle != null) {
            physxHandle.applyForce(force);
        }
    }

    @Environment(EnvType.SERVER)
    public void applyImpulse(Vec3 impulse) {
        if (physxHandle != null) {
            physxHandle.applyImpulse(impulse);
        }
    }

    public void updateFromPhysX() {
        if (physxHandle != null) {
            this.position = physxHandle.getPosition();
            this.rotation = physxHandle.getRotation();
            this.linearVelocity = physxHandle.getLinearVelocity();
            this.angularVelocity = physxHandle.getAngularVelocity();
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("id", id);

        if (this.position != null) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", this.position.x);
            tag.putDouble("y", this.position.y);
            tag.putDouble("z", this.position.z);
            nbt.put("pos", tag);
        }
        if (this.rotation != null) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", this.rotation.x());
            tag.putDouble("y", this.rotation.y());
            tag.putDouble("z", this.rotation.z());
            tag.putDouble("w", this.rotation.w());
            nbt.put("rot", tag);
        }
        if (this.linearVelocity != null) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", this.linearVelocity.x);
            tag.putDouble("y", this.linearVelocity.y);
            tag.putDouble("z", this.linearVelocity.z);
            nbt.put("linearVel", tag);
        }
        if (this.angularVelocity != null) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", this.angularVelocity.x);
            tag.putDouble("y", this.angularVelocity.y);
            tag.putDouble("z", this.angularVelocity.z);
            nbt.put("angularVel", tag);
        }
        nbt.putDouble("mass", mass);

        // Let subclasses add their own data
        serializeAdditionalNBT(nbt);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        id = nbt.getUUID("id");
        if (nbt.contains("pos", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("pos");
            this.position = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        }
        if (nbt.contains("rot", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("rot");
            this.rotation = new Quaterniond(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getDouble("w"));
        }
        if (nbt.contains("linearVel", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("linearVel");
            this.linearVelocity = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        }
        if (nbt.contains("angularVel", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("angularVel");
            this.angularVelocity = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        }
        mass = nbt.getDouble("mass");

        deserializeAdditionalNBT(nbt);
    }

    @NotNull
    protected abstract CollisionShapeBuilder gatherCollisionShapes(@NotNull CollisionShapeBuilder builder, @NotNull RegistryAccess access);

    protected abstract void serializeAdditionalNBT(CompoundTag nbt);

    protected abstract void deserializeAdditionalNBT(CompoundTag nbt);

    public abstract PhysicsObjectType<? extends AbstractPhysicsObject> getType();

    public abstract AABB getBoundingBox();

    public void setPhysXHandle(PhysXRigidBodyWrapper handle) {
        this.physxHandle = handle;
    }

    @Environment(EnvType.SERVER)
    public PhysXRigidBodyWrapper getPhysXHandle() {
        return physxHandle;
    }
}
