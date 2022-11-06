import java.awt.*;

public interface SimObject {

    void fixedUpdate();

    void update(double interpolation);

    void render(Graphics2D g, float interpolation);
}
