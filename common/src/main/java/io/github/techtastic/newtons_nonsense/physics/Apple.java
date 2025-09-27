package io.github.techtastic.newtons_nonsense.physics;

import java.util.UUID;

public class Apple {
    private final UUID id;

    public Apple(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return this.id;
    }
}
