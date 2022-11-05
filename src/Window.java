import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collection;

public class Window {

    final int WIDTH = (Tower.NUMBER_OF_ELEVATORS + 1) * 10;
    final int HEIGHT = (Tower.NUMBER_OF_FLOORS + 1) * 10;

    private Collection<SimObject> simObjects = new ArrayList<>();

    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy bufferStrategy;

    public Window() {
        frame = new JFrame("Elevator Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = (JPanel) frame.getContentPane();

        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        canvas = new Canvas();
        canvas.setBounds(0, 0, WIDTH, HEIGHT);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.pack();

        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();
    }

    public void render(float interpolation) {
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.scale(1, -1);
        g.translate(0, -HEIGHT);
        g.clearRect(0, 0, WIDTH, HEIGHT);
        simObjects.forEach(simObject -> simObject.render(g, interpolation));
        g.dispose();
        bufferStrategy.show();
    }

    public Collection<SimObject> getRenderObjects() {
        return simObjects;
    }
}
