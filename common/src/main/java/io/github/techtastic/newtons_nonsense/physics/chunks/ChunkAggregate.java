package io.github.techtastic.newtons_nonsense.physics.chunks;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import physx.physics.PxAggregate;
import physx.physics.PxRigidStatic;

import java.util.HashMap;

public record ChunkAggregate(PxAggregate aggregate, HashMap<BlockPos, PxRigidStatic> posToBody) {
    public void addBlock(BlockPos pos, PxRigidStatic body) {
        this.posToBody.put(pos, body);
        this.aggregate.addActor(body);
    }

    public void removeBlock(BlockPos pos) {
        PxRigidStatic body = this.posToBody.getOrDefault(pos, null);
        if (body == null) return;
        this.posToBody.remove(pos);
        this.aggregate.removeActor(body);
        body.release();
    }

    public @Nullable PxRigidStatic getBlock(BlockPos pos) {
        return this.posToBody.getOrDefault(pos, null);
    }
}
