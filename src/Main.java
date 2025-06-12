import javax.swing.*;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in headless mode for testing");
            GamePanel gamePanel = new GamePanel();
            // Simulate more realistic player actions for testing
            for (int i = 0; i < 200; i++) {
                try {
                    Thread.sleep(50); // Faster game loop timing
                    // Simulate more natural jumping pattern
                    if (i % 15 == 0 || i % 16 == 0) { // Double-tap for controlled ascent
                        System.out.println("\nPlayer jump!");
                        gamePanel.mousePressed(null);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Neon Runner");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                
                GamePanel gamePanel = new GamePanel();
                frame.add(gamePanel);
                
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        }
    }
}