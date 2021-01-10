package penner.easing;

public class Quad {

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Quad() {
    }

    public static float  easeIn(float t,float b , float c, float d) {
        return c*(t/=d)*t + b;
    }

    public static float  easeOut(float t,float b , float c, float d) {
        return -c *(t/=d)*(t-2) + b;
    }

    public static float  easeInOut(float t,float b , float c, float d) {
        if ((t/=d/2) < 1) return c/2*t*t + b;
        return -c/2 * ((--t)*(t-2) - 1) + b;
    }

}