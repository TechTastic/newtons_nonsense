package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import io.github.techtastic.newtons_nonsense.physics.Backend;
import io.github.techtastic.newtons_nonsense.physics.ClientPhysicsWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AbstractPhysicsObjectEffect implements Effect {
    private final ClientLevel level;
    private final UUID id;

    public AbstractPhysicsObjectEffect(ClientLevel level, UUID id) {
        this.level = level;
        this.id = id;
    }

    @Override
    public @NotNull LevelAccessor level() {
        return this.level;
    }

    @Override
    public @NotNull AbstractPhysicsObjectVisual<? extends AbstractPhysicsObject> visualize(VisualizationContext visualizationContext, float v) {
        ClientPhysicsWorld world = (ClientPhysicsWorld) Backend.getOrCreatePhysicsWorld(this.level);
        AbstractPhysicsObject object = world.getPhysicsObject(this.id);
        AbstractPhysicsObject previousObject = world.getPhysicsObject(this.id);
        assert object != null;
        return object.getType().createVisual(this.level, object, previousObject, visualizationContext);
    }
}