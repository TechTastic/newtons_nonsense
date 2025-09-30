package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.networking.NetworkManager;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.networking.PhysicsObjectPayload;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import physx.PxTopLevelFunctions;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.geometry.PxPlaneGeometry;
import physx.physics.PxMaterial;
import physx.physics.PxRigidStatic;
import physx.physics.PxScene;
import physx.physics.PxSceneDesc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPhysicsWorld extends PhysicsWorld<ServerLevel> {
    private final Map<UUID, AbstractPhysicsObject> objects = new ConcurrentHashMap<>();
    private final Map<UUID, PhysXRigidBodyWrapper> wrappers = new ConcurrentHashMap<>();

    private final Backend backend;
    private final PxScene scene;
    private boolean pause;

    protected ServerPhysicsWorld(Backend backend, ServerLevel level) {
        super(level);
        this.backend = backend;
        PxSceneDesc desc = new PxSceneDesc(Backend.getPhysics().getTolerancesScale());
        // TODO In the future, get gravity from Level
        desc.setGravity(new PxVec3(0f, -9.81f, 0f));
        desc.setCpuDispatcher(backend.getDispatcher());
        desc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
        this.scene = Backend.getPhysics().createScene(desc);
        desc.destroy();

        // TODO Remove, this is for debugging
        PxRigidStatic plane = Backend.getPhysics().createRigidStatic(new PxTransform(new PxVec3(0f, -60f, 0f)));
        PxPlaneGeometry geom = new PxPlaneGeometry();
        PxMaterial material = Backend.getPhysics().createMaterial(.5f, .5f, .5f);
        plane.attachShape(Backend.getPhysics().createShape(geom, material));
        this.scene.addActor(plane);
    }

    public void pause(boolean pause) {
        this.pause = pause;
    }

    public void tryTick() {
        if (!this.pause)
            tick(1/60f);

        this.objects.forEach((id, object) -> {
            object.updateFromPhysX();
            NewtonsNonsense.LOGGER.info("Object {} Updated!\nPosition: {}\nRotation: {}", id, object.getPosition(), object.getRotation());
            NetworkManager.sendToPlayers(this.getLevel().getPlayers(player -> true), new PhysicsObjectPayload<>(object));
        });
    }

    public void tick(float delta) {
        this.scene.simulate(delta);
        this.scene.fetchResults(true);
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
        if (this.objects.containsKey(object.getId()) || this.wrappers.containsKey(object.getId()))
            throw new RuntimeException("Attempted to create duplicate Physics Object with ID " + object.getId() + ", ignoring...");

        this.objects.put(object.getId(), object);
        PhysXRigidBodyWrapper wrapper = this.wrappers.compute(object.getId(), (id, old) -> {
            if (old != null) {
                this.scene.removeActor(old.getActor());
                old.cleanup();
            }

            return object.getPhysXHandle();
        });
        if (wrapper != null)
            this.scene.addActor(wrapper.getActor());

        NewtonsNonsense.LOGGER.info("Object {} Created!\nPosition: {}\nRotation: {}", object.getId(), object.getPosition(), object.getRotation());
    }

    @Override
    public void removePhysicsObject(AbstractPhysicsObject object) {
        this.wrappers.computeIfPresent(object.getId(), (id, wrapper) -> {
            this.scene.removeActor(wrapper.getActor());
            wrapper.cleanup();
            return null;
        });

        this.wrappers.remove(object.getId());
        this.objects.remove(object.getId());
    }
}
