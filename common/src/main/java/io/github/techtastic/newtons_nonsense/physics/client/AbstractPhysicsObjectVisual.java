package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbstractPhysicsObjectVisual<T extends AbstractPhysicsObject> implements DynamicVisual, EffectVisual<AbstractPhysicsObjectEffect<T>> {
    private final ClientLevel level;
    private final T object;
    private final @Nullable T previousObject;
    private final VisualizationContext context;

    public AbstractPhysicsObjectVisual(ClientLevel level, T object, @Nullable T previousObject, VisualizationContext context) {
        this.level = level;
        this.object = object;
        this.previousObject = previousObject;
        this.context = context;
    }

    @Override
    public @NotNull Plan<Context> planFrame() {
        NewtonsNonsense.LOGGER.info("This is the planFrame method!");
        return RunnablePlan.of(context ->
                this.object.getType().renderObject(this.level, this.object, this.previousObject, this.context, context)
        );
    }

    @Override
    public void update(float v) {}

    @Override
    public void delete() {}
}
