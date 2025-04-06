package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import physx.common.PxCollection;
import physx.common.PxVec3;
import physx.extensions.PxCollectionExt;
import physx.geometry.PxTriangleMesh;
import physx.geometry.PxTriangleMeshGeometry;
import physx.physics.*;

import java.util.ArrayList;

public class Stage {
    public final PxScene scene;
    public PxRigidStatic ground = null;
    public final ArrayList<PxActor> actors;
    public final ArrayList<Mimic> mimics = new ArrayList<>();
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
        Stage.getOrCreateStage(level).step(1f/20f);
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

                        stage.addToGround(block);
                    }
                }
            }
        }
    }

    public void updateClientView(ServerPlayer player) {
        PxCollection collectedScene = PxCollectionExt.createCollection(this.scene);


    }

    /*public String serializeScene() {
        PxScene scene = Backstage.createEmptyScene();
        scene.addActor(Backstage.createDefaultBox(0f, 0f, 0f));
        PxCollection sceneCollection = PxCollectionExt.createCollection(scene);

        PxSerializationRegistry sr = PxSerialization.createSerializationRegistry(Backstage.physics);
        PxDefaultMemoryOutputStream memOut = new PxDefaultMemoryOutputStream();
        PxSerialization.complete(sceneCollection, sr);
        PxSerialization.serializeCollectionToXml(memOut, sceneCollection, sr);

        PxU8ConstPtr serData = NativeArrayHelpers.voidToU8Ptr(memOut.getData());
        byte[] bin = new byte[memOut.getSize()];
        for (int i = 0; i < bin.length; i++) {
            bin[i] = NativeArrayHelpers.getU8At(serData, i);
        }

        sr.release();
        memOut.destroy();
        sceneCollection.release();
        scene.release();

        return new String(bin);
    }

    public static void deserializeScene(String serialized) {

    }*/

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

    public void step(float dt) {
        if (!this.simulate) {
            return;
        }

        this.scene.simulate(dt);
        this.scene.fetchResults(true);

        this.mimics.forEach(Mimic::physTick);
        this.mimics.removeIf((mimic) -> {
            if (mimic.display.isRemoved()) {
                mimic.free();
                return true;
            }

            return false;
        });
    }

    public void free() {
        this.actors.forEach(PxActor::release);
        this.actors.clear();
        this.scene.release();
    }

    public void addToGround(PxShape newBlock) {
        if (this.ground == null) {
            PxVec3 localPos = newBlock.getLocalPose().getP();
            this.ground = Backstage.createStaticBody(newBlock, localPos.getX(), localPos.getY(), localPos.getZ());
            return;
        }

        this.ground.attachShape(newBlock);
    }
}