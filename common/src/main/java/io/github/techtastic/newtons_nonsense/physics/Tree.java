package io.github.techtastic.newtons_nonsense.physics;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.joml.Quaternionf;
import physx.geometry.PxHeightField;
import physx.physics.PxRigidStatic;
import physx.physics.PxScene;
import physx.physics.PxShape;

import java.util.HashMap;
import java.util.List;

public class Tree {
    public final ResourceKey<Level> dimensionId;
    private final PxScene scene;
    private boolean isPaused = false;
    private final HashMap<ChunkPos, ChunkHandler> chunks = new HashMap<>();

    protected Tree(ResourceKey<Level> dimensionId, PxScene scene) {
        this.dimensionId = dimensionId;
        this.scene = scene;
    }

    protected void pause(boolean pause) {
        this.isPaused = pause;
    }

    protected void step(float dt) {
        if (this.isPaused) return;
        this.scene.simulate(dt);
        this.scene.fetchResults(true);
    }

    public void onChunkLoad(ServerLevel level, ChunkAccess chunkAccess) {
        chunks.computeIfAbsent(chunkAccess.getPos(), chunkPos ->
                new ChunkHandler(this.scene, chunkPos)).onChunkLoad(level, chunkAccess);
    }

    public void onChunkUnload(ServerLevel level, ChunkAccess chunkAccess) {
        chunks.computeIfAbsent(chunkAccess.getPos(), chunkPos ->
                new ChunkHandler(this.scene, chunkPos)).onChunkUnload(level, chunkAccess);
    }

    public void onBlockChanged(ServerLevel level, BlockPos pos, BlockState newState) {
        chunks.computeIfAbsent(level.getChunk(pos).getPos(), chunkPos ->
                new ChunkHandler(this.scene, chunkPos)).onBlockChanged(level, pos, newState);
    }

    protected static class ChunkHandler {
        private final ChunkPos chunkPos;
        private final PxRigidStatic chunkBody;

        private final PxHeightField cachedHeightField;
        private final HashMap<BlockPos, List<PxShape>> cachedShapes;

        protected ChunkHandler(PxScene scene, ChunkPos chunkPos) {
            this.chunkPos = chunkPos;

            this.chunkBody = Orchard.createEmptyStaticBody(chunkPos.getWorldPosition().getCenter().toVector3f(), new Quaternionf());
            scene.addActor(this.chunkBody);

            this.cachedHeightField = null;
            this.cachedShapes = new HashMap<>();
        }

        // Create and/or swap to `cachedShapes`, also cache HeightField
        protected void onChunkLoad(ServerLevel level, ChunkAccess chunkAccess) {}

        // Swap to `cachedHeightField`
        protected void onChunkUnload(ServerLevel level, ChunkAccess chunkAccess) {}

        // Change cached info, update accordingly
        protected void onBlockChanged(ServerLevel level, BlockPos pos, BlockState newState) {}
    }
}
