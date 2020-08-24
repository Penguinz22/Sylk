package dev.penguinz.Sylk.ui;

import dev.penguinz.Sylk.ui.constraints.UIConstraints;

import java.util.ArrayList;
import java.util.List;

public class UIComponent {

    private UIComponent parent;
    private UIConstraints constraints;

    public List<UIComponent> children = new ArrayList<>();

    public void addComponent(UIComponent child, UIConstraints constraints) {
        child.setParent(this);
        child.setConstraints(constraints);
        child.updateConstraints();

        this.children.add(child);
    }

    public void updateConstraints() {
        this.constraints.update(this.parent != null ? this.parent.constraints : null);

        this.children.forEach(UIComponent::updateConstraints);
    }

    void setConstraints(UIConstraints constraints) {
        this.constraints = constraints;
    }

    public void setParent(UIComponent parent) {
        this.parent = parent;
    }

    public UIConstraints getConstraints() {
        return constraints;
    }

    List<UIComponent> getChildren() {
        return children;
    }
}