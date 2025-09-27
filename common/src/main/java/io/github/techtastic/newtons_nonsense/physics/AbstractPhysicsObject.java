package io.github.techtastic.newtons_nonsense.physics;

import com.mojang.datafixers.util.Pair;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;

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

    public void setPosition(Vec3 position) {
        this.position = position;
        if (physxHandle != null) {
            physxHandle.setPosition(position);
        }
    }

    public Quaterniondc getRotation() { return rotation; }

    public void setRotation(Quaterniondc rotation) {
        this.rotation = rotation;
        if (physxHandle != null) {
            physxHandle.setRotation(rotation);
        }
    }

    public Vec3 getLinearVelocity() { return linearVelocity; }

    public void setLinearVelocity(Vec3 linearVelocity) {
        this.linearVelocity = linearVelocity;
        if (physxHandle != null) {
            physxHandle.setLinearVelocity(linearVelocity);
        }
    }

    public double getMass() { return mass; }

    public void setMass(double mass) {
        this.mass = mass;
        if (physxHandle != null) {
            physxHandle.setMass(mass);
        }
    }

    public void applyForce(Vec3 force) {
        if (physxHandle != null) {
            physxHandle.applyForce(force);
        }
    }

    public void applyImpulse(Vec3 impulse) {
        if (physxHandle != null) {
            physxHandle.applyImpulse(impulse);
        }
    }

    public void updateFromPhysX(PhysXRigidBodyWrapper physxBody) {
        this.position = physxBody.getPosition();
        this.rotation = physxBody.getRotation();
        this.linearVelocity = physxBody.getVelocity();
        this.angularVelocity = physxBody.getAngularVelocity();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("id", id);
        nbt.put("pos", Vec3.CODEC.encode(this.position, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        nbt.put("rot", ExtraCodecs.QUATERNIONF.encode(new Quaternionf(this.rotation), NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        nbt.put("linearVel", Vec3.CODEC.encode(this.linearVelocity, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        nbt.put("angularVel", Vec3.CODEC.encode(this.angularVelocity, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        nbt.putDouble("mass", mass);

        // Let subclasses add their own data
        serializeAdditionalNBT(nbt);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        id = nbt.getUUID("id");
        this.position = Vec3.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("pos")).mapOrElse(Pair::getFirst, ignored -> Vec3.ZERO);
        this.rotation = ExtraCodecs.QUATERNIONF.decode(NbtOps.INSTANCE, nbt.getCompound("rot")).mapOrElse(pair -> new Quaterniond(pair.getFirst()), ignored -> new Quaterniond());
        this.linearVelocity = Vec3.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("linearVel")).mapOrElse(Pair::getFirst, ignored -> Vec3.ZERO);
        this.angularVelocity = Vec3.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("angularVel")).mapOrElse(Pair::getFirst, ignored -> Vec3.ZERO);
        mass = nbt.getDouble("mass");

        deserializeAdditionalNBT(nbt);
    }

    public void tick(ServerPhysicsWorld world) {}

    public void onAddedToWorld(ServerPhysicsWorld world) {}

    public void onRemovedFromWorld(ServerPhysicsWorld world) {}

    protected abstract void serializeAdditionalNBT(CompoundTag nbt);

    protected abstract void deserializeAdditionalNBT(CompoundTag nbt);

    public abstract PhysicsObjectType<? extends AbstractPhysicsObject> getType();

    public abstract AABB getBoundingBox();

    public void setPhysXHandle(PhysXRigidBodyWrapper handle) {
        this.physxHandle = handle;
    }

    public PhysXRigidBodyWrapper getPhysXHandle() {
        return physxHandle;
    }
}
