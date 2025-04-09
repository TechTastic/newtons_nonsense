package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.jetbrains.annotations.Nullable;
import physx.physics.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Stage {
    public final PxScene scene = Backstage.createEmptyScene();
    public final ArrayList<PxActor> actors = new ArrayList<>();
    public final HashMap<ChunkPos, PxRigidStatic> chunkBodies = new HashMap<>();
    private boolean simulate = false;

    public static Stage getOrCreateStage(ServerLevel level) {
        return ((StageProvider) level).newtons_nonsense$getOrCreateStage();
    }

    public static void onServerLevelLoad(ServerLevel level) {
        Stage.getOrCreateStage(level).canSimulate(true);
    }

    public static void onServerLevelUnload(ServerLevel level) {
        Stage.getOrCreateStage(level).canSimulate(false);
    }

    public static void onServerLevelPostTick(ServerLevel level) {
        Stage.getOrCreateStage(level).tryAndStep(level, 1f/20f);
    }

    public static void onChunkLoad(ChunkAccess chunk, @Nullable ServerLevel level, SerializableChunkData data) {
        if (level == null) return;

        Registry<PxMaterial> materialRegistry = level.registryAccess().lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY);
        PxMaterial defaultMaterial = materialRegistry.getValueOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);
        Stage stage = Stage.getOrCreateStage(level);

        ArrayList<PxShape> shapes = new ArrayList<>();

        // Loop over sections that could contain blocks
        for (int s = chunk.getMinSectionY(); s <= chunk.getHighestFilledSectionIndex(); s++) {
            // Ignore empty sections
            if (chunk.isSectionEmpty(s))
                continue;

            // Loop over Blocks in Section
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(s));
            for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                for (int y = 0; y < LevelChunkSection.SECTION_HEIGHT; y++) {
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        // Ignore Air
                        if (state.isAir())
                            continue;

                        BlockPos truePos = chunk.getPos().getBlockAt(x, s * LevelChunkSection.SECTION_HEIGHT + y, z);

                        // Ill make a VoxelShape to PxShape later...
                        // Adjust Material here

                        PxShape shape = Backstage.createBoxShape(
                                .5f,
                                .5f,
                                .5f,
                                (float) (truePos.getCenter().x - chunk.getPos().x),
                                truePos.getY(),
                                (float) (truePos.getCenter().z - chunk.getPos().z),
                                defaultMaterial
                        );

                        shapes.addLast(shape);
                    }
                }
            }
        }

        if (shapes.isEmpty()) return;

        PxRigidStatic chunkBody = Backstage.createStaticBodyWithShapes(chunk.getPos().x, 0, chunk.getPos().z, shapes.toArray(new PxShape[0]));
        stage.addChunk(chunk.getPos(), chunkBody);
    }

    public void addActor(PxActor actor) {
        if (actor.getScene() != this.scene)
            this.scene.addActor(actor);
        this.actors.add(actor);
    }

    public void removeActor(PxActor actor) {
        if (actor.getScene() == this.scene)
            this.scene.removeActor(actor);
        this.actors.remove(actor);
    }

    public void removeAndFreeActor(PxActor actor) {
        removeActor(actor);
        actor.release();
    }

    public void addChunk(ChunkPos pos, PxRigidStatic actor) {
        if (actor.getScene() != this.scene)
            this.scene.addActor(actor);
        this.chunkBodies.put(pos, actor);
    }

    public void removeChunk(ChunkPos pos, PxRigidStatic actor) {
        if (actor.getScene() == this.scene)
            this.scene.removeActor(actor);
        this.chunkBodies.remove(pos);
    }

    public void removeAndFreeChunk(ChunkPos pos, PxRigidStatic actor) {
        removeChunk(pos, actor);
        actor.release();
    }

    public void canSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void tryAndStep(ServerLevel level, float dt) {
        this.actors.forEach((actor) -> {
            if (actor instanceof PxRigidDynamic rigid) {
                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        rigid.getGlobalPose().getP().getX(),
                        rigid.getGlobalPose().getP().getY(),
                        rigid.getGlobalPose().getP().getZ(),
                        0, 0, 0, 0, 0
                );
            }
        });

        if (!this.simulate) {
            return;
        }

        step(level, dt);
    }

    public void step(ServerLevel level, float dt) {
        this.scene.simulate(dt);
        this.scene.fetchResults(true);
    }

    public void free() {
        this.actors.forEach(PxActor::release);
        this.actors.clear();
        this.scene.release();
    }
}