package io.github.techtastic.newtons_nonsense.physics.object.box;

import io.github.techtastic.newtons_nonsense.PhysicsObjectTypes;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.physics.CollisionShapeBuilder;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BoxPhysicsObject extends AbstractPhysicsObject {
    private Vec3 dimensions;
    private BlockState state;

    public BoxPhysicsObject(CompoundTag nbt) {
        super(nbt);
    }

    public BoxPhysicsObject(UUID id, Vec3 position, Vec3 dimensions, BlockState state) {
        super(id);
        this.dimensions = dimensions;
        this.state = state;

        this.setPosition(position);

        // TODO Use BlockState for Material later
    }

    public BoxPhysicsObject(Vec3 position, Vec3 dimensions, BlockState state) {
        this(UUID.randomUUID(), position, dimensions, state);
    }

    public BlockState getState() {
        return this.state;
    }

    public Vec3 getDimensions() {
        return dimensions;
    }
    public void setDimensions(Vec3 dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    protected @NotNull CollisionShapeBuilder gatherCollisionShapes(@NotNull CollisionShapeBuilder builder, @NotNull RegistryAccess access) {
        return builder.box(access, dimensions);
    }

    @Override
    protected void serializeAdditionalNBT(CompoundTag nbt) {
        nbt.putDouble("dim_x", dimensions.x);
        nbt.putDouble("dim_y", dimensions.y);
        nbt.putDouble("dim_z", dimensions.z);
        BlockState.CODEC.encode(this.state, NbtOps.INSTANCE, new CompoundTag()).ifSuccess(tag -> nbt.put("state", tag));
    }

    @Override
    protected void deserializeAdditionalNBT(CompoundTag nbt) {
        dimensions = new Vec3(nbt.getDouble("dim_x"), nbt.getDouble("dim_y"), nbt.getDouble("dim_z"));
        BlockState.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("state"))
                .ifSuccess(state -> this.state = state)
                .ifError(ignored -> this.state = Blocks.AIR.defaultBlockState());
    }

    @Override
    public AABB getBoundingBox() {
        Vec3 half = dimensions.scale(0.5);
        Vec3 position = this.getPosition();
        return new AABB(
                position.x - half.x, position.y - half.y, position.z - half.z,
                position.x + half.x, position.y + half.y, position.z + half.z
        );
    }

    @Override
    public PhysicsObjectType<BoxPhysicsObject> getType() {
        return (PhysicsObjectType<BoxPhysicsObject>) PhysicsObjectTypes.BOX_OBJECT_TYPE.get();
    }
}