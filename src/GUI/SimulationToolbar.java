/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import static GUI.GUIManager.brightness;
import static GUI.GUIManager.getColour;
import static GUI.GUIManager.getImage;
import static GUI.GUIManager.getScreenSize;
import static GUI.GUIManager.getSimulationPanel;
import static GUI.SimulationPanel.DEFAULT_FRICTION;
import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Color.BLACK;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 *
 * @author seanjhardy
 */
public class SimulationToolbar extends MenuManager{
    
    public int creatureIconWidth = 250, creatureIconHeight = 140;
    public BufferedImage creatureIcon = null;
    
    public SimulationToolbar(SimulationPanel panel) {
        this.parent = panel;
        
        int width = (int)getScreenSize().getWidth();
        int height = (int)getScreenSize().getHeight();
        
        this.menuHeight = 150;
        this.startX = 0;
        this.startY = height-menuHeight;
        this.menuWidth = width;
        
        createComponents(panel);
        createCreatureDataComponents(panel);
    }
    
    public final void createComponents(SimulationPanel panel){
        Color mainColour = getColour("menuBackground");
        Color mainShadowColour = brightness(mainColour,0.5);
        Color mainColour2 = getColour("menuBackground2");
        Color mainColour3 = getColour("menuBackground3");
        Color mainColour4 = getColour("menuBackground4");
        Color mainColour5 = getColour("menuBackground5");
        Color redButton = getColour("red");
        Color noColour = new Color(0,0,0,0);
        
        JLabel info = createLabel("componentInfo","", null, SwingConstants.LEFT, 14, new Color(0,0,0,220));
        info.setOpaque(true);
        info.setBorder(new MatteBorder(10,10,10,10, BLACK));
        ImageIcon icon = new ImageIcon(getImage("ShowInfo")); 
        info.setIcon(icon);
        //info.setVerticalAlignment(JLabel.TOP);
        
        createImageButton("homeBtn","Planet","PlanetHighlighted","PlanetHighlighted","Planet", "Home button", panel.getHomeBool(), noColour).setBorder(null);
        createImageButton("showUIBtn","ShowUI","ShowUIHighlighted","HideUIHighlighted","HideUI", "Show/hide menu button", panel.getShowUIBool(), noColour).setBorder(null);
        createImageButton("infoBtn","ShowInfo","ShowInfoHighlighted","HideInfoHighlighted","HideInfo", "Show/hide component info", panel.getShowInfoBool(), noColour).setBorder(null);
        createImageButton("saveBtn","Save","SaveHighlighted","SaveHighlighted","Save", "Save the current simulation", panel.getSaveBool(), noColour).setBorder(null);
        createImageButton("loadBtn","Load","LoadHighlighted","LoadHighlighted","Load", "Load a saved simulation", panel.getLoadBool(), noColour).setBorder(null);
        createImageButton("showQuadTreeBtn","ShowQuadTree","ShowQuadTreeHighlighted","HideQuadTreeHighlighted","HideQuadTree", "Show/hide the quadtree (spatial partitioning used to optimise the simulation)", panel.getShowQuadTreeBool(), noColour).setBorder(null);
        createImageButton("followCreatureBtn","FollowCreature","FollowCreatureHighlighted","FreeCameraHighlighted","FreeCamera", "Toggle camera between following the selected creature and being free", panel.getFollowCreatureBool(), noColour).setBorder(null);
        createImageButton("viewModeBtn","TopDown","TopDownHighlighted","SideScrollingHighlighted","SideScrolling", "Toggle between a top down view and a sidescrolling view (with gravity)", panel.getViewModeBool(), noColour).setBorder(null);
        createImageButton("detailModeBtn","DetailMode","DetailModeHighlighted","SimpleModeHighlighted","SimpleMode", "Toggle between a detailed render and a simplistic view", panel.getDetailModeBool(), noColour).setBorder(null);
        
        createImageButton("simulationBtn","Simulation","SimulationHighlighted","SimulationHighlighted","Simulation", "Show simulation settings in the toolbar", panel.getSimulationBool(), noColour).setBorder(null);
        createImageButton("creatureBtn","Creature","CreatureHighlighted","CreatureHighlighted","Creature", "Show creature settings in the toolbar", panel.getCreatureBool(), noColour).setBorder(null);
        
        JTextField nameField = createTextField("nameTextField", panel.getFileManager().getFileName(), "Set the name of the current simulation", mainColour);
        nameField.setBorder(null);
        nameField.addFocusListener( new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                boolean fileExists = panel.getFileManager().fileExists(nameField.getText());
                if(!fileExists){
                    panel.getFileManager().renameFile(nameField.getText());
                }
            }
        });
        createTextButton("slowDownBtn","<<","<<", "Slow down the simulation", panel.getSlowDownBool(), mainColour);
        createImageButton("pauseBtn","Playing","","","Paused", "Pause/play the simulation", panel.getPausedBool(), mainColour);
        createTextButton("speedUpBtn",">>",">>", "Speed up the simulation (may cause instability and greatly decreases accuracy)", panel.getSpeedUpBool(), mainColour);
        createTextButton("resetBtn","Reset","Reset", "Kill all creatures and reset the map", panel.getResetBool(), mainColour2);
        createTextButton("newCreatureBtn","New Creature","New Creature", "Generate a creature with a randomised genome", panel.getRandomCreatureBool(), mainColour3);
        createTextButton("selectRandomBtn","<html><center>Select <br>Random</center></html>","<html><center>Select <br>Random</center></html>", "Select a random creature", panel.getSelectRandomBool(), mainColour4);
        createTextButton("killRandomBtn","<html><center>Kill <br>Random</center></html>","<html><center>Kill <br>Random</center></html>", "Kill a random creature from the pool", panel.getKillRandomBool(), mainColour5);
        createTextButton("killHalfBtn","<html><center>Kill <br>Half</center></html>","<html><center>Kill <br>Half</center></html>", "Kill half of all creatures from the pool", panel.getKillHalfBool(), mainColour5);
        
        int xSize = (int) panel.getXSize();
        int ySize = (int) panel.getYSize();
        createLabel("sliderBorder", "", null, SwingConstants.CENTER, 14, mainColour).setBorder(new MatteBorder(3,3,3,3, mainShadowColour));
        createLabel("xSizeLabel","Width: " + xSize, "Change the width of the simulation map", SwingConstants.CENTER, 14, noColour);
        createSlider("xSizeSlider","X Size:", "Change the width of the simulation map", 10, 200, xSize, 20, true, panel.getXSizeUpdateBool(), noColour);
        createLabel("ySizeLabel","Height: " + ySize, "Change the height of the simulation map",  SwingConstants.CENTER, 14, noColour);
        createSlider("ySizeSlider","Y Size:", "Change the height of the simulation map", 10, 200, ySize, 20, true, panel.getYSizeUpdateBool(), noColour);  
        createLabel("dragLabel","Drag:", "Change the frictional force",  SwingConstants.CENTER, 14, noColour);
        createSlider("dragSlider","Drag Coefficient:", "Change the frictional force", 0, 20, (int) ((1.0 - DEFAULT_FRICTION)*1000), 20, false, panel.getDragUpdateBool(), noColour);  
        String maxPopText = "<html> Max Pop: <font face = \"Bedrock\" size = \"5\">∞</font></html>";
        createLabel("maxPopulationLabel",maxPopText, "Restrict the maximum number of creatures in the simulation",  SwingConstants.CENTER, 14, noColour);
        createSlider("maxPopulationSlider", "Max Population:", "Restrict the maximum number of creatures in the simulation", 0, 50, 50, 50, false, panel.getMaxPopulationUpdateBool(), noColour);  
        
        createLabel("infoLabel", "", "Simulation data", SwingConstants.CENTER, 14, mainColour);
        
        //load file menu
        createLabel("loadLabel","Load File:", "Load a saved file", SwingConstants.CENTER, 14, mainColour);
        createTextButton("deleteFileBtn","Delete","Delete", "Delete the selected file", panel.getDeleteBool(), redButton);
        createTextButton("importFileBtn","Import","Import", "Import a new file from a specified location on the computer", panel.getImportBool(), mainColour2);
        JComboBox comboBox = createComboBox("loadFileComboBox");
        panel.getFileManager().refreshComboBox(comboBox);
    }
    
    public final void createCreatureDataComponents(SimulationPanel panel){
        Color mainColour = getColour("menuBackground");
        Color noColour = new Color(0,0,0,0);
        
        JLabel creatureIconContainer = createLabel("creatureIcon", "", "Visualisation of the creatures genetic code", SwingConstants.CENTER, 14, brightness(mainColour, 2));
        creatureIconContainer.setBorder(new AdvancedBevelBorder(creatureIconContainer, 5));
        //createLabel("nameLabel", "NameData", SwingConstants.CENTER, 18, noColour).setBorder(null);
        JLabel label = createLabel("creatureData", "Info test", "Creature data", SwingConstants.LEFT, 14, mainColour);
        label.setBorder(new EmptyBorder(10,10,10,10));
        //label.setBorder(new AdvancedBevelBorder(label, 5));
        createImageButton("cloneBtn","CloneCreature","CloneCreatureHighlighted","CloneCreatureHighlighted","CloneCreature", "Clone the creature", panel.getCloneCreatureBool(), noColour).setBorder(null);
        createImageButton("mutateBtn","MutateCreature","MutateCreatureHighlighted","MutateCreatureHighlighted","MutateCreature", "Mutate the creature randomly", panel.getMutateCreatureBool(), noColour).setBorder(null);
        createImageButton("killBtn","KillCreature","KillCreatureHighlighted","KillCreatureHighlighted","KillCreature", "Kill the creature (irreversible)", panel.getKillCreatureBool(), noColour).setBorder(null);
        createImageButton("addEnergyBtn","AddEnergy","AddEnergyHighlighted","AddEnergyHighlighted","AddEnergy", "Give the creature 10 energy", panel.getAddEnergyBool(), noColour).setBorder(null);
    
    }
    
    public void draw(Graphics2D g){
        if(parent.getShowUIBool().getValue()){
            drawBanner(g);
        }
        setBounds(g);
    }
        
    public void drawBanner(Graphics2D g){
        Color background = getColour("menuBackground");
        int width = (int)getScreenSize().getWidth();
        int height = (int)getScreenSize().getHeight();
        
        //dna banner
        g.setColor(brightness(background,0.5));
        g.fillRect(startX,startY-24,width,24);
        g.drawImage(getImage("DNABannerSmall"), startX,startY-19,null);   
        g.setColor(brightness(background,2));
        g.setStroke(new BasicStroke(4));
        g.drawRect(startX,startY-26,menuWidth,24);
        g.setStroke(new BasicStroke(1));
        
        //name info tab
        g.setColor(background);
        g.fillRoundRect(startX+(menuWidth/2)-200,startY-30,400,50,20,20);
        g.fillRoundRect(startX+(menuWidth/2)-200,startY-30,400,50,20,20);
        g.setColor(brightness(background,2));
        g.setStroke(new BasicStroke(4));
        g.drawLine(startX,startY,startX+menuWidth,startY);
        g.drawRoundRect(startX+(menuWidth/2)-200,startY-30,400,50,20,20);
        g.setStroke(new BasicStroke(1));
        
        //banner
        g.setColor(background);
        g.fillRect(startX,startY,width,menuHeight);
        
        //home circle
        g.setColor(brightness(background,0.5));
        g.fillRoundRect(startX-100, height-152, 320, 151, 50, 50);
        g.setColor(brightness(background,2));
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(startX-100, height-152, 320, 151, 50, 50);
        g.setStroke(new BasicStroke(1));
        
    }
    
    public void setBounds(Graphics2D g){
        if(parent.getShowUIBool().getValue()){
            for(JComponent c : components){
                c.setBounds(0,0,0,0);
            }
            double mouseX = MouseInfo.getPointerInfo().getLocation().getX();
            double mouseY = MouseInfo.getPointerInfo().getLocation().getY();
            if(parent.getShowInfoBool().getValue() && !((JLabel)getComponent("componentInfo")).getText().equals("")){
                double width = 300;
                double height = 100;
                if(mouseX + width > getScreenSize().getWidth()){
                    mouseX -= (50+width);
                }
                if(mouseY + height > getScreenSize().getHeight()){
                    mouseY -= (height);
                }
                getComponent("componentInfo").setBounds((int)mouseX + 25, (int)mouseY, (int)width, (int)height);
            }
            getComponent("homeBtn").setBounds(startX, startY+3, 140, 140);
            
            getComponent("infoBtn").setBounds(startX+145, startY+20, 25, 25);
            getComponent("showQuadTreeBtn").setBounds(startX+145, startY+60, 25, 25);
            getComponent("showUIBtn").setBounds(startX+145, startY+100, 25, 25);
            
            getComponent("followCreatureBtn").setBounds(startX+180, startY+20, 25, 25);
            getComponent("viewModeBtn").setBounds(startX+180, startY+60, 25, 25);
            getComponent("detailModeBtn").setBounds(startX+180, startY+100, 25, 25);
            
            
            getComponent("nameTextField").setBounds(startX + (menuWidth/2)-100,startY-25,200,23);
            getComponent("simulationBtn").setBounds(startX+(menuWidth/2)-190,startY-25,20,20);
            getComponent("creatureBtn").setBounds(startX+(menuWidth/2)-160,startY-25,20,20);
            getComponent("saveBtn").setBounds(startX+(menuWidth/2)+130,startY-25,20,20);
            getComponent("loadBtn").setBounds(startX+(menuWidth/2)+160,startY-25,20,20);
               
            if(currentTab.equals("simulation")){
                getComponent("slowDownBtn").setBounds(startX + (menuWidth/2)-150,startY,100,50);
                getComponent("pauseBtn").setBounds(startX + (menuWidth/2)-50,startY,100,50);
                getComponent("speedUpBtn").setBounds(startX + (menuWidth/2)+50,startY,100,50);
                getComponent("newCreatureBtn").setBounds(startX + (menuWidth/2)-150,startY+50,150,50);
                getComponent("resetBtn").setBounds(startX + (menuWidth/2),startY+50,150,50);
                
                getComponent("selectRandomBtn").setBounds(startX + (menuWidth/2)-150,startY+100,100,50);
                getComponent("killRandomBtn").setBounds(startX + (menuWidth/2)-50,startY+100,100,50);
                getComponent("killHalfBtn").setBounds(startX + (menuWidth/2)+50,startY+100,100,50);

                getComponent("infoLabel").setBounds(startX + (menuWidth/2)+150, startY, 150, menuHeight);

                getComponent("sliderBorder").setBounds(startX + (menuWidth/2)+298,startY,304,menuHeight);
                getComponent("xSizeLabel").setBounds(startX + (menuWidth/2)+310,startY,130,25);
                getComponent("xSizeSlider").setBounds(startX + (menuWidth/2)+310,startY+25,130,50);
                getComponent("ySizeLabel").setBounds(startX + (menuWidth/2)+310,startY+75,130,25);
                getComponent("ySizeSlider").setBounds(startX + (menuWidth/2)+310,startY+100,130,50);
                getComponent("dragLabel").setBounds(startX + (menuWidth/2)+460,startY,130,25);
                getComponent("dragSlider").setBounds(startX + (menuWidth/2)+460,startY+25,130,50);
                getComponent("maxPopulationLabel").setBounds(startX + (menuWidth/2)+460,startY+75,130,25);
                getComponent("maxPopulationSlider").setBounds(startX + (menuWidth/2)+460,startY+100,130,50);
             
            }else if(currentTab.equals("load")){
                getComponent("loadLabel").setBounds(startX+(menuWidth/2)-200,startY,100,50);
                
                JComboBox loadFileComboBox = (JComboBox)getComponent("loadFileComboBox");
                loadFileComboBox.setBounds(startX+(menuWidth/2)-100,startY,300,50);
                Component arrow = loadFileComboBox.getComponents()[0];
                arrow.setSize(20, 50);
                arrow.setLocation(loadFileComboBox.getWidth()-arrow.getWidth(),0);
                
                getComponent("importFileBtn").setBounds(startX+(menuWidth/2)-200,startY+50,100,50);
                getComponent("deleteFileBtn").setBounds(startX+(menuWidth/2)+100,startY+50,100,50);
                
            }else if(currentTab.equals("creature")){
                getComponent("creatureIcon").setBounds(startX+(menuWidth/2)-650, startY+5, creatureIconWidth, creatureIconHeight);
                if((int)getSimulationPanel().getGlobalStep() % 5 == 0){
                    if(getSimulationPanel().getSelectedSegment() != null && getSimulationPanel().getSelectedSegment().getCreature() != null){
                        creatureIcon = getSimulationPanel().getSelectedSegment().getCreature().createIcon();
                    }
                }
                g.drawImage(creatureIcon, startX+(menuWidth/2)-650, startY+5, null); 
                getComponent("creatureData").setBounds(startX+(menuWidth/2)-395, startY, 200, menuHeight);
                
                int creatureButtonsY = 10;
                getComponent("cloneBtn").setBounds(startX+(menuWidth/2)-200, startY+creatureButtonsY, 30, 30);
                getComponent("mutateBtn").setBounds(startX+(menuWidth/2)-200, startY+creatureButtonsY+31, 30, 30);
                getComponent("killBtn").setBounds(startX+(menuWidth/2)-200, startY+creatureButtonsY+31*2, 30, 30);
                getComponent("addEnergyBtn").setBounds(startX+(menuWidth/2)-200, startY+creatureButtonsY+31*3, 30, 30);
                if(getSimulationPanel().getSelectedSegment() != null && getSimulationPanel().getSelectedSegment().getCreature() != null){
                    getSimulationPanel().getSelectedSegment().getCreature().updateCreatureData();
                }
            }
        }else{
            for(JComponent c : components){
                c.setBounds(0,0,0,0);
            }
            getComponent("showUIBtn").setBounds(startX+145, startY+100, 25, 25);
        }
    }
    
    public int getCreatureIconWidth(){
        return creatureIconWidth;
    }
    public int getCreatureIconHeight(){
        return creatureIconHeight;
    }
    
}
