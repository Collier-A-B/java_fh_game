public class PowerUp {
    private double x, y;
    private PowerUpType type;
    private static final int SIZE = 20;
    private static final double SPEED = 3.0;
    private double glowEffect = 0;
    private boolean increasing = true;

    public enum PowerUpType {
        ROCKET_BOOST,
        DATA_PACK
    }

    public PowerUp(int startX, int startY, PowerUpType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
    }

    public void update() {
        x -= SPEED;
        
        // Update glow effect
        if (increasing) {
            glowEffect += 0.05;
            if (glowEffect >= 1.0) {
                increasing = false;
            }
        } else {
            glowEffect -= 0.05;
            if (glowEffect <= 0.0) {
                increasing = true;
            }
        }
    }

    public void render(java.awt.Graphics2D g2d) {
        // Draw the power-up with a glowing effect
        if (type == PowerUpType.ROCKET_BOOST) {
            // Rocket boost (cyan)
            g2d.setColor(java.awt.Color.CYAN);
        } else {
            // Data pack (green)
            g2d.setColor(new java.awt.Color(0, 255, 128));
        }

        // Draw the main shape
        g2d.fillRect((int)x, (int)y, SIZE, SIZE);

        // Draw the glow effect
        float alpha = Math.max(0.0f, Math.min(1.0f, (float)(0.3 * glowEffect)));
        java.awt.AlphaComposite alphaComposite = java.awt.AlphaComposite.getInstance(
            java.awt.AlphaComposite.SRC_OVER, 
            alpha
        );
        g2d.setComposite(alphaComposite);
        g2d.fillRect((int)x - 5, (int)y - 5, SIZE + 10, SIZE + 10);
        g2d.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    public boolean isOffScreen() {
        return x + SIZE < 0;
    }

    public boolean collidesWith(Player player) {
        java.awt.Rectangle powerUpBounds = new java.awt.Rectangle((int)x, (int)y, SIZE, SIZE);
        return powerUpBounds.intersects(player.getBounds());
    }

    public PowerUpType getType() {
        return type;
    }
}
