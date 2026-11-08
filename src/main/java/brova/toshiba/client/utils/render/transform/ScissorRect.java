package brova.toshiba.client.utils.render.transform;

public class ScissorRect implements Cloneable {

    public boolean enabled;

    public int transX;

    public int transY;

    public int x;

    public int y;

    public int width;

    public int height;

    ScissorRect() {
    }

    public ScissorRect clone() {
        try {
            return (ScissorRect) super.clone();
        } catch (CloneNotSupportedException failure) {
            throw new AssertionError(failure);
        }
    }
}
