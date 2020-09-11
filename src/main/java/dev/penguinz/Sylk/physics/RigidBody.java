package dev.penguinz.Sylk.physics;

import dev.penguinz.Sylk.util.maths.Vector2;

public class RigidBody {

    private Vector2 velocity;

    public RigidBody() {
        this.velocity = new Vector2();
    }

    public Vector2 getVelocity() {
        return velocity;
    }
}
