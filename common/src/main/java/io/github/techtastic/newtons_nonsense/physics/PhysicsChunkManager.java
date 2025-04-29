package io.github.techtastic.newtons_nonsense.physics;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import io.github.techtastic.newtons_nonsense.util.PhysxUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.PxBoundedData;
import physx.common.PxStridedData;
import physx.geometry.*;
import physx.physics.PxMaterial;
import physx.physics.PxRigidStatic;
import physx.physics.PxShape;
import physx.support.PxArray_PxHeightFieldSample;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage.*;

public class PhysicsChunkManager {
    public final Stage stage;
    public final HashMap<ChunkPos, PxRigidStatic> chunkBodies = new HashMap<>();
    public final HashMap<ChunkPos, Pair<PxRigidStatic, PxRigidStatic>> chunkLODBodies = new HashMap<>();
    public final ConcurrentLinkedQueue<ChunkPos> dirtyChunks = new ConcurrentLinkedQueue<>();

    public PhysicsChunkManager(Stage stage) {
        this.stage = stage;
    }

    public void onChunkLoad(ChunkAccess chunk, ServerLevel level) {
        System.out.println("Loading Chunk " + chunk.getPos());
        Pair<PxRigidStatic, PxRigidStatic> lodBodies = this.chunkLODBodies.computeIfAbsent(chunk.getPos(), k ->
                Pair.of(this.generateChunkTerrainMesh(chunk, level), this.generateChunkHeightmap(chunk, level)));

        if (lodBodies.getSecond().getScene() == this.stage.scene)
            this.stage.removeActor(lodBodies.getSecond());

        this.stage.addActor(lodBodies.getFirst(), false);
    }

    public void onChunkUnload(ChunkAccess chunk, ServerLevel level) {
        System.out.println("Unloading Chunk " + chunk.getPos());
        Pair<PxRigidStatic, PxRigidStatic> lodBodies = this.chunkLODBodies.computeIfAbsent(chunk.getPos(), k ->
                Pair.of(this.generateChunkTerrainMesh(chunk, level), this.generateChunkHeightmap(chunk, level)));

        if (lodBodies.getFirst().getScene() == this.stage.scene)
            this.stage.removeActor(lodBodies.getFirst());

        this.stage.addActor(lodBodies.getSecond(), false);
    }

    public PxRigidStatic generateChunkTerrainMesh(ChunkAccess chunk, ServerLevel level) {
        System.out.println("Generating Chunk Terrain...");
        Registry<PxMaterial> materials = level.registryAccess().lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY);

        List<PxShape> shapes = new ArrayList<>();

        try (MemoryStack mem = MemoryStack.stackPush()){
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getMinY(); y <= chunk.getHighestFilledSectionIndex() * LevelChunkSection.SECTION_HEIGHT; y++) {
                        BlockPos pos = chunk.getPos().getBlockAt(x, y, z);

                        if (level.isStateAtPosition(pos, state -> state.isAir() || !state.isSolid() || !state.blocksMotion()))
                            continue;
                        BlockState state = level.getBlockState(pos);
                        System.out.println(state.getBlockHolder().getRegisteredName());

                        boolean exposed = false;
                        for (Direction dir : Direction.values()) {
                            BlockPos neighborPos = pos.relative(dir);
                            BlockState neighborState = chunk.getBlockState(neighborPos);

                            if (exposesNeighbor(level, neighborState, neighborPos)) {
                                exposed = true;
                                break;
                            }
                        }

                        if (!exposed) continue;

                        PxGeometry geom = Backstage.generateBlockShape(level, state, pos);
                        if (geom == null) continue;

                        PxMaterial material = materials.getValue(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                        PxShape shape = Backstage.createShapeFromGeometry(geom, material, x + .5f, y + .5f, z + .5f, true, mem);
                        shapes.add(shape);
                    }
                }
            }
        }

        BlockPos chunkBlockPos = chunk.getPos().getWorldPosition();
        System.out.println("Finishing Chunk Terrain Collision...");
        return Backstage.createStaticBodyWithShapes(chunkBlockPos.getX(), 0, chunkBlockPos.getZ(), shapes.toArray(new PxShape[] {}));
    }

    private static boolean exposesNeighbor(ServerLevel level, BlockState state, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        return state.isAir() || !state.isSolid() || !state.blocksMotion() || shape.isEmpty() || !shape.equals(Shapes.block());
    }

    public PxRigidStatic generateChunkHeightmap(ChunkAccess chunk, ServerLevel level) {
        Registry<PxMaterial> materials = level.registryAccess().lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY);
        PxMaterial material = materials.getValue(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxArray_PxHeightFieldSample samples = PxArray_PxHeightFieldSample.createAt(mem, MemoryStack::nmalloc, 16 * 16);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);

                    PxHeightFieldSample sample = PxHeightFieldSample.createAt(mem, MemoryStack::nmalloc);
                    sample.setHeight((short) height);
                    samples.set(x * 16 + z, sample);
                }
            }

            PxBoundedData samplesData = PxBoundedData.createAt(mem, MemoryStack::nmalloc);
            samplesData.setData(samples.begin());
            samplesData.setCount(samples.size());
            samplesData.setStride(PxHeightFieldSample.SIZEOF);

            PxHeightFieldDesc fieldDesc = PxHeightFieldDesc.createAt(mem, MemoryStack::nmalloc);
            fieldDesc.setNbRows(16);
            fieldDesc.setNbColumns(16);
            fieldDesc.setFlags(PxHeightFieldFlags.createAt(mem, MemoryStack::nmalloc, (short) PxHeightFieldFlagEnum.eNO_BOUNDARY_EDGES.ordinal()));
            fieldDesc.setFormat(PxHeightFieldFormatEnum.eS16_TM);
            fieldDesc.setSamples(samplesData);

            PxHeightField field = PxTopLevelFunctions.CreateHeightField(fieldDesc);
            PxHeightFieldGeometry geom = PxHeightFieldGeometry.createAt(mem, MemoryStack::nmalloc);
            geom.setHeightField(field);
            geom.setColumnScale(1);
            geom.setRowScale(1);
            geom.setHeightScale(1);

            PxShape shape = Backstage.createShapeFromGeometry(geom, material, 0,0, 0, true, mem);
            return createStaticBodyWithShapes(chunk.getPos().x, 0, chunk.getPos().z, shape);
        }
    }

    public void onBlockChanged(ChunkPos chunkPos) {
        this.dirtyChunks.add(chunkPos);
    }

    public void update(ServerLevel level) {
        List<ChunkPos> currentChunks = this.dirtyChunks.stream().distinct().toList();
        this.dirtyChunks.clear();

        for (ChunkPos pos : currentChunks) {
            ChunkAccess chunk = level.getChunk(pos.getWorldPosition());

            Pair<PxRigidStatic, PxRigidStatic> lodBodies = this.chunkLODBodies.getOrDefault(pos, null);
            if (lodBodies != null) {
                this.stage.removeAndFreeActor(lodBodies.getFirst());
                this.stage.removeAndFreeActor(lodBodies.getSecond());
            }

            lodBodies = this.chunkLODBodies.computeIfAbsent(pos, k ->
                    Pair.of(this.generateChunkTerrainMesh(chunk, level), this.generateChunkHeightmap(chunk, level)));

            this.stage.addActor(lodBodies.getFirst(), false);
        }
    }
}