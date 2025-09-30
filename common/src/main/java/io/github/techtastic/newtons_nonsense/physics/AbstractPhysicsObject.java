package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.fabricmc.api.EnvType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

import java.util.UUID;

public abstract class AbstractPhysicsObject {
    private UUID id;
    private Vec3 position;
    private Quaterniondc rotation;
    private Vec3 linearVelocity;
    private Vec3 angularVelocity;
    private double mass;

    private PhysXRigidBodyWrapper physxHandle;

    public AbstractPhysicsObject(UUID id, Vec3 position, Quaterniondc rotation, Vec3 linearVelocity, Vec3 angularVelocity, double mass) {
        this.id = id;
        if (Platform.getEnvironment() == Env.SERVER)
            this.setPhysXHandle(new PhysXRigidBodyWrapper(id));
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
        return this.position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
        EnvExecutor.runInEnv(EnvType.SERVER, () -> () -> {
            if (this.physxHandle != null)
                this.physxHandle.setPosition(position);
        });
    }

    public Quaterniondc getRotation() {
        return this.rotation;
    }

    public void setRotation(Quaterniondc rotation) {
        this.rotation = rotation;
        EnvExecutor.runInEnv(EnvType.SERVER, () -> () -> {
            if (this.physxHandle != null)
                this.physxHandle.setRotation(rotation);
        });
    }

    public Vec3 getLinearVelocity() {
        return this.linearVelocity;
    }

    public void setLinearVelocity(Vec3 linearVelocity) {
        this.linearVelocity = linearVelocity;
        EnvExecutor.runInEnv(EnvType.SERVER, () -> () -> {
            if (this.physxHandle != null)
                this.physxHandle.setLinearVelocity(linearVelocity);
        });
    }

    public Vec3 getAngularVelocity() {
        return this.angularVelocity;
    }

    public void setAngularVelocity(Vec3 angularVelocity) {
        this.angularVelocity = angularVelocity;
        EnvExecutor.runInEnv(EnvType.SERVER, () -> () -> {
            if (this.physxHandle != null)
                this.physxHandle.setAngularVelocity(angularVelocity);
        });
    }

    public double getMass() {
        return this.mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
        EnvExecutor.runInEnv(EnvType.SERVER, () -> () -> {
            if (this.physxHandle != null)
                this.physxHandle.setMass(mass);
        });
    }

    public void applyForce(Vec3 force) {
        if (this.physxHandle != null)
            this.physxHandle.applyForce(force);
    }

    public void applyImpulse(Vec3 impulse) {
        if (this.physxHandle != null)
            this.physxHandle.applyImpulse(impulse);
    }

    public void updateFromPhysX() {
        if (this.physxHandle != null) {
            this.position = this.physxHandle.getPosition();
            this.rotation = this.physxHandle.getRotation();
            this.linearVelocity = this.physxHandle.getLinearVelocity();
            this.angularVelocity = this.physxHandle.getAngularVelocity();
            this.mass = this.physxHandle.getMass();
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

    public void setPhysXHandle(PhysXRigidBodyWrapper handle) {
        this.physxHandle = handle;
    }

    public PhysXRigidBodyWrapper getPhysXHandle() {
        return this.physxHandle;
    }
}
