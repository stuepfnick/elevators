package project.simulation;

import java.awt.*;

public interface SimObject {
    void fixedUpdate();

    void update(double deltaTime);

    void render(Graphics2D g, float interpolation);

    String getStatusText();
}