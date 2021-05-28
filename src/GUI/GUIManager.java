/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.awt.AlphaComposite;
import java.awt.CardLayout;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.jafama.FastMath;

/**
 *
 * @author s-hardy
 */
public final class GUIManager extends JFrame{
    //panels
    private static CardLayout layoutController;
    private static JPanel mainPanel;
    private static String currentPanel;
    
    private static MainMenuPanel mainMenuPanel;
    private static SimulationPanel simulationPanel;
    
    //visuals and sprites
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final File IMAGE_DIRECTORY = new File("assets");
    private static final File FONT_DIRECTORY = new File("fonts\\Russo_One.ttf");
    private static HashMap<String, BufferedImage> images;
    private static HashMap<String, Color> colourScheme;
    
    //custom variables
    private static String font;
    private static DecimalFormat df;
    
    private static AlphaComposite alphaComposite, normalComposite;
        

    //initialisation
    public GUIManager(){
        super("HyperLife");
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUIManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
        //define variables
        alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , 0.5f);
        normalComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , 1f);
        df = new DecimalFormat("0.00");
        try {
            GraphicsEnvironment ge = 
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, FONT_DIRECTORY));
            font = "Russo One";
        } catch (IOException|FontFormatException e) {
            System.out.println("Please install the required fonts folder to run this program");
        }
        
        //load sprites
        loadColourScheme();
        loadImages();
        createPanels();
        setFrameProperties();
    }
    
    public void createPanels(){ 
        mainMenuPanel = new MainMenuPanel(this);
        
        simulationPanel = new SimulationPanel(this);
        
        layoutController = new CardLayout();
        mainPanel = new JPanel(layoutController);
        
        //This componentListener allows the panel to
        //dynamically resize every widget when the frame changes shape
        mainPanel.addComponentListener(new ComponentAdapter() {  
            @Override
            public void componentResized(ComponentEvent evt) {
                switch (currentPanel) {
                    case "mainMenuPanel":
                        mainMenuPanel.revalidate();
                        mainMenuPanel.repaint();
                        break;
                    case "simulationPanel":
                        simulationPanel.revalidate();
                        simulationPanel.repaint();
                        break;
                    default:
                        break;
                }
            }
        });
        //add the panels to the cardlayout
        layoutController.addLayoutComponent(mainMenuPanel, "mainMenuPanel");
        mainPanel.add(mainMenuPanel);
        layoutController.addLayoutComponent(simulationPanel, "simulationPanel");  
        mainPanel.add(simulationPanel);
        //add the cardlayout panel to the main frame
        add(mainPanel);
    }
    
    public void setFrameProperties(){
        setCurrentPanel("mainMenuPanel");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setSize((int)screenSize.getWidth(), (int)screenSize.getHeight());
        setBackground(new Color(16, 29, 48));
        setVisible(true); 
    }
    
    public static void setComponentProperties(JComponent comp, int size){
        comp.setFont(new Font(getDefaultFont(), 1, size));
        comp.setForeground(WHITE);
    }
    
    //image/colour manipulation
    public static Color brightness(Color c, double i){
        int R = (int) FastMath.max(FastMath.min(c.getRed()*i,255),0);
        int G = (int) FastMath.max(FastMath.min(c.getGreen()*i,255),0);
        int B = (int) FastMath.max(FastMath.min(c.getBlue()*i,255),0);
        return new Color(R,G,B,c.getAlpha());
    }
    
    public static Color interpolate(Color x, Color y, double blending){
        double inverse_blending = 1 - blending;
        double red =   x.getRed()   * inverse_blending   +   y.getRed()   * blending;
        double green = x.getGreen() * inverse_blending   +   y.getGreen() * blending;
        double blue =  x.getBlue()  * inverse_blending   +   y.getBlue()  * blending;
        double alpha =  x.getAlpha()  * inverse_blending   +   y.getAlpha()  * blending;
        Color blended = new Color((int)red, (int)green, (int)blue, (int)alpha);
        return blended;
    }
    
    public static Color addAlpha(Color c, int a){
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
    
    public static BufferedImage resize(BufferedImage img, double newW, double newH) {
        BufferedImage after = new BufferedImage((int)newW, (int)newH, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(newW/img.getWidth(), newH/img.getHeight());
        AffineTransformOp scaleOp = 
           new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        after = scaleOp.filter(img, after);
        return after;
    }
    
    public static BufferedImage rotate(BufferedImage img, double rads){
        final double sin = FastMath.abs(FastMath.sin(rads));
        final double cos = FastMath.abs(FastMath.cos(rads));
        final int w = (int) FastMath.floor(img.getWidth() * cos + img.getHeight() * sin);
        final int h = (int) FastMath.floor(img.getHeight() * cos + img.getWidth() * sin);
        final BufferedImage rotatedImage = new BufferedImage(w, h, img.getType());
        final AffineTransform at = new AffineTransform();
        at.translate(w / 2, h / 2);
        at.rotate(rads, 0, 0);
        at.translate(-img.getWidth() / 2, -img.getHeight() / 2);
        final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        rotateOp.filter(img, rotatedImage);
        return rotatedImage;
    }
    
    public static BufferedImage tintImage(BufferedImage image, int red, int green, int blue, int alpha) {
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics(); 
        
        int rule = AlphaComposite.SRC;
        alphaComposite = AlphaComposite.getInstance(rule, 1.0f);
        g.setComposite(alphaComposite);
        g.drawImage(image, 0, 0, null);
        
        Color newColor = new Color(FastMath.min(FastMath.max(red,0),255),
                FastMath.min(FastMath.max(green,0),255),
                FastMath.min(FastMath.max(blue,0),255), alpha);
        rule = AlphaComposite.SRC_ATOP;
        alphaComposite = AlphaComposite.getInstance(rule, 1.0f);
        g.setComposite(alphaComposite);
        g.setColor(newColor);
        g.fillRect(0,0,img.getWidth(),img.getHeight());
        g.dispose();
        return img;
    }
    
    public static BufferedImage flipHorizontally(BufferedImage image){
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(), 0);
        AffineTransformOp  op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = op.filter(image, null);
        return image;
    }
    
    public static void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font f) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(f);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(f);
        // Draw the String
        g.drawString(text, x, y);
        g.setFont(new Font(getDefaultFont(), Font.PLAIN, 16)); 
    }
    
    
    //getter methods
    public static Dimension getScreenSize(){
        return screenSize;
    }
    
    public static SimulationPanel getSimulationPanel(){
        return (SimulationPanel) simulationPanel;
    }
    
    public static AlphaComposite getAlphaComposite(){
        return alphaComposite;
    }
    
    public static AlphaComposite getNormalComposite(){
        return normalComposite;
    }
    
    public static void loadImages(){
        images = new HashMap<>();
        loadImagesFromDirectory(IMAGE_DIRECTORY);
    }
    
    public static void loadImagesFromDirectory(File directory){
        try {
            for (File file : directory.listFiles()){
                //create a variable to store the sprite name
                if (file.isDirectory() ) {
                    loadImagesFromDirectory(file);
                }else{
                    String name = file.getName();
                    name = name.substring(0, name.lastIndexOf('.'));
                    BufferedImage image = ImageIO.read(file);
                    //add name image pair to the images hashmap
                    images.put(name, image);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GUIManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void loadColourScheme(){
        colourScheme = new HashMap<>();
        colourScheme.put("default", new Color(255,0,230));
        colourScheme.put("background", new Color(0,0,0));
        colourScheme.put("button", new Color(6, 65, 66));
        colourScheme.put("menuBackground", new Color(6, 65, 66));
        colourScheme.put("menuBackground2", new Color(24, 99, 80));
        colourScheme.put("menuBackground3", new Color(24, 77, 99));
        colourScheme.put("menuBackground4", new Color(58, 107, 16));
        colourScheme.put("menuBackground5", new Color(102, 28, 34));
        
        colourScheme.put("red", new Color(204, 54, 43));
        colourScheme.put("green", new Color(23, 105, 27));
    }
    
    public static String format(double number){
        return df.format(number);
    }
    
    public static BufferedImage getImage(String imageName){
        if (images.containsKey(imageName)) {
            return images.get(imageName);
        }
        return images.get("DefaultTexture");
    }
    
    public static Color getColour(String colourName){
        if (colourScheme.containsKey(colourName)) {
            return colourScheme.get(colourName);
        }
        return colourScheme.get("default");
    }
    
    public static String getDefaultFont(){
        return font;
    }
    
    public static DecimalFormat getDecimalFormat(){
        return df;
    }
    
    //setter methods
    public static void setCurrentPanel(String panel){
        currentPanel = panel;
        layoutController.show(mainPanel, panel);
    }
    
}

