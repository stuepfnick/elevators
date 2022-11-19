package project.simulation;

import java.awt.*;

public interface SimObject {
    
    void fixedUpdate(double deltaTime);

    void render(Graphics2D g, float interpolation);

    String getStatusText();
}
