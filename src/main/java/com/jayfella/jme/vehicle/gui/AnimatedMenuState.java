package com.jayfella.jme.vehicle.gui;

import com.jayfella.easing.Easings;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;

public abstract class AnimatedMenuState extends BaseAppState {

    private final Node node = new Node("Menu");
    private Panel[] items;
    private float maxWidth;
    private AnimCompleteEvent animComplete;

    protected abstract Button[] createItems();

    protected void animateOut(AnimCompleteEvent animComplete) {
        time = 0;
        animsComplete = false;
        in = false;
        this.animComplete = animComplete;
    }

    private void formatButton(Button item) {
        item.setTextHAlignment(HAlignment.Center);
        ((TbtQuadBackgroundComponent) item.getBackground()).setMargin(10, 5);
        item.setFontSize(16);
        item.setInsets(new Insets3f(0, 0, 5, 0));
    }

    @Override
    protected void initialize(Application app) {
        items = createItems();
        int height = app.getCamera().getHeight() - 20;

        for (Panel item : items) {
            if (item instanceof Button) {
                formatButton((Button) item);
            }

            node.attachChild(item);

            // find the longest button so we can set them all to -maxSize so they are all offscreen
            // then animate them in to something like x = 20
            if (item.getPreferredSize().x > maxWidth) {
                maxWidth = item.getPreferredSize().x;
            }

            // make all the buttons the same width
            item.setPreferredSize(new Vector3f(maxWidth, item.getPreferredSize().y, item.getPreferredSize().z));

            // position them all one below the other.
            item.setLocalTranslation(-maxWidth, height, 1);
            height -= (item.getPreferredSize().y);
        }
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getGuiNode().attachChild(node);
    }

    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    private float time = 0; // time passed.
    private final float duration = 0.5f; // the duration of the animation
    private final float delay = 0.1f; // the delay between each button animating in.

    private final float startTime = 0.5f; // a delay before the animations begin.
    private float startDuration = 0; // start time elapsed

    private boolean in = true;
    private boolean animsComplete = false;

    @Override
    public void update(float tpf) {
        if (animsComplete) {
            return;
        }

        // add a delay before the menu animates in. It just looks nicer.
        startDuration += tpf;
        if (startDuration < startTime) {
            return;
        }

        // stop the time from continuously growing.
        time = FastMath.clamp(time + tpf, 0, 100);

        for (int i = 0; i < items.length; i++) {
            float currentDelay = delay * i;
            // make each button wait their turn.
            if (time < currentDelay) {
                continue;
            }

            float currentTime = FastMath.clamp(time - currentDelay, 0, duration);

            Panel item = items[i];

            if (in) {
                float x = Easings.Function.Quart.easeOut(currentTime, 0, maxWidth + 20, duration);
                item.setLocalTranslation(-maxWidth + x, item.getLocalTranslation().y, item.getLocalTranslation().z);
            } else {
                float x = Easings.Function.Quart.easeOut(currentTime, 0, -maxWidth, duration);
                item.setLocalTranslation(x, item.getLocalTranslation().y, item.getLocalTranslation().z);
            }

            if (i == items.length - 1 && currentTime == duration) {
                animsComplete = true;

                if (animComplete != null) {
                    animComplete.completed();
                    animComplete = null;
                }
            }
        }
    }
}
