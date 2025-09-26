package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.util.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import physx.PxTopLevelFunctions;
import physx.common.PxVec3;
import physx.physics.PxScene;
import physx.physics.PxSceneDesc;

import java.util.HashMap;
import java.util.UUID;

public class PhysicsLevel {
    public static final HashMap<ResourceKey<Level>, PhysicsLevel> LEVELS = new HashMap<>();

    private final PxScene scene;
    private final HashMap<UUID, PhysicsObject> objects = new HashMap<>();

    private PhysicsLevel(Level level) {
        PxSceneDesc desc = new PxSceneDesc(Constants.TOLERANCES);
        desc.setGravity(new PxVec3(0f, -9.81f, 0f));
        desc.setCpuDispatcher(Constants.DEFAULT_DISPATCHER);
        desc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
        this.scene = Constants.PHYSICS.createScene(desc);
    }

    public void tick() {
        this.scene.simulate(1f/20f);
        this.scene.fetchResults(true);
    }

    public static PhysicsLevel getOrCreatePhysicsLevel(Level level) {
        return LEVELS.computeIfAbsent(level.dimension(), ignored -> new PhysicsLevel(level));
    }
}
