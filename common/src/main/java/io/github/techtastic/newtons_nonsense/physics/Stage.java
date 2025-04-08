package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.chunks.ChunkAggregate;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.jetbrains.annotations.Nullable;
import physx.physics.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Stage {
    public final PxScene scene = Backstage.createEmptyScene();
    public final ArrayList<PxActor> actors = new ArrayList<>();
    public final HashMap<ChunkPos, ChunkAggregate> chunkAggregates = new HashMap<>();
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
        Stage.getOrCreateStage(level).step(level, 1f/20f);
    }

    public static void onChunkLoad(ChunkAccess chunk, @Nullable ServerLevel level, SerializableChunkData data) {
        if (level == null) return;

        Stage stage = Stage.getOrCreateStage(level);
        ChunkAggregate chunkAgg = Backstage.createChunkAggregate(chunk, level.registryAccess());
        stage.scene.addAggregate(chunkAgg.aggregate());
        stage.chunkAggregates.put(chunk.getPos(), chunkAgg);
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

    public void canSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void step(ServerLevel level, float dt) {
        if (!this.simulate) {
            return;
        }

        this.scene.simulate(dt);
        this.scene.fetchResults(true);

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
    }

    public void free() {
        this.actors.forEach(PxActor::release);
        this.actors.clear();
        this.scene.release();
    }
}