package io.github.techtastic.newtons_nonsense.physics.object;

import io.github.techtastic.newtons_nonsense.PhysicsObjectTypes;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BoxPhysicsObject extends AbstractPhysicsObject {
    private Vec3 dimensions;

    public BoxPhysicsObject(CompoundTag nbt) {
        super(nbt);
    }

    public BoxPhysicsObject(Vec3 position, Vec3 dimensions, ServerLevel level, BlockState state) {
        super();
        this.position = position;
        this.dimensions = dimensions;

        // TODO Use BlockState for Material later
    }

    public Vec3 getDimensions() {
        return dimensions;
    }
    public void setDimensions(Vec3 dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    protected void serializeAdditionalNBT(CompoundTag nbt) {
        nbt.putDouble("dim_x", dimensions.x);
        nbt.putDouble("dim_y", dimensions.y);
        nbt.putDouble("dim_z", dimensions.z);
    }

    @Override
    protected void deserializeAdditionalNBT(CompoundTag nbt) {
        dimensions = new Vec3(nbt.getDouble("dim_x"), nbt.getDouble("dim_y"), nbt.getDouble("dim_z"));
    }

    @Override
    public AABB getBoundingBox() {
        Vec3 half = dimensions.scale(0.5);
        return new AABB(
                position.x - half.x, position.y - half.y, position.z - half.z,
                position.x + half.x, position.y + half.y, position.z + half.z
        );
    }

    @Override
    public PhysicsObjectType<? extends AbstractPhysicsObject> getType() {
        return PhysicsObjectTypes.BOX_OBJECT_TYPE.get();
    }
}
