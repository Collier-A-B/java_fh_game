public class Enemy {
    private double x, y;
    private double velocityX, velocityY;
    private EnemyType type;
    private static final int SIZE = 30;
    private static final double BASE_SPEED = 3.0;
    
    public enum EnemyType {
        SECURITY_DRONE,
        HUNTER_BOT,
        TURRET
    }

    public Enemy(int startX, int startY, EnemyType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        
        switch (type) {
            case SECURITY_DRONE:
                velocityX = -BASE_SPEED;
                velocityY = Math.random() * 2 - 1; // Random up/down movement
                break;
            case HUNTER_BOT:
                velocityX = -BASE_SPEED * 0.75;
                velocityY = 0; // Will be updated to move towards player
                break;
            case TURRET:
                velocityX = -BASE_SPEED;
                velocityY = 0;
                break;
        }
    }

    public void update(Player player) {
        x += velocityX;
        
        switch (type) {
            case SECURITY_DRONE:
                y += velocityY;
                // Bounce off screen edges
                if (y <= 0 || y >= GamePanel.getPanelHeight() - SIZE) {
                    velocityY *= -1;
                }
                break;
            case HUNTER_BOT:
                // Move towards player
                double dy = player.getY() - y;
                velocityY = Math.signum(dy) * BASE_SPEED * 0.5;
                y += velocityY;
                break;
            case TURRET:
                // Turrets just move left with the scene
                break;
        }
    }

    public void render(java.awt.Graphics2D g2d) {
        switch (type) {
            case SECURITY_DRONE:
                g2d.setColor(java.awt.Color.RED);
                g2d.fillRect((int)x, (int)y, SIZE, SIZE);
                break;
            case HUNTER_BOT:
                g2d.setColor(java.awt.Color.ORANGE);
                g2d.fillRect((int)x, (int)y, SIZE + 10, SIZE);
                break;
            case TURRET:
                g2d.setColor(java.awt.Color.YELLOW);
                // Draw turret base
                g2d.fillRect((int)x, (int)y, SIZE, SIZE);
                // Draw turret cannon
                g2d.fillRect((int)x - 15, (int)y + SIZE/3, 15, SIZE/3);
                break;
        }
    }

    public boolean isOffScreen() {
        return x + SIZE < 0;
    }

    public boolean collidesWith(Player player) {
        java.awt.Rectangle enemyBounds = new java.awt.Rectangle((int)x, (int)y, SIZE, SIZE);
        return enemyBounds.intersects(player.getBounds());
    }
}
