package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import physx.physics.*;

import java.util.ArrayList;

public class Stage {
    public final PxScene scene = Backstage.createEmptyScene();
    public final ArrayList<PxActor> actors = new ArrayList<>();
    private boolean simulate = false;
    public final PhysicsChunkManager chunkManager = new PhysicsChunkManager(this);

    public Stage() {
    }

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
        Stage stage = Stage.getOrCreateStage(level);

        stage.chunkManager.update(level);
        stage.tryAndStep(level, 1f/20f);
    }

    public static void onChunkLoad(ChunkAccess chunk, @Nullable ServerLevel level) {
        if (level == null) return;

        Stage stage = Stage.getOrCreateStage(level);
        stage.chunkManager.onChunkLoad(chunk, level);
    }

    public static void onChunkUnload(ChunkAccess chunk, @Nullable ServerLevel level) {
        if (level == null) return;

        Stage stage = Stage.getOrCreateStage(level);
        stage.chunkManager.onChunkUnload(chunk, level);
    }

    public static EventResult onBlockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (!(level instanceof ServerLevel sLevel))
            return EventResult.pass();

        Stage stage = Stage.getOrCreateStage(sLevel);
        stage.chunkManager.onBlockChanged(new ChunkPos(pos));

        return EventResult.pass();
    }

    public static EventResult onBlockPlace(Level level, BlockPos pos, BlockState state, @Nullable Entity placer) {
        if (!(level instanceof ServerLevel sLevel))
            return EventResult.pass();

        Stage stage = Stage.getOrCreateStage(sLevel);
        stage.chunkManager.onBlockChanged(new ChunkPos(pos));

        return EventResult.pass();
    }

    public void addActor(PxActor actor, boolean track) {
        if (actor.getScene() != this.scene)
            this.scene.addActor(actor);
        if (track)
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
        this.actors.clear();
        this.scene.release();
    }
}