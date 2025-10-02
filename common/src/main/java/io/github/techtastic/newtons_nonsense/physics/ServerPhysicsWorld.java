package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.networking.NetworkManager;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.networking.payload.PhysicsObjectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import physx.PxTopLevelFunctions;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.extensions.PxRigidActorExt;
import physx.extensions.PxRigidBodyExt;
import physx.geometry.PxPlaneGeometry;
import physx.physics.*;
import physx.support.PxPvdSceneFlagEnum;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPhysicsWorld extends PhysicsWorld<ServerLevel> {
    private final Map<UUID, AbstractPhysicsObject> objects = new ConcurrentHashMap<>();

    private final PxScene scene;
    private boolean pause;

    protected ServerPhysicsWorld(ServerLevel level) {
        super(level);
        PxSceneDesc desc = new PxSceneDesc(Backend.PHYSICS.getTolerancesScale());
        // TODO In the future, get gravity from Level
        desc.setGravity(new PxVec3(0f, -9.81f, 0f));
        desc.setCpuDispatcher(Backend.DISPATCHER);
        desc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());

        PxVec3 vec = desc.getSanityBounds().getMinimum();
        vec.setX((float)level.getWorldBorder().getMinX());
        vec.setY((float)level.getMinBuildHeight());
        vec.setZ((float)level.getWorldBorder().getMinZ());
        desc.getSanityBounds().setMinimum(vec);

        vec = desc.getSanityBounds().getMaximum();
        vec.setX((float)level.getWorldBorder().getMaxX());
        vec.setY((float)level.getMaxBuildHeight());
        vec.setZ((float)level.getWorldBorder().getMaxZ());
        desc.getSanityBounds().setMaximum(vec);

        this.scene = Backend.PHYSICS.createScene(desc);
        desc.destroy();

        // TODO Remove, this is for debugging
        PxRigidStatic plane = Backend.PHYSICS.createRigidStatic(new PxTransform(new PxVec3(0f, -60f, 0f)));
        PxPlaneGeometry geom = new PxPlaneGeometry();
        PxMaterial material = Backend.PHYSICS.createMaterial(.5f, .5f, .5f);
        plane.attachShape(Backend.PHYSICS.createShape(geom, material));
        this.scene.addActor(plane);
    }

    public void pause(boolean pause) {
        this.pause = pause;
    }

    public void tryTick() {
        if (!this.pause)
            tick(1/20f);
    }

    public void tick(float delta) {
        this.scene.simulate(delta);
        this.scene.fetchResults(true);

        this.objects.forEach((id, object) -> {
            NewtonsNonsense.LOGGER.info("Object {} Updated!\nPosition: {}\nRotation: {}", id, object.getPosition(), object.getRotation());
            NetworkManager.sendToPlayers(this.getLevel().getPlayers(player -> true), new PhysicsObjectPayload<>(object));
        });
    }

    @Override
    public Map<UUID, AbstractPhysicsObject> getAllPhysicsObjects() {
        return Map.copyOf(this.objects);
    }

    @Override
    public @Nullable AbstractPhysicsObject getPhysicsObject(UUID id) {
        return this.objects.getOrDefault(id, null);
    }

    @Override
    public void addPhysicsObject(AbstractPhysicsObject object) {
        if (this.objects.containsKey(object.getId()))
            throw new RuntimeException("Attempted to create duplicate Physics Object with ID " + object.getId() + ", ignoring...");

        PxRigidDynamic body = Backend.PHYSICS.createRigidDynamic(new PxTransform());
        object.setPhysXBody(body);

        PxShape[] shapes = object.gatherCollisionShapes(new CollisionShapeBuilder(), this.getLevel().registryAccess()).getShapes();
        for (PxShape shape : shapes) {
            body.attachShape(shape);
        }

        this.objects.put(object.getId(), object);
        this.scene.addActor(object.getPhysXBody());

        NetworkManager.sendToPlayers(this.getLevel().getPlayers(player -> true), new PhysicsObjectPayload<>(object));
    }

    @Override
    public void removePhysicsObject(AbstractPhysicsObject object) {
        this.objects.remove(object.getId());
        this.scene.removeActor(object.getPhysXBody());
        object.getPhysXBody().release();
    }
}
