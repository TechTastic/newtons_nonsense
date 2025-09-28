package io.github.techtastic.newtons_nonsense.physics;

import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.physics.object.BoxPhysicsObject;
import io.github.techtastic.newtons_nonsense.physx.PhysXRigidBodyWrapper;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
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

    public void updateFromPhysX(PhysXRigidBodyWrapper physxBody) {
        this.position = physxBody.getPosition();
        this.rotation = physxBody.getRotation();
        this.linearVelocity = physxBody.getLinearVelocity();
        this.angularVelocity = physxBody.getAngularVelocity();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("id", id);
        Vec3.CODEC.encode(this.position, NbtOps.INSTANCE, new CompoundTag()).ifSuccess(tag -> nbt.put("pos", tag));
        ExtraCodecs.QUATERNIONF.encode(new Quaternionf(this.rotation), NbtOps.INSTANCE, new CompoundTag()).ifSuccess(tag -> nbt.put("rot", tag));
        Vec3.CODEC.encode(this.linearVelocity, NbtOps.INSTANCE, new CompoundTag()).ifSuccess(tag -> nbt.put("linearVel", tag));
        Vec3.CODEC.encode(this.angularVelocity, NbtOps.INSTANCE, new CompoundTag()).ifSuccess(tag -> nbt.put("angularVel", tag));
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

    @NotNull
    protected abstract CollisionShapeBuilder gatherCollisionShapes(@NotNull CollisionShapeBuilder builder, @NotNull RegistryAccess access);

    protected abstract void serializeAdditionalNBT(CompoundTag nbt);

    protected abstract void deserializeAdditionalNBT(CompoundTag nbt);

    public abstract PhysicsObjectType<? extends AbstractPhysicsObject> getType();

    public abstract AABB getBoundingBox();

    @Environment(EnvType.CLIENT)
    public abstract void render(ClientLevel level, BoxPhysicsObject previousBox, VisualizationContext visualizationContext, DynamicVisual.Context dynamicContext);

    public void setPhysXHandle(PhysXRigidBodyWrapper handle) {
        this.physxHandle = handle;
    }

    @Environment(EnvType.SERVER)
    public PhysXRigidBodyWrapper getPhysXHandle() {
        return physxHandle;
    }
}
