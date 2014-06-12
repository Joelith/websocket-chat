package chatws;

import java.awt.Color;

import java.util.Random;

public class User {
    
    private String username;
    private String colour;
    private Integer id;
    
    public User(Integer id) {
        this.id = id;
        this.colour = this.getRandomHexColour();
    }
    
    public void setUsername (String username) {
        this.username = username;
    }

    public String getUsername () {
        return username;
    }
    
    public String getColour () {
        return colour;
    }
    
    public static String getRandomHexColour() {
        Random random = new Random();
        float hue = random.nextFloat();
        // sat between 0.1 and 0.9
        float saturation = (random.nextInt(2000) + 7000) / 10000f;
        float luminance = 0.9f;
        Color colour = Color.getHSBColor(hue, saturation, luminance);
        return '#' + Integer.toHexString((colour.getRGB() & 0xffffff) | 0x1000000).substring(1);
    }
}
