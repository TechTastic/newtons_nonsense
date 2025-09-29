package io.github.techtastic.newtons_nonsense.physics;

import dev.architectury.networking.NetworkManager;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.networking.PhysicsObjectPayload;
import io.github.techtastic.newtons_nonsense.physx.PhysXRigidBodyWrapper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.PxQuat;
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
    }

    public void tick() {
        this.scene.simulate(1/20f);
        this.scene.fetchResults(true);

        this.objects.forEach((id, object) -> {
            if (this.wrappers.containsKey(id)) {
                object.updateFromPhysX();
                NewtonsNonsense.LOGGER.info("Object {} Updated!\nPosition: {}\nRotation: {}", id, object.getPosition(), object.getRotation());
                NetworkManager.sendToPlayers(this.level.getPlayers(player -> true), new PhysicsObjectPayload<>(object));
            }
        });
    }

    public void addNewPhysicsObject(AbstractPhysicsObject object) {
        PhysXRigidBodyWrapper wrapper = this.wrappers.compute(object.getId(), (id, old) -> {
            if (old != null)
                old.cleanup();

            return new PhysXRigidBodyWrapper(id, object.gatherCollisionShapes(new CollisionShapeBuilder(), this.access).getShapes());
        });

        wrapper.setPosition(object.getPosition());
        wrapper.setRotation(object.getRotation());
        wrapper.setLinearVelocity(object.getLinearVelocity());
        wrapper.setAngularVelocity(object.getAngularVelocity());
        wrapper.setMass(object.getMass());

        NewtonsNonsense.LOGGER.info("Object {} Created!\nPosition: {}\nRotation: {}", object.getId(), object.getPosition(), object.getRotation());

        this.objects.compute(object.getId(), (id, ignored) -> {
            object.setPhysXHandle(wrapper);
            this.scene.addActor(wrapper.getActor());
            return object;
        });

        //NewtonsNonsense.LOGGER.info("Has Object been Added Successfully? {} and {}", this.objects.containsKey(object.getId()), this.wrappers.containsKey(object.getId()));
    }
}
