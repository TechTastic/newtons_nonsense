package io.github.techtastic.newtons_nonsense.physics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import physx.physics.PxRigidStatic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhysicsChunkManager {
    public final Stage stage;
    public final HashMap<ChunkPos, PxRigidStatic> chunkBodies = new HashMap<>();
    public final ConcurrentLinkedQueue<ChunkPos> dirtyChunks = new ConcurrentLinkedQueue<>();

    public PhysicsChunkManager(Stage stage) {
        this.stage = stage;
    }

    public void onChunkLoad(ChunkAccess chunk, ServerLevel level) {
        // Transition to or Create Triangle Mesh
    }

    public void onChunkUnload(ChunkAccess chunk, ServerLevel level) {
        // Transition to or Create Heightmap
    }

    public PxRigidStatic generateChunkTerrainMesh(ChunkAccess chunk, ServerLevel level) {
        return null;
    }

    public static Set<BlockPos> findExposedBlocks(ChunkAccess chunk, ServerLevel level) {
        Set<BlockPos> exposedBlocks = new HashSet<>();
        ChunkPos chunkPos = chunk.getPos();

        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                for (int y = chunk.getMinY(); y <= chunk.getHighestFilledSectionIndex() * LevelChunkSection.SECTION_HEIGHT; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir))
                        continue;
                    BlockState state = level.getBlockState(pos);

                    for (Direction dir : Direction.values()) {
                        BlockPos neighborPos = pos.relative(dir);
                        BlockState neighborState = chunk.getBlockState(neighborPos);

                        if (exposesNeighbor(level, neighborState, neighborPos)) {
                            exposedBlocks.add(pos);
                            break;
                        }
                    }
                }
            }
        }

        return exposedBlocks;
    }

    public static boolean exposesNeighbor(ServerLevel level, BlockState state, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);

        return state.isAir() || shape.isEmpty();
    }

    public PxRigidStatic generateChunkHeightmap(ChunkAccess chunk, ServerLevel level) {
        return null;
    }

    public void onBlockChanged(ChunkPos chunkPos) {
        this.dirtyChunks.add(chunkPos);
    }

    public void update(ServerLevel level) {
        List<ChunkPos> currentChunks = this.dirtyChunks.stream().distinct().toList();
        this.dirtyChunks.clear();
    }
}