/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import CellParts.SegmentInstance;
import File.FileManager;
import static GUI.GUIManager.format;
import static GUI.GUIManager.getColour;
import static GUI.GUIManager.getDefaultFont;
import static GUI.GUIManager.getImage;
import static GUI.GUIManager.getScreenSize;
import static GUI.GUIManager.interpolate;
import static GUI.GUIManager.setCurrentPanel;
import GeneticAlgorithm.Creature;
import static HyperLife.HyperLife.getGA;
import static HyperLife.HyperLife.getRand;
import Physics.NoiseGenerator;
import Physics.Particle;
import static Physics.PhysicsManager.inversePositions;
import static Physics.PhysicsManager.newPositions;
import static Physics.PhysicsManager.pointIntersectsSegment;
import static Physics.PhysicsManager.interpolate;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import QuadTree.QuadTree;
import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class SimulationPanel extends JPanel implements MouseListener, MouseWheelListener{
    //parent/widgets
    private final GUIManager parent;
    private final SimulationToolbar simulationToolbar;
    
    //file manager
    private final FileManager fileManager;
    
    //global variables
    public static double DEFAULT_FRICTION = 0.99;
    public static double FRICTION = 0.99;
    public static double DEFAULT_GRAVITY = 0.00005;
    public static double GRAVITY = 0.00005;
    
    //UI boolean
    private final ButtonVariable home = new ButtonVariable(false),
            showUI = new ButtonVariable(true),
            showInfo = new ButtonVariable(false),
            showQuadTree = new ButtonVariable(false),
            viewMode = new ButtonVariable(false),
            slowDown = new ButtonVariable(false),
            paused = new ButtonVariable(false),
            speedUp = new ButtonVariable(false),
            reset = new ButtonVariable(false),
            randomCreature = new ButtonVariable(false),
            killRandom = new ButtonVariable(false),
            killHalf = new ButtonVariable(false),
            selectRandom = new ButtonVariable(false),
            saveFile = new ButtonVariable(false),
            loadFile = new ButtonVariable(false),
            deleteFile = new ButtonVariable(false),
            simulation = new ButtonVariable(false),
            creature = new ButtonVariable(false),
            importFile = new ButtonVariable(false),
            xSizeUpdate = new ButtonVariable(false),
            ySizeUpdate = new ButtonVariable(false),
            dragUpdate = new ButtonVariable(false),
            maxPopulationUpdate = new ButtonVariable(false),
            killCreature = new ButtonVariable(false),
            cloneCreature = new ButtonVariable(false),
            mutateCreature = new ButtonVariable(false),
            addEnergy = new ButtonVariable(false),
            followCreature = new ButtonVariable(false),
            detailMode = new ButtonVariable(false);
    
    //simulation map
    private QuadTree quadTree;
    private final Rectangle screenRect = new Rectangle(0,0, 
            (int)getScreenSize().getWidth(), (int)getScreenSize().getHeight());
    private final int maxDepth = 6, capacity = 4;
    private int xSize = 20, ySize = 20;
    private double cameraX = 0, cameraY = 0, scale = 50, cameraMoveSpeed = 5;
    private double lastCameraX = 0, lastCameraY = 0, lastScale = 50;
    private boolean up, down, left, right;
    private HashMap<String, BufferedImage> textures = new HashMap<>();    
    private String[][] terrain = new String[xSize][ySize];
    private double seed = getRand().nextDouble() + getRand().nextInt(100000);
    private double waterZ = 0, waterY = 0;
    //particles
    private ArrayList<Particle> particles = new ArrayList<>();
    
    //time keeping/speed
    private int globalStep = 0;
    private double step = 0;
    private double speed = 1;
    private long lastFPSUpdate, lastTime, currentTime;
    private double fps = 400;
    private double decaySpeed, growthSpeed, efficiencyDecrease;
    
    //selection/mouseinfo
    private double mappedMouseX, mappedMouseY, mouseX, mouseY;
    private Queue<Double[]> mouseData = new LinkedList<>();
    private boolean mousePressed = false;
    private SegmentInstance selectedSegment = null;
    
    public SimulationPanel(GUIManager parent){
        this.parent = parent;
        fileManager = new FileManager(this, "Untitled");
        simulationToolbar = new SimulationToolbar(this);
        generateTerrain();
        resetQuadTree();
        setBindings();
        addMouseListener();
        addMouseWheelListener();
    }
    
    public final void generateTerrain(){
        terrain = new String[xSize][ySize];
        if(textures.isEmpty()){
            String[] textureData = {"Rock", "Rock2", "Rock3","Rock4", "MossyRock"};
            for(String texture : textureData){
                textures.put(texture, getImage(texture));
            }
        }
        for(int x = -xSize/2; x < xSize/2; x++){
            for(int y = -ySize/2; y < ySize/2; y++){
                double firstOctave = NoiseGenerator.noise((double)x/(25), (double)y/(25), seed);
                double secondOctave = NoiseGenerator.noise((double)x/(12.5), (double)y/(12.5), seed-100);
                double thirdOctave = NoiseGenerator.noise((double)x/(6.25), (double)y/(6.25), seed-200);
                double fourthOctave = NoiseGenerator.noise((double)x/(3.125), (double)y/(3.125), seed-300);
                double finalNoise = firstOctave + secondOctave*0.5 + thirdOctave*0.25 + fourthOctave;
                finalNoise = FastMath.min(FastMath.max(finalNoise, -0.5), 0.5);
                String quantisedResult = "Rock";
                if(finalNoise < - 0.3){
                    quantisedResult = "MossyRock";
                }if(finalNoise >= 0.0){
                    quantisedResult = "Rock2";
                }if(finalNoise >= 0.3){
                    quantisedResult = "Rock3";
                }
                terrain[x+xSize/2][y+ySize/2] = quantisedResult;
            }
        }
        /*for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                if(terrain[x][y].equals("Rock")){ //mossify the stones
                    double result = NoiseGenerator.noise((double)x/(xSize*0.25), (double)y/(ySize*0.25), z+100);
                    String quantisedResult = "Rock";
                    if(result >= 0.2){
                        quantisedResult = "MossyRock";
                    }
                    terrain[x][y] = quantisedResult;
                }
            }
        }*/
        
        resizeTextures();
    }
    
    public final void setBindings(){
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
          
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "UP1");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "DOWN1");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "LEFT1");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "RIGHT1");
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "UP2");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "DOWN2");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "LEFT2");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "RIGHT2");
        
        am.put("UP1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               up = true;
            }   
        });
        am.put("DOWN1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               down = true;
            }   
        });
        am.put("LEFT1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               left = true;
            }   
        });
        am.put("RIGHT1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               right = true;
            }   
        });
        am.put("UP2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               up = false;
            }   
        });
        am.put("DOWN2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               down = false;
            }   
        });
        am.put("LEFT2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               left = false;
            }   
        });
        am.put("RIGHT2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               right = false;
            }   
        });
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "addEnergy");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false), "clone");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, false), "mutate");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), "kill");
        
        am.put("addEnergy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               addEnergy.setValue(true);
            }   
        });
        am.put("clone", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               cloneCreature.setValue(true);
            }   
        });
        am.put("mutate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               mutateCreature.setValue(true);
            }   
        });
        am.put("kill", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               killCreature.setValue(true);
            }   
        });
    }
    
    public final void addMouseWheelListener(){
        addMouseWheelListener(this);
    }
    public final void addMouseListener(){
        addMouseListener(this);
    }
    
    //drawing methods
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        //time methods
        lastTime = currentTime;
        currentTime = System.nanoTime();
        globalStep += 1;
        if(!paused.getValue() && lastTime != 0.0){
            double timeSeconds = (currentTime - lastTime)/ 1000000.0;
            step += timeSeconds*speed;
        }
        cameraMoveSpeed = 5.0 * (400.0/(FastMath.max(fps, 1)));
        //graphics setup
        System.setProperty("sun.java2d.opengl", "true");
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, 
                RenderingHints.VALUE_DITHER_DISABLE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2.setFont(new Font(getDefaultFont(),1,15));
        drawBackground(g2);
        calculateCameraMovement();
        calculateMouseMovement(g2);
        drawGrid(g2);
        if(showQuadTree.getValue()){
            quadTree.draw(g2);
        }
        
        //calculation methods
        updateFPS();
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.simulate(g2);
            if(p.getLifeTime() <= 0){
                iterator.remove();
            }
        }
        getGA().simulate(g2);
        quadTree.recalibrate();
        drawWater(g2);
        
        
        //drawing methods
        drawMenu(g2);
        drawData(g2);
        
        revalidate();
        repaint();
    }
    
    public void drawBackground(Graphics2D g){
        int width = (int) getScreenSize().getWidth();
        int height = (int) getScreenSize().getHeight();
        g.setColor(getColour("background"));
        g.fillRect(0,0, width, height);
        if(!detailMode.getValue()){
            return;
        }
        double[] topLeftBound = inversePositions(0, 0, cameraX, cameraY, scale);
        double[] bottomRightBound = inversePositions(screenRect.getWidth(), screenRect.getHeight(), cameraX, cameraY, scale);
        double[] textureCoords = newPositions(-xSize/2, -ySize/2, cameraX, cameraY, scale);
        for(int x = -xSize/2; x < xSize/2; x++){
            if(x-1 < bottomRightBound[0] && x+1 > topLeftBound[0]){
                for(int y = -ySize/2; y < ySize/2; y++){
                    if(y-1 < bottomRightBound[1] && y+1 > topLeftBound[1]){
                        double[] newCoords = {textureCoords[0] + (x+xSize/2)*scale, textureCoords[1] + (y+ySize/2)*scale};
                        BufferedImage texture = getTextureAtPoint(x+xSize/2, y+ySize/2);
                        g.drawImage(texture, (int)newCoords[0], (int)newCoords[1], null);
                    }
                }
            }
        }
    }
    
    public void drawGrid(Graphics2D g){
        if(!detailMode.getValue()){
            int brightness = (int) FastMath.min(FastMath.max(30 + 0.5*scale,0), 255);
            g.setColor(new Color(brightness,brightness,brightness));
            for(int x = 1; x < xSize; x++){
                double[] startCoords = newPositions(x-xSize/2, -ySize/2, cameraX, cameraY, scale);
                double[] endCoords = new double[]{startCoords[0], startCoords[1] + ySize*scale};
                Line2D line = new Line2D.Double(startCoords[0],startCoords[1],endCoords[0],endCoords[1]);
                if(screenRect.intersectsLine(line)){
                    g.drawLine((int)startCoords[0], (int)startCoords[1],
                            (int)endCoords[0], (int)endCoords[1]);
                }
            }
            for(int y = 1; y < ySize; y++){
                double[] startCoords = newPositions(-xSize/2, y-ySize/2, cameraX, cameraY, scale);
                double[] endCoords = new double[]{startCoords[0] + xSize*scale, startCoords[1]};
                Line2D line = new Line2D.Double(startCoords[0],startCoords[1],endCoords[0],endCoords[1]);
                if(screenRect.intersectsLine(line)){
                    g.drawLine((int)startCoords[0], (int)startCoords[1],
                            (int)endCoords[0], (int)endCoords[1]);
                }
            }
        }
        //border
        double[] startCoords = newPositions(-xSize/2, -ySize/2, cameraX, cameraY, scale);
        double[] endCoords = newPositions(xSize/2, ySize/2, cameraX, cameraY, scale);
        g.setColor(WHITE);
        g.setStroke(new BasicStroke(FastMath.max((float)(0.05*scale),1f)));
        g.drawRoundRect((int)startCoords[0], (int)startCoords[1], (int)(endCoords[0]-startCoords[0]), (int)(endCoords[1]-startCoords[1]), (int)(0.25*scale), (int)(0.25*scale));
        g.setStroke(new BasicStroke(1));
        
    }
    
    public void drawWater(Graphics2D g){
        if(!detailMode.getValue()){
            return;
        }
        waterZ += 0.0005;//0.0005
        waterY -= 0.005;
        double resolution = FastMath.min(scale*0.1,2), hRes = resolution/2, recipRes = 1.0/resolution;
        /*g.fillRect((int)startCoords[0], (int)startCoords[1], (int)(endCoords[0]-startCoords[0]), (int)(endCoords[1]-startCoords[1]));*/
        double[] topLeftBound = inversePositions(0, 0, cameraX, cameraY, scale);
        double[] bottomRightBound = inversePositions(screenRect.getWidth(), screenRect.getHeight(), cameraX, cameraY, scale);
        double[] bottomRightMapBound = newPositions(xSize/2, ySize/2, cameraX, cameraY, scale);
        double[] textureCoords = newPositions(-xSize/2, -ySize/2, cameraX, cameraY, scale);
        int[] nextCoords, currentCoords;
        for(double y = 0; y < ySize*resolution; y++){
            if((y-ySize*hRes)/resolution-1 < bottomRightBound[1] && (y-ySize*hRes)/resolution+1 > topLeftBound[1]){
                for(double x = 0; x < xSize*resolution; x++){
                    if((x-xSize*hRes)/resolution-1 < bottomRightBound[0] && (x-xSize*hRes)/resolution+1 > topLeftBound[0]){
                        currentCoords  = new int[]{(int)(textureCoords[0] + x*scale*recipRes), (int)(textureCoords[1] + y*scale*recipRes)};
                        nextCoords = new int[]{(int)(textureCoords[0] + (x+1)*scale*recipRes), (int)(textureCoords[1] + (y+1)*scale*recipRes)};
                        double waterVal = NoiseGenerator.noise(x/(resolution*10), (y+waterY)/(resolution*2), waterZ);
                        waterVal += NoiseGenerator.noise(x/(resolution), (y+waterY)/(resolution*0.2), waterZ)*0.5;
                        double a = FastMath.min(FastMath.max(waterVal+0.5,0),1);
                        g.setColor(interpolate(new Color(40, 160, 255, 50), new Color(126, 198, 255, 100), a));
                        if(waterVal > 0.6){
                            a = FastMath.min(FastMath.max((waterVal-0.6)*3,0),1);
                            g.setColor(interpolate(new Color(126, 198, 255, 100), new Color(255, 255, 255, 255), a));
                        }
                        int xLen = nextCoords[0] - currentCoords[0];
                        int yLen = nextCoords[1] - currentCoords[1];
                        if(currentCoords[0] + xLen >= bottomRightMapBound[0]){
                            xLen = (int) (bottomRightMapBound[0] - currentCoords[0]);
                        }
                        if(currentCoords[1] + yLen >= bottomRightMapBound[1]){
                            yLen = (int) (bottomRightMapBound[1] - currentCoords[1]);
                        }
                        g.fillRect(currentCoords[0], currentCoords[1], xLen, yLen);
                    }
                }
            }
        }
    }
    
    public BufferedImage getTextureAtPoint(int x, int y){
        if(x < xSize && y < ySize){
            String blockID = terrain[x][y];
            return textures.get(blockID);
        }else{
            return getImage("DefaultTexture");
        }
    }
    
    public void resizeTextures(){
        for(Map.Entry<String, BufferedImage> entry : textures.entrySet()){
            BufferedImage image = getImage(entry.getKey());
            double w = FastMath.ceil(image.getWidth()*scale/25.0);
            double h = FastMath.ceil(image.getHeight()*scale/25.0);
            image = GUIManager.resize(image, w, h);
            textures.put(entry.getKey(), image);
        }
    }
    
    public void drawMenu(Graphics2D g){
        //drawing menu
        simulationToolbar.draw(g);
        processComponentUpdate(g);
    }
    
    public void drawData(Graphics2D g){
        String info = "<html>";
        long uptime = (long) step;
        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        uptime -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(uptime);
        uptime -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime);
        uptime -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime);
        uptime -= TimeUnit.SECONDS.toMillis(seconds);
        String time = "";
        if(days > 0){
            time += days + " days ";
        }
        time += hours+":"+minutes+":"+seconds;
        info += "Time: " + time + "<br>";
        info += "Speed: " + format(speed) + "<br>";
        info += "FPS: " + format(fps) + "<br>";
        info += "Creatures: " + getGA().getNumCreatures() + "<br>";
        info += "Segments: " + getGA().getSegments().size() + "<br>";
        info += "Total Species: " + getGA().getTotalSpecies() + "<br>";
        info += "Current Species: " + getGA().getCurrentSpecies() + "<br>";
        info += "</html>";
        ((JLabel)simulationToolbar.getComponent("infoLabel")).setText(info);
    }
      
    public void drawDataPlot(Graphics g, String name){
        
    }
    
    //proccess methods
    public void processComponentUpdate(Graphics2D g){
        if(slowDown.getValue()){
            changeSpeed(speed/1.5);
            slowDown.setValue(false);
        }
        if(speedUp.getValue()){
            changeSpeed(speed*1.5);
            speedUp.setValue(false);
        }
        if(reset.getValue()){
            reset();
            reset.setValue(false);
        }
        if(randomCreature.getValue()){
            getGA().createRandomCreature();
            randomCreature.setValue(false);
        }
        if(selectRandom.getValue() && getGA().getNumCreatures() != 0){
            selectedSegment = getGA().getRandomCreature().getHead();
            selectRandom.setValue(false);
        }
        if(killRandom.getValue()){
            getGA().killRandomCreature();
            killRandom.setValue(false);
        }
        if(killHalf.getValue()){
            getGA().killHalfCreatures();
            killHalf.setValue(false);
        }
        if(home.getValue()){
            setCurrentPanel("mainMenuPanel");
            home.setValue(false);
        }
        if(saveFile.getValue()){
            fileManager.saveFile();
            saveFile.setValue(false);
        }
        if(loadFile.getValue()){
            simulationToolbar.setCurrentTab("load");
            loadFile.setValue(false);
        }
        if(simulation.getValue()){
            simulationToolbar.setCurrentTab("simulation");
            simulation.setValue(false);
        }
        if(creature.getValue() && selectedSegment != null && selectedSegment.getCreature() != null){
            simulationToolbar.setCurrentTab("creature");
            creature.setValue(false);
        }
        if(deleteFile.getValue()){
            fileManager.deleteFile();
            deleteFile.setValue(false);
        }
        if(importFile.getValue()){
            fileManager.importFile();
            importFile.setValue(false);
        }
        if(xSizeUpdate.getValue() || ySizeUpdate.getValue()){
            xSize = (int)(((JSlider)simulationToolbar.getComponent("xSizeSlider")).getValue()/2)*2;
            ySize = (int)(((JSlider)simulationToolbar.getComponent("ySizeSlider")).getValue()/2)*2;
            ((JLabel)simulationToolbar.getComponent("xSizeLabel")).setText("Width: " + xSize);
            ((JLabel)simulationToolbar.getComponent("ySizeLabel")).setText("Height: " + ySize);
            resetQuadTree();
            generateTerrain();
            xSizeUpdate.setValue(false);
            ySizeUpdate.setValue(false);
        }
        if(dragUpdate.getValue()){
            DEFAULT_FRICTION = 1.0 - (double)((JSlider)simulationToolbar.getComponent("dragSlider")).getValue()/1000;
            FRICTION = FastMath.pow(DEFAULT_FRICTION, speed);
            dragUpdate.setValue(false);
        }
        if(maxPopulationUpdate.getValue()){
            JLabel label = ((JLabel)simulationToolbar.getComponent("maxPopulationLabel"));
            JSlider slider = ((JSlider)simulationToolbar.getComponent("maxPopulationSlider"));
            int maxCreatures = (int) FastMath.pow(slider.getValue(),2);
            if(maxCreatures == slider.getMaximum()){
                maxCreatures = -1;
                label.setText("<html> Max Pop: <font face = \"Bedrock\" size = \"5\">∞</font></html>");
            }else{
                label.setText("Max Pop: " + maxCreatures);
            }
            getGA().setMaxCreatures(maxCreatures);
            maxPopulationUpdate.setValue(false);
        }
        if(cloneCreature.getValue() && selectedSegment != null && selectedSegment.getCreature() != null){
            double randomX = selectedSegment.getX() + (getRand().nextDouble()-0.5)*0.5;
            double randomY = selectedSegment.getY() + (getRand().nextDouble()-0.5)*0.5;
            double randomAngle = getRand().nextDouble()*FastMath.PI*2.0;
            getGA().cloneCreature(selectedSegment.getCreature(), randomX, randomY, randomAngle, false);
            cloneCreature.setValue(false);
        }
        if(mutateCreature.getValue() && selectedSegment != null && selectedSegment.getCreature() != null){
            getGA().mutateCreature(selectedSegment.getCreature());
            mutateCreature.setValue(false);
        }
        if(killCreature.getValue() && selectedSegment != null && selectedSegment.getCreature() != null){
            getGA().removeCreature(selectedSegment.getCreature());
            getGA().killCreature(selectedSegment.getCreature());
            killCreature.setValue(false);
        }
        if(addEnergy.getValue() && selectedSegment != null && selectedSegment.getCreature() != null){
            if(selectedSegment != null && selectedSegment.getCreature() != null){
                selectedSegment.getCreature().addEnergy(10.0);
            }
            addEnergy.setValue(false);
        }
    }
    
    public void changeSpeed(double newSpeed){
        speed = newSpeed;
        FRICTION = FastMath.pow(DEFAULT_FRICTION, speed);
        GRAVITY = DEFAULT_GRAVITY*speed;
        decaySpeed = FastMath.pow(0.9996,speed);
        growthSpeed = FastMath.pow(1.001,speed);
        efficiencyDecrease = 0.00001;//FastMath.pow(0.9999985,speed);
    }
    
    public void updateFPS(){
        if(globalStep % 20 == 0){
            fps = Double.parseDouble(format(1000000000.0 / (System.nanoTime() - lastFPSUpdate)*20));
            lastFPSUpdate = System.nanoTime();
        }
    }
    
    public void calculateCameraMovement(){
        lastCameraX = cameraX;
        lastCameraY = cameraY;
        if(!(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextField)){
            if(selectedSegment != null && selectedSegment.getCreature() != null && followCreature.getValue()){
                cameraX = selectedSegment.getX();
                cameraY = selectedSegment.getY();
                if(up){
                    double velChange = 0.0001;
                    double xChange = FastMath.cos(selectedSegment.getRealAngle())*velChange;
                    double yChange = FastMath.sin(selectedSegment.getRealAngle())*velChange;
                    selectedSegment.addX(xChange);
                    selectedSegment.addY(yChange);
                }if(down){
                    double velChange = -0.0001;
                    double xChange = FastMath.cos(selectedSegment.getRealAngle())*velChange;
                    double yChange = FastMath.sin(selectedSegment.getRealAngle())*velChange;
                    selectedSegment.addX(xChange);
                    selectedSegment.addY(yChange);
                }if(left){
                    selectedSegment.addAngle(0.0002);
                }if(right){
                    selectedSegment.addAngle(-0.0002);
                }
            }else{
                if(up){
                    if(cameraY > -(ySize/2)){
                        cameraY -= cameraMoveSpeed/scale;
                    }
                }if(down){
                    if(cameraY < (ySize/2)){
                        cameraY += cameraMoveSpeed/scale;
                    }
                }if(left){
                    if(cameraX > -(xSize/2)){
                        cameraX -= cameraMoveSpeed/scale;
                    }
                }if(right){
                    if(cameraX < (xSize/2)){
                        cameraX += cameraMoveSpeed/scale;
                    }
                }
                if(cameraY < -ySize/2) cameraY = -ySize/2;
                if(cameraY > ySize/2) cameraY = ySize/2;
                if(cameraX < -xSize/2) cameraX = -xSize/2;
                if(cameraX > xSize/2) cameraX = xSize/2;
            }
        }
        
    }
    
    public void calculateMouseMovement(Graphics2D g){
        mouseX = MouseInfo.getPointerInfo().getLocation().getX();
        mouseY = MouseInfo.getPointerInfo().getLocation().getY();
        double[] mouseCoords = inversePositions(mouseX, mouseY, cameraX, cameraY, scale);
        mappedMouseX = mouseCoords[0];
        mappedMouseY = mouseCoords[1];
        if(selectedSegment != null){
            if(selectedSegment.isDead()){
                selectedSegment = null;
            }else{
                if(!followCreature.getValue() && mousePressed && !simulationToolbar.isMouseOver((int)mouseX, (int)mouseY)){
                    double lastX = 0, lastY = 0;
                    for(Double[] data : mouseData){
                        lastX += data[0];
                        lastY += data[1];
                    }
                    lastX /= mouseData.size();
                    lastY /= mouseData.size();
                    double a = -FastMath.atan2(mappedMouseY - lastY, mappedMouseX - lastX);
                    double dist = FastMath.hypot(mappedMouseY - lastY, mappedMouseX - lastX);
                    if(mappedMouseX != lastX && mappedMouseY != lastY && dist > 0.05){
                        double b = -selectedSegment.getRealAngle() % (FastMath.PI*2);
                        double dtheta = (a - b);
                        if (dtheta > Math.PI) b += 2*Math.PI;
                        else if (dtheta < -Math.PI) b -= 2*Math.PI;
                        double targetVel = (a - b)*0.0005;
                        selectedSegment.addAngle(targetVel);
                    }
                    if(!paused.getValue()){
                        double posChangePercent = 0.2;
                        double newX = (mappedMouseX - selectedSegment.getX())*posChangePercent;
                        double newY = (mappedMouseY - selectedSegment.getY())*posChangePercent;
                        selectedSegment.addX(newX);
                        selectedSegment.addY(newY); 
                    }else{
                        selectedSegment.setXPos(mappedMouseX);
                        selectedSegment.setYPos(mappedMouseY); 
                    }
                }
                double radius = scale;
                g.setStroke(new BasicStroke((float) (2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{(float)(9)}, 0));
                double[] pos = newPositions(selectedSegment.getX(), selectedSegment.getY(), cameraX, cameraY, scale);
                g.setColor(WHITE);
                g.drawOval((int)(pos[0] - radius), (int)(pos[1] - radius), (int)(radius*2), (int)(radius*2));
                g.setStroke(new BasicStroke(1));
            }
        }
        mouseData.add(new Double[]{mappedMouseX, mappedMouseY});
        if(mouseData.size() > 20){
            mouseData.remove();
        }
    }
    
    public void reset(){
        globalStep = 0;
        step = 0;
        changeSpeed(1);
        getGA().clear();
        selectedSegment = null;
        simulationToolbar.setCurrentTab("simulation");
        resetQuadTree();
        seed = getRand().nextDouble() + getRand().nextInt(100000);
        generateTerrain();
    }
    
    public final void resetQuadTree(){
        QuadRect bounds = new QuadRect(0,0,xSize/2,ySize/2);
        quadTree = new QuadTree(null, bounds, capacity, maxDepth);
        for(Creature c : getGA().getCreatures()){
            c.insertIntoQuadTree(quadTree);
        }
    }
    
    public void setSelectedSegment(Creature selectedCreature){
        this.selectedSegment = selectedCreature.getHead();
    }
    
    public void setCameraX(double cameraX){
        this.cameraX = cameraX;
    }
    public void setCameraY(double cameraY){
        this.cameraY = cameraY;
    }
    public void setScale(double scale){
        this.scale = scale;
    }
    
    public void setInfoText(String text){
        JLabel label = ((JLabel)simulationToolbar.getComponent("componentInfo"));

        if(!text.equals("")){
            String newText = "<html>" + text + "</html>";
            newText = newText.replaceAll("creatures", "<font color=#27db93>" + "creatures" + "</font>");
            newText = newText.replaceAll(" creature", "<font color=#27db93>" + " creature" + "</font>");
            newText = newText.replaceAll("simulation", "<font color=#db8127>" + "simulation" + "</font>");
            newText = newText.replaceAll("quadtree", "<font color=#b7d948>" + "quadTree" + "</font>");
            newText = newText.replaceAll("energy", "<font color=#fff540>" + "energy" + "</font>");
            label.setText(newText);
        }else{
            label.setText("");
        }
    }
    
    public void addParticle(Particle p){
        particles.add(p);
    }
    
    //getter methods
    public double getStep(){
        return step;
    }
    public int getGlobalStep(){
        return globalStep;
    }
    public QuadTree getQuadTree(){
        return quadTree;
    }
    public double getSpeed(){
        return speed;
    }
    public double getDecaySpeed(){
        return decaySpeed;
    }
    public double getGrowthSpeed(){
        return growthSpeed;
    }
    public double getEfficiencyDecrease(){
        return efficiencyDecrease;
    }
    public double getCameraX(){
        return cameraX;
    }
    public double getCameraY(){
        return cameraY;
    }
    public double getScale(){
        return scale;
    }
    public double getLastCameraX(){
        return lastCameraX;
    }
    public double getLastCameraY(){
        return lastCameraY;
    }
    public double getLastScale(){
        return lastScale;
    }
    public double getXSize(){
        return xSize;
    }
    public double getYSize(){
        return ySize;
    }
    public int getMouseX(){
        return (int) mouseX;
    }
    public int getMouseY(){
        return (int) mouseY;
    }
    public boolean getMousePressed(){
        return mousePressed;
    }
    public FileManager getFileManager(){
        return fileManager;
    }
    public SegmentInstance getSelectedSegment(){
        return selectedSegment;
    }
    public SimulationToolbar getSimulationToolbar(){
        return simulationToolbar;
    }
    public ButtonVariable getShowUIBool(){
        return showUI;
    }
    public ButtonVariable getHomeBool(){
        return home;
    }
    public ButtonVariable getSlowDownBool(){
        return slowDown;
    }
    public ButtonVariable getSpeedUpBool(){
        return speedUp;
    }
    public ButtonVariable getPausedBool(){
        return paused;
    }
    public ButtonVariable getRandomCreatureBool(){
        return randomCreature;
    }
    public ButtonVariable getSelectRandomBool(){
        return selectRandom;
    }
    public ButtonVariable getKillRandomBool(){
        return killRandom;
    }
    public ButtonVariable getKillHalfBool(){
        return killHalf;
    }
    public ButtonVariable getResetBool(){
        return reset;
    }
    public ButtonVariable getSaveBool(){
        return saveFile;
    }
    public ButtonVariable getLoadBool(){
        return loadFile;
    }
    public ButtonVariable getSimulationBool(){
        return simulation;
    }
    public ButtonVariable getCreatureBool(){
        return creature;
    }
    public ButtonVariable getDeleteBool(){
        return deleteFile;
    }
    public ButtonVariable getImportBool(){
        return importFile;
    }
    public ButtonVariable getShowInfoBool(){
        return showInfo;
    }
    public ButtonVariable getShowQuadTreeBool(){
        return showQuadTree;
    }
    public ButtonVariable getViewModeBool(){
        return viewMode;
    }
    public ButtonVariable getXSizeUpdateBool(){
        return xSizeUpdate;
    }
    public ButtonVariable getYSizeUpdateBool(){
        return ySizeUpdate;
    }
    public ButtonVariable getDragUpdateBool(){
        return dragUpdate;
    }
    public ButtonVariable getMaxPopulationUpdateBool(){
        return maxPopulationUpdate;
    }
    public ButtonVariable getKillCreatureBool(){
        return killCreature;
    }
    public ButtonVariable getCloneCreatureBool(){
        return cloneCreature;
    }
    public ButtonVariable getFollowCreatureBool(){
        return followCreature;
    }
    public ButtonVariable getMutateCreatureBool(){
        return mutateCreature;
    }
    public ButtonVariable getAddEnergyBool(){
        return addEnergy;
    }
    public ButtonVariable getDetailModeBool(){
        return detailMode;
    }
    //mouse listener methods
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        lastScale = scale;
        int notches = e.getWheelRotation();
        if (notches < 0) {
            if(scale * 1.25 < 500){
                scale *= 1.25;
                resizeTextures();
            }
        }else if(notches > 0){
            if(scale / 1.25 > 1){
                scale /= 1.25;
                resizeTextures();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        if(!simulationToolbar.isMouseOver((int)mouseX, (int)mouseY)){
            selectedSegment = null;
            QuadRect range = new QuadRect(mappedMouseX, mappedMouseY, 1.25, 1.25);
            ArrayList<QuadNode> result = quadTree.query(range, new ArrayList<>());
            for(QuadNode node : result){
                if(node instanceof SegmentInstance){
                    if(pointIntersectsSegment(mappedMouseX, mappedMouseY, (SegmentInstance)node)){
                        selectedSegment = ((SegmentInstance)node);
                    }
                }
            }
            if(selectedSegment != null && selectedSegment.getCreature() != null){
                selectedSegment.getCreature().createIcon();
                simulationToolbar.setCurrentTab("creature");
            }else{
                simulationToolbar.setCurrentTab("simulation");
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
}
