public class Player {
    private static final int SIZE = 30;
    private static final double JUMP_VELOCITY = -8.0;
    private static final double GRAVITY = 0.4;
    private static final double MAX_FALL_SPEED = 10.0;
    private static final int MAX_HEALTH = 3;
    private static final int INVULNERABILITY_FRAMES = 120; // 2 seconds at 60 FPS
    
    private double x, y;
    private double velocityY;
    private int health;
    private int invulnerabilityTimer;
    private boolean hasRocketBoost;
    private int rocketBoostTimer;
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityY = 0;
        this.health = MAX_HEALTH;
        this.invulnerabilityTimer = 0;
        this.hasRocketBoost = false;
        this.rocketBoostTimer = 0;
    }
    
    public void update() {
        // Update position
        velocityY = Math.min(velocityY + GRAVITY, MAX_FALL_SPEED);
        y += velocityY;
        
        // Keep player in bounds
        if (y <= 0) {
            y = 0;
            velocityY = 0;
        } else if (y >= GamePanel.getPanelHeight() - SIZE) {
            y = GamePanel.getPanelHeight() - SIZE;
            velocityY = 0;
        }
        
        // Update invulnerability
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer--;
        }
        
        // Update rocket boost
        if (hasRocketBoost) {
            rocketBoostTimer++;
            if (rocketBoostTimer >= 300) { // 5 seconds at 60 FPS
                hasRocketBoost = false;
                rocketBoostTimer = 0;
            }
        }
    }
    
    public void jump() {
        velocityY = JUMP_VELOCITY;
    }
    
    public void takeDamage() {
        if (!isInvulnerable()) {
            health--;
            invulnerabilityTimer = INVULNERABILITY_FRAMES;
            SoundManager.playSound("hit");
        }
    }
    
    public void activateRocketBoost() {
        hasRocketBoost = true;
        rocketBoostTimer = 0;
    }
    
    public void restoreHealth() {
        health = MAX_HEALTH;
    }
    
    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }
    
    public void render(java.awt.Graphics2D g2d) {
        // Flash when invulnerable
        if (!isInvulnerable() || invulnerabilityTimer % 10 < 5) {
            if (hasRocketBoost) {
                g2d.setColor(java.awt.Color.ORANGE);
            } else {
                g2d.setColor(java.awt.Color.BLUE);
            }
            g2d.fillRect((int)x, (int)y, SIZE, SIZE);
        }
    }
    
    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle((int)x, (int)y, SIZE, SIZE);
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocityY() { return velocityY; }
    public int getHealth() { return health; }
    public boolean hasRocketBoost() { return hasRocketBoost; }
}
