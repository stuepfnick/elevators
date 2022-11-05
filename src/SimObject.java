import java.awt.*;

public interface SimObject {
    void render(Graphics2D g, float interpolation);

    void fixedUpdate();

    void update(double interpolation);
}
