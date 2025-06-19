package io.github.techtastic.newtons_nonsense.physics.pipeline;

import com.google.common.collect.ImmutableList;
import io.github.techtastic.newtons_nonsense.util.PhysxUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.geometry.*;
import physx.physics.PxRigidStatic;
import physx.physics.PxScene;
import physx.physics.PxShape;
import physx.physics.PxShapeExt;
import physx.support.PxArray_PxHeightFieldSample;

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

        RegistryAccess access = level.registryAccess();

        CompletableFuture.runAsync(() -> {
            try (MemoryStack mem = MemoryStack.stackPush()) {
                ConcurrentHashMap<BlockPos, List<PxShape>> loadedShapes = new ConcurrentHashMap<>();
                PxRigidStatic loadedBody = Orchard.PX_PHYSICS.createRigidStatic(new PxTransform(PhysxUtils.toPxVec3(chunkAccess.getPos().x, 0, chunkAccess.getPos().z)));
                PxRigidStatic unloadedBody = Orchard.PX_PHYSICS.createRigidStatic(new PxTransform(PhysxUtils.toPxVec3(chunkAccess.getPos().x, 0, chunkAccess.getPos().z)));

                Root root = new Root(chunkAccess.getPos(), this.scene, loadedBody, loadedShapes, unloadedBody);


                PxArray_PxHeightFieldSample samples = new PxArray_PxHeightFieldSample();

                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                    pos = pos.setX(x);
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {
                        pos = pos.setZ(z);

                        // Height Field Stuff
                        int maxHeight = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                        PxHeightFieldSample sample = PxHeightFieldSample.createAt(mem, MemoryStack::nmalloc);
                        sample.setHeight((short) maxHeight);
                        samples.pushBack(sample);

                        for (int y = maxHeight; y > chunkAccess.getMinY(); y--) {
                            pos = pos.setY(y);

                            BlockState state = chunkAccess.getBlockState(pos);
                            root.onBlockChange(level, pos, state);
                        }
                    }
                }

                PxHeightFieldDesc heightFieldDesc = PxHeightFieldDesc.createAt(mem, MemoryStack::nmalloc);
                heightFieldDesc.setNbColumns(LevelChunkSection.SECTION_WIDTH);
                heightFieldDesc.setNbRows(LevelChunkSection.SECTION_WIDTH);
                heightFieldDesc.setFormat(PxHeightFieldFormatEnum.eS16_TM);
                heightFieldDesc.setSamples(samples);
            }
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
        private final ConcurrentHashMap<BlockPos, List<PxShape>> loadedShapes;
        private final PxRigidStatic unloadedBody;

        protected Root(ChunkPos chunkPos, PxScene scene, PxRigidStatic loadedBody, ConcurrentHashMap<BlockPos, List<PxShape>> loadedShapes, PxRigidStatic unloadedBody) {
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
