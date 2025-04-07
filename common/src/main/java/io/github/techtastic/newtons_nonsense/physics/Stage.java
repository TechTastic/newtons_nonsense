package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import physx.common.PxBaseFlagEnum;
import physx.common.PxIDENTITYEnum;
import physx.common.PxVec3;
import physx.extensions.PxRigidActorExt;
import physx.extensions.PxRigidBodyExt;
import physx.geometry.PxBVH;
import physx.physics.*;
import physx.support.PxShapePtr;

import java.util.ArrayList;
import java.util.HashMap;

public class Stage {
    public final PxScene scene;
    public PxRigidStatic ground = null;
    public HashMap<BlockPos, PxShape> groundShapes = new HashMap<>();
    public final ArrayList<PxActor> actors;
    private boolean simulate = false;

    public Stage(ArrayList<PxActor> actors) {
        this.scene = Backstage.createEmptyScene();
        this.actors = actors;

        this.actors.forEach(this.scene::addActor);
    }

    public Stage() {
        this(new ArrayList<>());
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
        Stage.getOrCreateStage(level).step(level, 1f/20f);
    }

    public static void onChunkLoad(ChunkAccess chunk, @Nullable ServerLevel level, SerializableChunkData data) {
        if (level == null) {
            return;
        }

        Stage stage = Stage.getOrCreateStage(level);

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

                        PxShape block = Backstage.createDefaultBoxShape(truePos.getX(), truePos.getY(), truePos.getZ());

                        // Adjust Material here

                        stage.addToGround(block, truePos);
                    }
                }
            }
        }
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

        if (this.ground != null)
            level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.ground.getGlobalPose().getP().getX(),
                    this.ground.getGlobalPose().getP().getY(),
                    this.ground.getGlobalPose().getP().getZ(),
                    0, 0, 0, 0, 0
            );

        this.groundShapes.forEach((pos, shape) -> {
            /*level.sendParticles(
                    ParticleTypes.CLOUD,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    0, 0, 0, 0, 0
            );*/

            level.sendParticles(
                    ParticleTypes.BUBBLE,
                    shape.getLocalPose().getP().getX(),
                    shape.getLocalPose().getP().getY(),
                    shape.getLocalPose().getP().getZ(),
                    0, 0, 0, 0, 0
            );
        });
    }

    public void free() {
        this.actors.forEach(PxActor::release);
        this.actors.clear();
        this.scene.release();
    }

    public void addToGround(PxShape newBlock, BlockPos pos) {
        this.groundShapes.put(pos, newBlock);

        if (this.ground == null) {
            this.ground = Backstage.createStaticBody(Backstage.createBoxGeometry(0, 0, 0), 0, 0, 0);
        }

        newBlock.setFlag(PxShapeFlagEnum.eSCENE_QUERY_SHAPE, true);
        this.ground.attachShape(newBlock);
    }
}