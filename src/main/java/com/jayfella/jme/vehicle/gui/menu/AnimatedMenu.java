package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.gui.AnimCompleteEvent;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import java.util.List;
import jme3utilities.math.MyMath;

/**
 * A Lemur menu whose buttons slide in and out of the display.
 */
abstract class AnimatedMenu extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * delay between successive button animations (in seconds)
     */
    final private static float buttonDelay = 0.05f;
    /**
     * duration of each button animation (in seconds)
     */
    final private static float duration = 0.2f;
    /**
     * delay before the first animation starts (in seconds)
     */
    final private static float startupDelay = 0.1f;
    // *************************************************************************
    // fields

    private AnimCompleteEvent completeEvent;
    private boolean allComplete = false;
    private boolean in = true;
    /**
     * width of the widest button
     */
    private float maxWidth;
    /**
     * elapsed startup delay (in seconds)
     */
    private float startupTime = 0f;
    /**
     * elapsed time since the animation started (in seconds)
     */
    private float time = 0f;
    /**
     * items in this menu
     */
    private List<Button> buttons;
    final private Node node = new Node("Menu");
    // *************************************************************************
    // new protected methods

    protected void animateOut(AnimCompleteEvent animComplete) {
        time = 0f;
        allComplete = false;
        in = false;
        completeEvent = animComplete;
    }

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    abstract protected List<Button> createItems();

    /**
     * Detach this menu and attach the specified AppState.
     *
     * @param next what to attach (not null)
     */
    protected void goTo(AppState next) {
        AppStateManager stateManager = getStateManager();
        stateManager.attach(next);
        stateManager.detach(this);
    }
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
        buttons = createItems();

        for (Panel item : buttons) {
            if (item instanceof Button) {
                formatButton((Button) item);
            }

            node.attachChild(item);

            // find the widest button so we can move them all offscreen
            float width = item.getPreferredSize().x;
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        int height = app.getCamera().getHeight() - 20;
        for (Panel item : buttons) {
            // make all the buttons the same width
            Vector3f preferredSize = item.getPreferredSize();
            preferredSize.x = maxWidth;
            item.setPreferredSize(preferredSize);

            // position them all one below the other.
            item.setLocalTranslation(-maxWidth, height, 1f);
            height -= preferredSize.y;
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

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
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
        time = FastMath.clamp(time + tpf, 0f, 100f);

        int numButtons = buttons.size();
        for (int i = 0; i < numButtons; ++i) {
            float currentDelay = buttonDelay * i;
            // make each button wait its turn
            if (time < currentDelay) {
                continue;
            }

            float easeTime = FastMath.clamp(time - currentDelay, 0f, duration);
            Panel item = buttons.get(i);

            Vector3f translation = item.getLocalTranslation();
            if (in) {
                float x = easeInQuartic(easeTime, -maxWidth, 20f, duration);
                item.setLocalTranslation(x, translation.y, translation.z);
            } else {
                float x = easeOutQuartic(easeTime, 20f, -maxWidth, duration);
                item.setLocalTranslation(x, translation.y, translation.z);
            }

            if (i == numButtons - 1 && easeTime == duration) {
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

    private static float easeInQuartic(float time, float start, float end,
            float duration) {
        float t = time / duration; // goes from 0 -> 1
        float t2 = t * t;
        float fraction = t2 * t2; // goes from 0 -> 1
        float result = MyMath.lerp(fraction, start, end);

        return result;
    }

    private static float easeOutQuartic(float time, float start, float end,
            float duration) {
        float t = time / duration - 1f; // goes from -1 -> 0
        float t2 = t * t;
        float fraction = 1f - t2 * t2; // goes from 0 -> 1
        float result = MyMath.lerp(fraction, start, end);

        return result;
    }

    private void formatButton(Button item) {
        item.setTextHAlignment(HAlignment.Center);
        ((TbtQuadBackgroundComponent) item.getBackground()).setMargin(10f, 5f);
        item.setFontSize(16f);
        item.setInsets(new Insets3f(0f, 0f, 5f, 0f));
    }
}
