public class Enemy {
    private static final int SIZE = 30;
    private static final double BASE_SPEED = 2.0;
    
    private double x, y;
    private double velocityX, velocityY;
    private final EnemyType type;
    
    public enum EnemyType {
        SECURITY_DRONE,
        HUNTER_BOT,
        TURRET
    }

    public Enemy(double x, double y, EnemyType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        
        // Set initial velocities based on type
        switch (type) {
            case SECURITY_DRONE -> {
                this.velocityX = -BASE_SPEED;
                this.velocityY = BASE_SPEED;
            }
            case HUNTER_BOT -> {
                this.velocityX = -BASE_SPEED * 0.5;
                this.velocityY = 0;
            }
            case TURRET -> {
                this.velocityX = -BASE_SPEED * 0.75;
                this.velocityY = 0;
            }
        }
    }
    
    public void update(Player player, java.util.ArrayList<Obstacle> obstacles) {
        // Store original position
        double originalX = x;
        double originalY = y;
        
        // Calculate intended movement
        double nextX = x + velocityX;
        double nextY = y + velocityY;
        
        // Check if next position would collide
        java.awt.Rectangle nextBounds = new java.awt.Rectangle(
            (int)nextX,
            (int)nextY,
            type == EnemyType.HUNTER_BOT ? SIZE + 10 : SIZE,
            SIZE
        );
        
        boolean willCollide = false;
        for (Obstacle obstacle : obstacles) {
            if (obstacle.collidesWithRect(nextBounds)) {
                willCollide = true;
                break;
            }
        }
        
        // Only move if no collision would occur
        if (!willCollide) {
            x = nextX;
            y = nextY;
            
            // Process AI behavior
            switch (type) {
                case SECURITY_DRONE -> {
                    // Bounce off screen edges
                    if (y <= 0 || y >= GamePanel.getPanelHeight() - SIZE) {
                        velocityY *= -1;
                    }
                }
                case HUNTER_BOT -> {
                    if (!isPathBlocked(player, obstacles)) {
                        // Chase player with reduced speed
                        double dy = player.getY() - y;
                        velocityY = Math.signum(dy) * BASE_SPEED * 0.5;
                    }
                }
                case TURRET -> {
                    // Maintain constant left movement
                }
            }
        } else {
            // On collision, reverse direction with reduced speed
            if (type != EnemyType.TURRET) {  // Turrets just stop
                velocityX *= -0.5;
                velocityY *= -0.5;
            } else {
                velocityX = 0;
                velocityY = 0;
            }
            // Ensure we don't get stuck in the obstacle
            x = originalX;
            y = originalY;
        }
        
        // Apply speed limits
        velocityX = Math.max(-BASE_SPEED, Math.min(BASE_SPEED, velocityX));
        velocityY = Math.max(-BASE_SPEED, Math.min(BASE_SPEED, velocityY));
    }
    
    private boolean isPathBlocked(Player player, java.util.ArrayList<Obstacle> obstacles) {
        // Create a line from enemy to player
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check several points along the path
        int checkPoints = 10;
        for (int i = 1; i < checkPoints; i++) {
            double checkX = x + (dx * i) / checkPoints;
            double checkY = y + (dy * i) / checkPoints;
            
            java.awt.Rectangle checkBounds = new java.awt.Rectangle(
                (int)checkX,
                (int)checkY,
                type == EnemyType.HUNTER_BOT ? SIZE + 10 : SIZE,
                SIZE
            );
            
            for (Obstacle obstacle : obstacles) {
                if (obstacle.collidesWithRect(checkBounds)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void render(java.awt.Graphics2D g2d) {
        switch (type) {
            case SECURITY_DRONE -> {
                g2d.setColor(java.awt.Color.RED);
                g2d.fillOval((int)x, (int)y, SIZE, SIZE);
            }
            case HUNTER_BOT -> {
                g2d.setColor(java.awt.Color.ORANGE);
                g2d.fillRect((int)x, (int)y, SIZE + 10, SIZE);
            }
            case TURRET -> {
                g2d.setColor(java.awt.Color.YELLOW);
                g2d.fillRect((int)x, (int)y, SIZE, SIZE);
            }
        }
    }
    
    public boolean collidesWith(Player player) {
        return new java.awt.Rectangle(
            (int)x, 
            (int)y, 
            type == EnemyType.HUNTER_BOT ? SIZE + 10 : SIZE, 
            SIZE
        ).intersects(player.getBounds());
    }
    
    public boolean isOffScreen() {
        return x + SIZE < 0 || x > GamePanel.getPanelWidth() ||
               y + SIZE < 0 || y > GamePanel.getPanelHeight();
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public EnemyType getType() { return type; }
}
