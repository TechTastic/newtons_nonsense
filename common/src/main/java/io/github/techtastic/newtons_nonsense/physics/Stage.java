package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.jetbrains.annotations.Nullable;
import physx.physics.*;
import physx.support.SupportFunctions;

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

        PxShape[] shapes = Backstage.getChunkAsShapes(chunk, level);
        PxRigidStatic chunkBody = Backstage.createStaticBodyWithShapes(chunk.getPos().x, 0, chunk.getPos().z, shapes);
        Stage.getOrCreateStage(level).addChunk(chunk.getPos(), chunkBody);
    }

    public static EventResult onBlockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (!(level instanceof ServerLevel sLevel)) return EventResult.pass();

        Stage stage = Stage.getOrCreateStage(sLevel);
        ChunkPos chunkPos = level.getChunkAt(pos).getPos();
        PxRigidStatic chunkBody = stage.chunkBodies.getOrDefault(chunkPos, null);
        if (chunkBody == null) return EventResult.pass();

        for (int i = 0; i < chunkBody.getNbShapes(); i++) {
            PxShape shape = SupportFunctions.PxActor_getShape(chunkBody, i);
            if (shape.getLocalPose().getP().getX() == pos.getCenter().x - chunkPos.x &&
                    shape.getLocalPose().getP().getY() == pos.getCenter().y &&
                    shape.getLocalPose().getP().getZ() == pos.getCenter().z - chunkPos.z) {
                chunkBody.detachShape(shape, true);
                shape.release();
                break;
            }
        }

        return EventResult.pass();
    }

    public static EventResult onBlockPlace(Level level, BlockPos pos, BlockState state, @Nullable Entity placer) {
        if (!(level instanceof ServerLevel sLevel)) return EventResult.pass();

        Stage stage = Stage.getOrCreateStage(sLevel);
        ChunkPos chunkPos = level.getChunkAt(pos).getPos();
        PxRigidStatic chunkBody = stage.chunkBodies.getOrDefault(chunkPos, null);
        if (chunkBody == null) return EventResult.pass();

        // I'll make a VoxelShape to PxShape later...
        // Adjust Material here

        Registry<PxMaterial> materialRegistry = level.registryAccess().lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY);
        PxMaterial defaultMaterial = materialRegistry.getValueOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

        PxShape shape = Backstage.createBoxShape(
                .5f,
                .5f,
                .5f,
                (float) (pos.getCenter().x - chunkPos.x),
                (float) pos.getCenter().y,
                (float) (pos.getCenter().z - chunkPos.z),
                defaultMaterial
        );

        chunkBody.attachShape(shape);
        return EventResult.pass();
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

    public PxRigidStatic removeChunk(ChunkPos pos) {
        PxRigidStatic actor = this.chunkBodies.remove(pos);

        if (actor != null && actor.getScene() == this.scene)
            this.scene.removeActor(actor);
        return actor;
    }

    public void removeAndFreeChunk(ChunkPos pos) {
        PxRigidStatic actor = removeChunk(pos);
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