package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.networking.NetworkManager;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.networking.PhysicsObjectPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
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

public class ServerPhysicsWorld {
    private final Map<UUID, AbstractPhysicsObject> objects = new ConcurrentHashMap<>();
    private final Map<UUID, PhysXRigidBodyWrapper> wrappers = new ConcurrentHashMap<>();

    private final PxScene scene;
    private final ServerLevel level;
    private final RegistryAccess access;
    private boolean pause;

    protected ServerPhysicsWorld(Backend backend, ServerLevel level) {
        PxSceneDesc desc = new PxSceneDesc(Backend.getPhysics().getTolerancesScale());
        // TODO In the future, get gravity from Level
        desc.setGravity(new PxVec3(0f, -9.81f, 0f));
        desc.setCpuDispatcher(backend.getDispatcher());
        desc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
        this.scene = Backend.getPhysics().createScene(desc);
        desc.destroy();

        this.level = level;
        this.access = level.registryAccess();

        // TODO Remove, this is for debugging
        PxRigidStatic plane = Backend.getPhysics().createRigidStatic(new PxTransform(new PxVec3(0f, -60f, 0f)));
        PxPlaneGeometry geom = new PxPlaneGeometry();
        PxMaterial material = Backend.getPhysics().createMaterial(.5f, .5f, .5f);
        plane.attachShape(Backend.getPhysics().createShape(geom, material));
        geom.destroy();
        this.scene.addActor(plane);
    }

    public void pause(boolean pause) {
        this.pause = pause;
    }

    public void tryTick() {
        if (!this.pause)
            tick(1/60f);

        this.objects.forEach((id, object) -> {
            if (this.wrappers.containsKey(id)) {
                object.updateFromPhysX();
                NewtonsNonsense.LOGGER.info("Object {} Updated!\nPosition: {}\nRotation: {}", id, object.getPosition(), object.getRotation());
                NetworkManager.sendToPlayers(this.level.getPlayers(player -> true), new PhysicsObjectPayload<>(object));
            }
        });
    }

    public void tick(float delta) {
        this.scene.simulate(delta);
        this.scene.fetchResults(true);
    }

    public void addNewPhysicsObject(AbstractPhysicsObject object) {
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
}
