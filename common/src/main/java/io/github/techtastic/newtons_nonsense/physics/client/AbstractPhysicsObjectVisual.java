package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import net.minecraft.client.multiplayer.ClientLevel;

public abstract class AbstractPhysicsObjectVisual<T extends AbstractPhysicsObject> implements SimpleDynamicVisual, EffectVisual<AbstractPhysicsObjectEffect> {
    private final ClientLevel level;
    private final T object;
    private final T previousObject;

    public AbstractPhysicsObjectVisual(ClientLevel level, AbstractPhysicsObject object, AbstractPhysicsObject previousObject, VisualizationContext context) {
        this.level = level;
        this.object = (T) object;
        this.previousObject = (T) previousObject;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public T getPhysicsObject() {
        return this.object;
    }

    public T getPreviousPhysicsObject() {
        return this.previousObject;
    }
}
