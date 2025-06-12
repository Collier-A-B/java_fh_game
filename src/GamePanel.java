import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements MouseListener, ActionListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int OBSTACLE_WIDTH = 80;
    private static final int GAP_HEIGHT = 200;
    private static final double OBSTACLE_SPEED = 3.0;
    private static final int MAX_ENEMIES = 5;
    private static final int MAX_POWERUPS = 2;
    private static final double MIN_OBSTACLE_DISTANCE = 300.0; // Minimum distance between obstacles
    
    private final Player player;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Obstacle> obstacles;
    private final ArrayList<PowerUp> powerUps;
    private final Random random;
    private GameState gameState;
    private Timer gameTimer;
    private int score;
    private final Color backgroundColor1;
    private final Color backgroundColor2;
    
    public enum GameState {
        RUNNING,
        GAME_OVER
    }
    
    public GamePanel() {
        random = new Random();
        backgroundColor1 = new Color(0, 0, 40);
        backgroundColor2 = new Color(0, 0, 80);
        
        player = new Player(100, PANEL_HEIGHT / 2);
        enemies = new ArrayList<>();
        obstacles = new ArrayList<>();
        powerUps = new ArrayList<>();
        score = 0;
        gameState = GameState.RUNNING;
        
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addMouseListener(this);
        
        setupTimer();
    }
    
    private void setupTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer.removeActionListener(this);
        }
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }
    
    private void resetGame() {
        player.restoreHealth();
        enemies.clear();
        obstacles.clear();
        powerUps.clear();
        score = 0;
        gameState = GameState.RUNNING;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }
    
    private void updateGame() {
        if (gameState == GameState.RUNNING) {
            player.update();
            updateEnemies();
            updateObstacles();
            updatePowerUps();
            checkCollisions();
            spawnEnemies();
            spawnObstacles();
            checkPowerUpSpawn();
            score++;
        }
    }
    
    private void updateObstacles() {
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.setX(obstacle.getX() - OBSTACLE_SPEED);
            
            if (obstacle.getX() + obstacle.getWidth() < 0) {
                obstacles.remove(i);
            }
        }
    }
    
    private void spawnObstacles() {
        if (gameState == GameState.RUNNING && random.nextInt(100) < 2) { // 2% chance per frame
            // Check distance to nearest obstacle
            boolean tooClose = false;
            for (Obstacle obstacle : obstacles) {
                if (Math.abs(PANEL_WIDTH - obstacle.getX()) < MIN_OBSTACLE_DISTANCE) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                int gapY = random.nextInt(PANEL_HEIGHT - GAP_HEIGHT);
                
                // Top obstacle
                obstacles.add(new Obstacle(
                    PANEL_WIDTH,
                    0,
                    OBSTACLE_WIDTH,
                    gapY
                ));
                
                // Bottom obstacle
                obstacles.add(new Obstacle(
                    PANEL_WIDTH,
                    gapY + GAP_HEIGHT,
                    OBSTACLE_WIDTH,
                    PANEL_HEIGHT - (gapY + GAP_HEIGHT)
                ));
            }
        }
    }
    
    private void updatePowerUps() {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            if (powerUp.isOffScreen()) {
                powerUps.remove(i);
            }
        }
    }
    
    private void checkPowerUpSpawn() {
        if (powerUps.size() < MAX_POWERUPS) {
            if (random.nextInt(100) < 1) { // 1% chance per frame
                int powerUpY = random.nextInt(PANEL_HEIGHT - 20); // 20 is PowerUp size
                
                // Check if spawn position is valid
                boolean validPosition = true;
                for (Obstacle obstacle : obstacles) {
                    if (obstacle.collidesWithRect(new Rectangle(
                        PANEL_WIDTH,
                        powerUpY,
                        20,
                        20
                    ))) {
                        validPosition = false;
                        break;
                    }
                }
                
                if (validPosition) {
                    PowerUp.PowerUpType type = random.nextInt(2) == 0 ? 
                        PowerUp.PowerUpType.ROCKET_BOOST : 
                        PowerUp.PowerUpType.DATA_PACK;
                    powerUps.add(new PowerUp(PANEL_WIDTH, powerUpY, type));
                }
            }
        }
    }
    
    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(player, obstacles);
            if (enemy.isOffScreen()) {
                enemies.remove(i);
            }
        }
    }
    
    private void spawnEnemies() {
        if (enemies.size() < MAX_ENEMIES && random.nextInt(100) < 2) {
            int enemyY = random.nextInt(PANEL_HEIGHT - 30); // 30 is Enemy size
            Enemy.EnemyType type = Enemy.EnemyType.values()[random.nextInt(Enemy.EnemyType.values().length)];
            enemies.add(new Enemy(PANEL_WIDTH, enemyY, type));
        }
    }
    
    private void checkCollisions() {
        // Check obstacle collisions
        for (Obstacle obstacle : obstacles) {
            if (obstacle.collidesWith(player)) {
                gameOver();
                return;
            }
        }
        
        // Check enemy collisions
        for (Enemy enemy : enemies) {
            if (enemy.collidesWith(player)) {
                if (!player.isInvulnerable()) {
                    player.takeDamage();
                    if (player.getHealth() <= 0) {
                        gameOver();
                        return;
                    }
                }
            }
        }
        
        // Check power-up collisions
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            if (powerUp.collidesWith(player)) {
                if (powerUp.getType() == PowerUp.PowerUpType.ROCKET_BOOST) {
                    player.activateRocketBoost();
                } else {
                    player.restoreHealth();
                }
                powerUps.remove(i);
                SoundManager.playSound("powerup");
            }
        }
    }
    
    private void gameOver() {
        gameState = GameState.GAME_OVER;
        SoundManager.playSound("gameover");
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        GradientPaint gradient = new GradientPaint(
            0, 0, backgroundColor1,
            0, PANEL_HEIGHT, backgroundColor2
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw game elements
        for (Obstacle obstacle : obstacles) {
            obstacle.render(g2d);
        }
        
        for (PowerUp powerUp : powerUps) {
            powerUp.render(g2d);
        }
        
        for (Enemy enemy : enemies) {
            enemy.render(g2d);
        }
        
        player.render(g2d);
        
        // Draw UI
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Health: " + player.getHealth(), 20, 60);
        
        if (gameState == GameState.GAME_OVER) {
            String gameOver = "GAME OVER - Click to Restart";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(gameOver);
            g2d.drawString(gameOver, (PANEL_WIDTH - textWidth) / 2, PANEL_HEIGHT / 2);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (gameState == GameState.RUNNING) {
            player.jump();
            SoundManager.playSound("jump");
        } else if (gameState == GameState.GAME_OVER) {
            resetGame();
            SoundManager.playSound("restart");
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    public static int getPanelWidth() {
        return PANEL_WIDTH;
    }
    
    public static int getPanelHeight() {
        return PANEL_HEIGHT;
    }
}
