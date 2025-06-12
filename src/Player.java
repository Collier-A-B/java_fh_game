public class Player {
    private double x, y;
    private double velocityY;
    private int health;
    private boolean hasBoost;
    private int boostTimer;
    private static final double THRUST_POWER = -6.0;  // Reduced from -8.0 for better control
    private static final double GRAVITY = 0.3;        // Reduced from 0.4 for more float time
    private static final double DAMPENING = 0.98;     // Added to smooth out motion
    private static final int MAX_HEALTH = 3;
    private static final int BOOST_DURATION = 300;    // frames (5 seconds at 60 FPS)

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocityY = 0;
        this.health = MAX_HEALTH;
        this.hasBoost = false;
        this.boostTimer = 0;
    }

    public void update() {
        if (hasBoost) {
            boostTimer--;
            if (boostTimer <= 0) {
                hasBoost = false;
            }
        }

        // Apply gravity and dampening
        velocityY += GRAVITY;
        velocityY *= DAMPENING;  // Apply dampening to smooth out motion
        
        // Update position
        y += velocityY;

        // Limit maximum falling speed
        if (velocityY > 7) {     // Further reduced for better control
            velocityY = 7;
        }
        
        // Handle boundaries with bounce effect
        if (y < 0) {
            y = 0;
            velocityY = Math.abs(velocityY) * 0.5; // Bounce with reduced velocity
        } else if (y > GamePanel.getPanelHeight()) {
            y = GamePanel.getPanelHeight();
            velocityY = -Math.abs(velocityY) * 0.5; // Bounce with reduced velocity
        }
    }

    public void thrust() {
        // If falling fast, provide a stronger thrust for better recovery
        if (velocityY > 4) {
            velocityY = hasBoost ? THRUST_POWER * 1.8 : THRUST_POWER * 1.3;
        } else {
            velocityY = hasBoost ? THRUST_POWER * 1.5 : THRUST_POWER;
        }
    }

    public void activateBoost() {
        hasBoost = true;
        boostTimer = BOOST_DURATION;
    }

    public void takeDamage() {
        if (!hasBoost) { // Immune during boost
            health--;
        }
    }

    public void heal() {
        if (health < MAX_HEALTH) {
            health++;
        }
    }

    public void render(java.awt.Graphics2D g2d) {
        int bikeWidth = 40;
        int bikeHeight = 20;
        
        // Main bike body
        g2d.setColor(hasBoost ? java.awt.Color.CYAN : java.awt.Color.WHITE);
        g2d.fillRect((int)x, (int)y, bikeWidth, bikeHeight);
        
        // Glow effect
        if (hasBoost) {
            java.awt.AlphaComposite alphaComposite = java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 0.3f);
            g2d.setComposite(alphaComposite);
            g2d.setColor(java.awt.Color.CYAN);
            g2d.fillRect((int)x - 2, (int)y - 2, bikeWidth + 4, bikeHeight + 4);
            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
        }
        
        // Thruster effect
        if (velocityY < 0) {
            g2d.setColor(java.awt.Color.ORANGE);
            g2d.fillRect((int)x - 10, (int)y + 5, 10, 10);
        }
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getHealth() { return health; }
    public boolean hasBoost() { return hasBoost; }
    public double getVelocityY() {
        return velocityY;
    }

    // Collision detection
    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle((int)x, (int)y, 40, 20);
    }
}
