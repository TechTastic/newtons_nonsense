package io.github.techtastic.newtons_nonsense.physx;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.lib.model.Models;
import io.github.techtastic.newtons_nonsense.physics.Apple;
import io.github.techtastic.newtons_nonsense.util.Constants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.shapes.Shapes;
import physx.PxTopLevelFunctions;
import physx.common.PxVec3;
import physx.physics.PxRigidDynamic;
import physx.physics.PxScene;
import physx.physics.PxSceneDesc;

import java.util.HashMap;
import java.util.UUID;

public class PxSceneManager {
    private final PxScene scene;
    private final HashMap<UUID, PxRigidDynamicWrapper> rigidBodies = new HashMap<>();

    public PxSceneManager(ServerLevel level) {
        PxSceneDesc desc = new PxSceneDesc(Constants.TOLERANCES);
        // TODO In the future, get gravity from Level
        desc.setGravity(new PxVec3(0f, -9.81f, 0f));
        desc.setCpuDispatcher(Constants.DEFAULT_DISPATCHER);
        desc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
        this.scene = Constants.PHYSICS.createScene(desc);
        desc.destroy();
    }

    public PxRigidDynamicWrapper getOrCreateBasicBody(Apple apple) {
        //Models.block().meshes().get(0).material().
        //MutableVertexList ls = null;

        return rigidBodies.computeIfAbsent(apple.getId(), id -> {
            return null;
        });
    }

    public void tick(float delta) {
        this.scene.simulate(delta);
        this.scene.fetchResults(true);

        this.rigidBodies.values().forEach(PxRigidDynamicWrapper::syncFromPhysX);
    }

    public void removeBody(UUID uuid) {
        this.rigidBodies.computeIfPresent(uuid, (ignored, wrapper) -> {
            this.scene.removeActor(wrapper.getRigidBody());
            wrapper.destroy();
            return wrapper;
        });
        this.rigidBodies.remove(uuid);
    }

    public void cleanup() {
        this.rigidBodies.values().forEach(PxRigidDynamicWrapper::destroy);
        this.rigidBodies.clear();

        this.scene.release();
    }
}
