import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class View extends JFrame {

    final int WIDTH = (Tower.NUMBER_OF_ELEVATORS) * Simulation.ELEVATOR_SPACING_PIXEL;
    static final int HEIGHT = (Tower.NUMBER_OF_FLOORS + 1) * Simulation.FLOOR_HEIGHT_PIXEL;

    private final List<SimObject> simObjects = new ArrayList<>();
    private final List<JLabel> statusLabels = new ArrayList<>();

    private JFrame frame;
    private BufferStrategy bufferStrategy;

    BufferedImage background;

    public View() {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        frame = new JFrame("Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = (JPanel) frame.getContentPane();

        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        Canvas canvas = new Canvas();
        canvas.setBounds(0, 0, WIDTH, HEIGHT);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.pack();

        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();

        JFrame statusView = new JFrame("Status");
        statusView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        statusView.setSize((int) (frame.getWidth() * 2.2),20 * Tower.NUMBER_OF_ELEVATORS);
        statusView.setMinimumSize(new Dimension(frame.getWidth(), 16 * Tower.NUMBER_OF_ELEVATORS));
        statusView.setMaximumSize(new Dimension(frame.getWidth() * 3, 25 * Tower.NUMBER_OF_ELEVATORS));
        statusView.setLayout(new GridLayout(Tower.NUMBER_OF_ELEVATORS, 1));
        for (int i = 0; i < Tower.NUMBER_OF_ELEVATORS; i++) {
            JLabel label = new JLabel(String.valueOf(i));
            statusView.add(label);
            statusLabels.add(label);
        }

        statusView.setLocation(0, frame.getHeight() + 24);
        statusView.setVisible(true);

        background = new BufferedImage(WIDTH * 2, HEIGHT * 2, BufferedImage.TYPE_INT_ARGB);
        drawBackground(background.createGraphics());
    }

    public void render(float interpolation) {
        if (bufferStrategy == null) return;
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.drawImage(background, 0, 0, WIDTH, HEIGHT, frame);

        setFontSize(g, 0.6f);

        for (int i = 0; i < Tower.NUMBER_OF_ELEVATORS; i++) {
            var simObject = simObjects.get(i);
            simObject.render(g, interpolation);
            statusLabels.get(i).setText(i + ": " + simObject.getStatusText());
        }

        g.dispose();
        bufferStrategy.show();
    }

    private void drawBackground(Graphics2D g) {
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, WIDTH * 2, HEIGHT * 2);
        g.setColor(Color.BLUE);
        setFontSize(g, 1.5f);
        for (int i = 0; i < Tower.NUMBER_OF_FLOORS + 2; i++) {
            int lineY = Simulation.FLOOR_HEIGHT_PIXEL * i * 2;
            g.drawLine(0, lineY, WIDTH * 2, lineY);
            g.drawString(String.valueOf(56 - i), 0, lineY - 2);
        }
    }

    private void setFontSize(Graphics2D g, float size) {
        Font currentFont = g.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getSize() * size);
        g.setFont(newFont);
    }

    public Collection<SimObject> getRenderObjects() {
        return simObjects;
    }

    public void close() {
        frame.dispose();
    }
}
