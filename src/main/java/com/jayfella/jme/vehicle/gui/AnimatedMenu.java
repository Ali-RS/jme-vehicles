package com.jayfella.jme.vehicle.gui;

import com.jayfella.easing.Easings;
import com.jayfella.jme.vehicle.Main;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;

/**
 * A Lemur menu whose buttons slide in and out of the display.
 */
abstract public class AnimatedMenu extends BaseAppState {
    // *************************************************************************
    // fields

    private AnimCompleteEvent completeEvent;
    private boolean allComplete = false;
    private boolean in = true;
    final private float buttonDelay = 0.05f; // delay between successive button animations (in seconds)
    final private float duration = 0.2f; // duration of each button animation (in seconds)
    private float maxWidth; // width of the widest button
    final private float startupDelay = 0.1f; // delay before the first animation starts (in seconds)
    private float startupTime = 0; // elapsed startup delay (in seconds)
    private float time = 0; // elapsed time since the animation started (in seconds)
    final private Node node = new Node("Menu");
    private Panel[] items;
    // *************************************************************************
    // new protected methods

    protected void animateOut(AnimCompleteEvent animComplete) {
        time = 0;
        allComplete = false;
        in = false;
        this.completeEvent = animComplete;
    }

    abstract protected Button[] createItems(); // TODO return List?
    // *************************************************************************
    // BaseAppState methods

    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this menu is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        items = createItems();
        int height = app.getCamera().getHeight() - 20;

        for (Panel item : items) {
            if (item instanceof Button) {
                formatButton((Button) item);
            }

            node.attachChild(item);

            // find the widest button so we can move them all offscreen
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

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        Main.getApplication().getGuiNode().attachChild(node);
    }

    @Override
    public void update(float tpf) {
        if (allComplete) {
            return;
        }

        // There's a delay before the first button starts moving. It looks nicer.
        startupTime += tpf;
        if (startupTime < startupDelay) {
            return;
        }

        // prevent time from growing endlessly
        time = FastMath.clamp(time + tpf, 0, 100);

        for (int i = 0; i < items.length; i++) {
            float currentDelay = buttonDelay * i;
            // make each button wait its turn
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
                allComplete = true;

                if (completeEvent != null) {
                    completeEvent.completed();
                    completeEvent = null;
                }
            }
        }
    }
    // *************************************************************************
    // private methods

    private void formatButton(Button item) {
        item.setTextHAlignment(HAlignment.Center);
        ((TbtQuadBackgroundComponent) item.getBackground()).setMargin(10, 5);
        item.setFontSize(16);
        item.setInsets(new Insets3f(0, 0, 5, 0));
    }
}
