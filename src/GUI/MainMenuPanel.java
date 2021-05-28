/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import static GUI.GUIManager.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.JPanel;

/**
 *
 * @author seanjhardy
 */
public class MainMenuPanel extends JPanel{
    private GUIManager parent;
    private AdvancedButton exitBtn, simulationBtn;
    private JFXPanel VFXPanel;
    
    public MainMenuPanel(GUIManager parent){
        this.parent = parent;
        createComponents();
        //createVideoPlayer();
    }
    
    public final void createComponents(){
        exitBtn = new AdvancedButton("Exit");
        exitBtn.addActionListener((ActionEvent e) -> {
            if(e.getSource() == exitBtn){
                System.exit(0);
            }
        });
        setComponentProperties(exitBtn, 13);
        exitBtn.setColour(getColour("red"));
        exitBtn.addBorder(5);
        add(exitBtn);
        
        simulationBtn = new AdvancedButton("New Simulation");
        simulationBtn.addActionListener((ActionEvent e) -> {
            if(e.getSource() == simulationBtn){
                setCurrentPanel("simulationPanel");
                getSimulationPanel().reset();
            }
        });
        setComponentProperties(simulationBtn,15);
        simulationBtn.setColour(getColour("button"));
        simulationBtn.addBorder(5);
        add(simulationBtn);
    }
    
    public final void createVideoPlayer(){
        try {
            File file = new File("D:\\USB-Backup\\Java\\AAA - simulations\\HyperLife\\assets\\videos\\background3.mov");
            Player mediaPlayer = Manager.createRealizedPlayer(new MediaLocator(file.toURL()));

            Component video = mediaPlayer.getVisualComponent();

            Component control = mediaPlayer.getControlPanelComponent();
            if (video != null) {
                add(video);          // place the video component in the panel
            }
            //add(control);            // place the control in  panel
            mediaPlayer.start();
        } catch (IOException ex) {
            Logger.getLogger(MainMenuPanel.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (NoPlayerException ex) {
            Logger.getLogger(MainMenuPanel.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (CannotRealizeException ex) {
            Logger.getLogger(MainMenuPanel.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        drawBackground(g2);
        drawMenu(g2);
    }
    
    public void drawBackground(Graphics2D g){
        int width = (int) getScreenSize().getWidth();
        int height = (int) getScreenSize().getHeight();
        g.setColor(getColour("background"));
        g.fillRect(0,0, width, height);
    }
    
    public void drawMenu(Graphics2D g){
        int width = (int) getScreenSize().getWidth();
        int height = (int) getScreenSize().getHeight();
        simulationBtn.setBounds(width/2-150, (int) (height*0.35),300,60);
        exitBtn.setBounds(width/2-150, (int) (height*0.35)+100,300,60);
    }
    
}
