package dev.penguinz.Sylk.ui;

public interface UIEventListener {

    default void onMouseEnter() {}

    default void onMouseExit() {}

    default void onMouseHover(float mouseX, float mouseY) {}

    default void onMouseClicked(int button) {}

    default void onMouseHeld(int button) {}

    default void onMouseReleased(int button) {}

}
