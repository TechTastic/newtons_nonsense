package io.github.techtastic.newtons_nonsense.physics.pipeline;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import physx.PxTopLevelFunctions;
import physx.common.PxVec3;
import physx.geometry.PxHeightField;
import physx.physics.PxRigidStatic;
import physx.physics.PxScene;
import physx.physics.PxShape;
import physx.physics.PxShapeExt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AppleTree {
    private boolean paused = false;
    private final PxScene scene;
    private final ConcurrentHashMap<UUID, Apple> apples = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChunkPos, Root> roots = new ConcurrentHashMap<>();

    protected AppleTree(PxScene scene) {
        this.scene = scene;
    }

    public AppleTree() {
        this(Orchard.createEmptyScene());
    }

    // Chunk Handling

    protected void onChunkLoad(ServerLevel level, ChunkAccess chunkAccess) {
        if (this.roots.containsKey(chunkAccess.getPos())) {
            this.roots.get(chunkAccess.getPos()).onLoad();
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int maxHeight = chunkAccess.getSectionYFromSectionIndex(chunkAccess.getHighestFilledSectionIndex()) * LevelChunkSection.SECTION_HEIGHT;
        for (int y = maxHeight; y > level.getMinY(); y--) {
            pos = pos.setY(y);
            for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                pos = pos.setX(x);
                for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {
                    pos = pos.setZ(z);

                    BlockState state = chunkAccess.getBlockState(pos);
                    ImmutableList<PxShape> shapes = Orchard.getOrCreateShapesForState(level.registryAccess(), level, pos, state);
                }
            }
        }

        CompletableFuture.runAsync(() -> {

        });

        this.roots.computeIfAbsent(chunkAccess.getPos(), chunkPos -> {
            // Make new Root
            return null;
        }).onLoad();
    }

    protected void onChunkUnload(ServerLevel level, ChunkAccess chunkAccess) {
        this.roots.computeIfPresent(chunkAccess.getPos(), (chunkPos, root) -> {
            root.onUnload();
            return root;
        });
    }

    // Pausing

    public boolean isPaused() {
        return this.paused;
    }

    public void togglePause(boolean bool) {
        this.paused = bool;
    }

    // Ticking

    public void tryAndTick(float dt) {
        if (this.paused)
            return;

        tick(dt);
    }

    private void tick(float dt) {
        this.scene.simulate(dt);
        this.scene.fetchResults(true);
    }

    // Freeing

    protected void free() {
        this.togglePause(true);
        this.apples.values().forEach(Apple::free);
        this.roots.values().forEach(Root::free);
        this.scene.release();
    }

    protected static class Root {
        private final ChunkPos chunkPos;
        private final PxScene scene;
        private final PxRigidStatic loadedBody;
        private final HashMap<BlockPos, List<PxShape>> loadedShapes;
        private final PxRigidStatic unloadedBody;

        protected Root(ChunkPos chunkPos, PxScene scene, PxRigidStatic loadedBody, HashMap<BlockPos, List<PxShape>> loadedShapes, PxRigidStatic unloadedBody) {
            this.chunkPos = chunkPos;
            this.scene = scene;
            this.loadedBody = loadedBody;
            this.loadedShapes = loadedShapes;
            this.unloadedBody = unloadedBody;
        }

        protected void onLoad() {
            this.scene.addActor(loadedBody);
            this.scene.removeActor(unloadedBody);
        }

        protected void onUnload() {
            this.scene.addActor(unloadedBody);
            this.scene.removeActor(loadedBody);
        }

        protected void onBlockChange(ServerLevel level, BlockPos pos, BlockState newState) {
            if (this.loadedShapes.containsKey(pos)) {
                this.loadedShapes.get(pos).forEach(shape -> {
                    this.loadedBody.detachShape(shape);
                    shape.release();
                });
                this.loadedShapes.remove(pos);
            }

            ImmutableList<PxShape> newShapes = Orchard.getOrCreateShapesForState(level.registryAccess(), level, pos, newState);
            newShapes.forEach(shape -> {
                // Duplicate
                PxShape newShape = PxTopLevelFunctions.CloneShape(Orchard.PX_PHYSICS, shape, true);

                // Add Offset by BlockPos - ChunkPos
                float xOffset = pos.getX() - chunkPos.getMinBlockX();
                float yOffset = pos.getY();
                float zOffset = pos.getZ() - chunkPos.getMinBlockZ();

                PxVec3 vec = newShape.getLocalPose().getP();
                vec.setX(vec.getX() + xOffset);
                vec.setY(vec.getY() + yOffset);
                vec.setZ(vec.getZ() + zOffset);

                newShape.getLocalPose().setP(vec);

                // Add to loadedShapes
                this.loadedShapes.computeIfAbsent(pos, key -> new ArrayList<>()).add(newShape);

                // Add to loadedBody
                this.loadedBody.attachShape(newShape);
            });
        }

        protected void free() {
            this.loadedShapes.values().forEach(list -> list.forEach(PxShape::release));
            this.loadedBody.release();
            this.unloadedBody.release();
        }
    }
}
