package project;

import project.tower.TowerConstants;
import project.simulation.SimObject;
import project.simulation.SimulationConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class View {

    public static final int WIDTH = (TowerConstants.NUMBER_OF_ELEVATORS) * SimulationConstants.ELEVATOR_SPACING_PIXEL;
    public static final int HEIGHT = (TowerConstants.NUMBER_OF_FLOORS + 1) * SimulationConstants.FLOOR_HEIGHT_PIXEL;

    private final List<SimObject> simObjects = new ArrayList<>();
    private final List<JLabel> statusLabels = new ArrayList<>();

    private JFrame frame;
    private JFrame statusFrame;
    private BufferStrategy bufferStrategy;

    BufferedImage background;

    public View() {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        // Sim View
        frame = new JFrame("Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = (JPanel) frame.getContentPane();

        panel.setSize(WIDTH, HEIGHT);
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        Canvas canvas = new Canvas();
        canvas.setBounds(0, 0, WIDTH, HEIGHT);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.pack();

        background = new BufferedImage(WIDTH * 2, HEIGHT * 2, BufferedImage.TYPE_INT_ARGB);
        drawBackground(background.createGraphics());

        // Status View
        statusFrame = new JFrame("Status");
        statusFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        statusFrame.setMinimumSize(new Dimension(frame.getWidth(), 18 * simObjects.size()));
        statusFrame.setMaximumSize(new Dimension(frame.getWidth() * 8, 24 * simObjects.size()));
        statusFrame.setSize((frame.getWidth() * 4),20 * simObjects.size());
        statusFrame.setLayout(new GridLayout(simObjects.size(), 1));
        for (int i = 0; i < simObjects.size(); i++) {
            JLabel label = new JLabel(String.valueOf(i));
            statusFrame.add(label);
            statusLabels.add(label);
        }

        statusFrame.setLocation(0, frame.getHeight() + frame.getInsets().top + 2);
        statusFrame.setVisible(true);

        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();
    }

    public void render(float interpolation) {
        if (bufferStrategy == null) return; // View not ready yet
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.drawImage(background, 0, 0, WIDTH, HEIGHT, frame);

        setFontSize(g, 0.6f);

        for (int i = 0; i < simObjects.size(); i++) {
            var simObject = simObjects.get(i);
            simObject.render(g, interpolation);
            statusLabels.get(i).setText((i + 1) + ": " + simObject.getStatusText());
        }

        g.dispose();
        bufferStrategy.show();
    }

    private void drawBackground(Graphics2D g) {
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, WIDTH * 2, HEIGHT * 2);
        g.setColor(Color.BLUE);
        setFontSize(g, 1.5f);
        for (int i = 0; i < TowerConstants.NUMBER_OF_FLOORS + 2; i++) {
            int lineY = SimulationConstants.FLOOR_HEIGHT_PIXEL * i * 2;
            g.drawLine(0, lineY, WIDTH * 2, lineY);
            g.drawString(String.valueOf(56 - i), 0, lineY - 2);
        }
    }

    private void setFontSize(Graphics2D g, float size) {
        Font currentFont = g.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getSize() * size);
        g.setFont(newFont);
    }

    public List<SimObject> getSimObjects() {
        return simObjects;
    }

    public void close() {
        frame.dispose();
        statusFrame.dispose();
    }
}
