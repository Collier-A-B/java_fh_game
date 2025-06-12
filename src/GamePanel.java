import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.GraphicsEnvironment;

public class GamePanel extends JPanel implements ActionListener, MouseListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    
    private static void setupPanel(GamePanel panel) {
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        panel.setBackground(Color.BLACK);
        panel.setFocusable(true);
        panel.addMouseListener(panel);
    }
    private final Color backgroundColor1 = new Color(0, 0, 50); // Deep blue
    private final Color backgroundColor2 = Color.BLACK;
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<PowerUp> powerUps;
    private ArrayList<Enemy> enemies;
    private Timer gameTimer;
    private int score;
    private boolean gameOver;
    private boolean headlessMode = false;
    private static final boolean IS_HEADLESS = GraphicsEnvironment.isHeadless();

    public GamePanel() {
        if (!IS_HEADLESS) {
            setupPanel(this);
        }
        headlessMode = IS_HEADLESS;
        SwingUtilities.invokeLater(this::initializeGame);
    }

    private void initializeGame() {
        player = new Player(100, PANEL_HEIGHT / 2);
        obstacles = new ArrayList<>();
        powerUps = new ArrayList<>();
        enemies = new ArrayList<>();
        score = 0;
        gameOver = false;
        gameTimer = new Timer(16, this); // ~60 FPS
        
        if (headlessMode) {
            System.out.println("Game initialized in headless mode");
            System.out.println("Player starting position: x=" + player.getX() + ", y=" + player.getY());
        }
        gameTimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (headlessMode) {
            return;
        }
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable antialiasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(0, 0, backgroundColor1, 0, PANEL_HEIGHT, backgroundColor2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // Draw game elements
        if (!gameOver) {
            player.render(g2d);
            for (Obstacle obstacle : obstacles) {
                obstacle.render(g2d);
            }
            for (PowerUp powerUp : powerUps) {
                powerUp.render(g2d);
            }
            for (Enemy enemy : enemies) {
                enemy.render(g2d);
            }
            
            // Draw score
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2d.drawString("Score: " + score, 20, 30);
            
            // Draw health
            drawHealth(g2d);
        } else {
            drawGameOver(g2d);
        }
    }

    private int highScore = 0;
    private int multiplier = 1;
    private int multiplierTimer = 0;

    private void drawHealth(Graphics2D g2d) {
        int healthBarWidth = 30;
        int healthBarHeight = 10;
        int spacing = 5;
        int startX = PANEL_WIDTH - (healthBarWidth + spacing) * 3 - spacing;
        int startY = 20;

        // Draw health bars
        for (int i = 0; i < 3; i++) {
            if (i < player.getHealth()) {
                g2d.setColor(Color.CYAN);
            } else {
                g2d.setColor(Color.DARK_GRAY);
            }
            g2d.fillRect(startX + (healthBarWidth + spacing) * i, startY, healthBarWidth, healthBarHeight);
        }

        // Draw score and multiplier
        g2d.setColor(Color.MAGENTA);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        String scoreText = String.format("Score: %d", score);
        String highScoreText = String.format("High Score: %d", highScore);
        g2d.drawString(scoreText, 20, 30);
        g2d.drawString(highScoreText, 20, 55);

        if (multiplier > 1) {
            g2d.setColor(Color.YELLOW);
            String multiplierText = String.format("x%d", multiplier);
            g2d.drawString(multiplierText, 200, 30);
        }
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(Color.MAGENTA);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 40));
        String gameOverText = "GAME OVER";
        String scoreText = "Score: " + score;
        String restartText = "Click to restart";
        
        FontMetrics fm = g2d.getFontMetrics();
        int gameOverX = (PANEL_WIDTH - fm.stringWidth(gameOverText)) / 2;
        int scoreX = (PANEL_WIDTH - fm.stringWidth(scoreText)) / 2;
        int restartX = (PANEL_WIDTH - fm.stringWidth(restartText)) / 2;
        
        g2d.drawString(gameOverText, gameOverX, PANEL_HEIGHT / 2 - 40);
        g2d.drawString(scoreText, scoreX, PANEL_HEIGHT / 2);
        g2d.drawString(restartText, restartX, PANEL_HEIGHT / 2 + 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        player.update();
        
        // Update obstacles
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update();
            if (obstacle.isOffScreen()) {
                obstacles.remove(i);
                // Update score with multiplier
                int points = 10 * multiplier;
                score += points;
                
                // Increase multiplier for consecutive obstacles cleared
                multiplier++;
                multiplierTimer = 180; // 3 seconds at 60 FPS
                SoundManager.playSound("multiplier");
                
                if (score > highScore) {
                    highScore = score;
                }
                
                if (headlessMode) {
                    System.out.println("Obstacle passed. Score: " + score + " (x" + multiplier + ")");
                }
            } else if (obstacle.collidesWith(player)) {
                // Reset multiplier on collision
                multiplier = 1;
                player.takeDamage();
                SoundManager.playSound("hit");
                if (headlessMode) {
                    System.out.println("Player hit obstacle! Health: " + player.getHealth());
                }
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    SoundManager.playSound("gameover");
                    if (headlessMode) {
                        System.out.println("Game Over! Final score: " + score);
                    }
                }
            }
        }

        // Update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(player);
            if (enemy.isOffScreen()) {
                enemies.remove(i);
                if (headlessMode) {
                    System.out.println("Enemy left screen");
                }
            } else if (enemy.collidesWith(player)) {
                player.takeDamage();
                SoundManager.playSound("hit");
                if (headlessMode) {
                    System.out.println("Player hit by enemy! Health: " + player.getHealth());
                }
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    SoundManager.playSound("gameover");
                    if (headlessMode) {
                        System.out.println("Game Over! Final score: " + score);
                    }
                }
            }
        }

        // Update power-ups
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            if (powerUp.isOffScreen()) {
                powerUps.remove(i);
                if (headlessMode) {
                    System.out.println("Power-up left screen");
                }
            } else if (powerUp.collidesWith(player)) {
                if (powerUp.getType() == PowerUp.PowerUpType.ROCKET_BOOST) {
                    player.activateBoost();
                    SoundManager.playSound("powerup");
                    if (headlessMode) {
                        System.out.println("Player collected Rocket Boost!");
                    }
                } else {
                    player.heal();
                    SoundManager.playSound("powerup");
                    if (headlessMode) {
                        System.out.println("Player collected Data Pack! Health: " + player.getHealth());
                    }
                }
                powerUps.remove(i);
                score += 5;
            }
        }
        
        // Generate new obstacles with patterns based on score
        if (obstacles.size() < 3) {
            if (score > 30 && Math.random() < 0.3) {
                // Create a challenging pattern
                obstacles.add(new Obstacle(PANEL_WIDTH));
                obstacles.add(new Obstacle(PANEL_WIDTH + 300));
            } else if (score > 15 && Math.random() < 0.4) {
                // Create a moderately difficult pattern
                obstacles.add(new Obstacle(PANEL_WIDTH));
                obstacles.add(new Obstacle(PANEL_WIDTH + 250));
            } else {
                // Regular single obstacle
                obstacles.add(new Obstacle(PANEL_WIDTH));
            }
            if (headlessMode) {
                System.out.println("New obstacle pattern generated");
            }
        }
        
        // Update multiplier timer
        if (multiplierTimer > 0) {
            multiplierTimer--;
            if (multiplierTimer == 0) {
                multiplier = 1;
            }
        }

        // Generate new enemies (random chance)
        if (score > 0 && Math.random() < 0.01) { // 1% chance per frame, only after scoring
            int enemyY = (int)(Math.random() * (PANEL_HEIGHT - 50));
            // Weight enemy types based on score
            Enemy.EnemyType type;
            double rand = Math.random();
            if (score < 10) {
                type = Enemy.EnemyType.SECURITY_DRONE; // Only basic enemies at start
            } else if (score < 20) {
                type = rand < 0.7 ? Enemy.EnemyType.SECURITY_DRONE : Enemy.EnemyType.TURRET;
            } else {
                if (rand < 0.5) type = Enemy.EnemyType.SECURITY_DRONE;
                else if (rand < 0.8) type = Enemy.EnemyType.TURRET;
                else type = Enemy.EnemyType.HUNTER_BOT;
            }
            enemies.add(new Enemy(PANEL_WIDTH, enemyY, type));
            if (headlessMode) {
                System.out.println("New enemy generated: " + type);
            }
        }

        // Generate new power-ups (random chance)
        if (Math.random() < 0.003) { // 0.3% chance per frame
            int powerUpY = (int)(Math.random() * (PANEL_HEIGHT - 30));
            // Favor health packs when health is low
            PowerUp.PowerUpType type;
            if (player.getHealth() == 1 && Math.random() < 0.7) {
                type = PowerUp.PowerUpType.DATA_PACK;
            } else {
                type = PowerUp.PowerUpType.values()[(int)(Math.random() * PowerUp.PowerUpType.values().length)];
            }
            powerUps.add(new PowerUp(PANEL_WIDTH, powerUpY, type));
            if (headlessMode) {
                System.out.println("New power-up generated: " + type);
            }
        }

        // Check if the player has been at the boundary for too long
        if ((player.getY() <= 0 || player.getY() >= PANEL_HEIGHT - 20) && 
            Math.abs(player.getVelocityY()) < 0.1) {
            gameOver = true;
            if (headlessMode) {
                System.out.println("Game Over! Player stuck at boundary!");
                System.out.println("Final score: " + score);
            }
        }
    }

    // MouseListener methods
    @Override
    public void mousePressed(MouseEvent e) {
        if (!gameOver) {
            player.thrust();
            SoundManager.playSound("jump");
        } else {
            initializeGame();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    // Getters for panel dimensions
    public static int getPanelWidth() {
        return PANEL_WIDTH;
    }

    public static int getPanelHeight() {
        return PANEL_HEIGHT;
    }
}
