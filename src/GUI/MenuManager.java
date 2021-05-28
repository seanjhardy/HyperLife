/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import static GUI.GUIManager.brightness;
import static GUI.GUIManager.getDefaultFont;
import static GUI.GUIManager.getImage;
import static GUI.GUIManager.getSimulationPanel;
import static GUI.GUIManager.addAlpha;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author s-hardy
 */
public class MenuManager{
    protected SimulationPanel parent;
    protected int menuHeight, menuWidth;
    protected int startX, startY;
    protected String currentTab = "simulation";
    protected final ArrayList<JComponent> components = new ArrayList<>();
    protected final ArrayList<String> componentNames = new ArrayList<>();  
    
    public final AdvancedButton createTextButton(String name, String onState, String offState, String info, ButtonVariable bool, Color colour){
        AdvancedButton button = new AdvancedButton("");
        if(bool.getValue()){
            button.setText(onState);
        }else{
            button.setText(offState);
        }
        button.addActionListener((ActionEvent e) -> {
            if(e.getSource() == button){
                bool.setValue(!bool.getValue());
                if(bool.getValue()){
                    button.setText(onState);
                }else{
                    button.setText(offState);
                }
            }
        });
        button.setFont(new Font(getDefaultFont(), 0, 14));
        button.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(addAlpha(colour, 255));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(addAlpha(colour, 200));
                getSimulationPanel().setInfoText(info);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(addAlpha(colour, 100));
                getSimulationPanel().setInfoText("");
            }
        });
        button.addBorder(6);
        button.setForeground(WHITE);
        button.setColour(colour);
        button.setFocusPainted(false);
        parent.add(button);
        components.add(button);
        componentNames.add(name);
        return button;
    }
    
    public final AdvancedButton createImageButton(String name, String onImage, String hoverOn, String hoverOff, String offImage, String info, ButtonVariable bool, Color colour){
        BufferedImage image = bool.getValue() ? getImage(onImage) : getImage(offImage);
        AdvancedButton button = new AdvancedButton(image, false);
        button.addActionListener((ActionEvent e) -> {
            if(e.getSource() == button){
                bool.setValue(!bool.getValue());
                if(hoverOn.equals("")){
                    if(bool.getValue()){
                        button.setIcon(getImage(onImage));
                    }else{
                        button.setIcon(getImage(offImage));
                    }
                }else{
                    if(bool.getValue()){
                        button.setIcon(getImage(hoverOn));
                    }else{
                        button.setIcon(getImage(hoverOff));
                    }
                }
            }
        });
        button.setFont(new Font(getDefaultFont(), 0, 14));
        button.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(addAlpha(colour, 255));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if(!hoverOn.equals("")){
                    if(bool.getValue()){
                        button.setIcon(getImage(hoverOn));
                    }else{
                        button.setIcon(getImage(hoverOff));
                    }
                }
                button.setBackground(addAlpha(colour, 200));
                getSimulationPanel().setInfoText(info);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(bool.getValue()){
                    button.setIcon(getImage(onImage));
                }else{
                    button.setIcon(getImage(offImage));
                }
                button.setBackground(addAlpha(colour, 100));
                getSimulationPanel().setInfoText("");
            }
        });
        button.addBorder(6);
        button.setForeground(WHITE);
        button.setColour(colour);
        button.setFocusPainted(false);
        
        parent.add(button);
        components.add(button);
        componentNames.add(name);
        return button;
    }
    
    public final JTextField createTextField(String name, String defaultText, String info, Color colour){
        JTextField textField = new JTextField(defaultText);
        textField.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                textField.setBackground(brightness(colour, 2));
                getSimulationPanel().setInfoText(info);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                textField.setBackground(brightness(colour, 1));
                getSimulationPanel().setInfoText("");
            }
        });
        textField.setBorder(new MatteBorder(5,5,5,5, brightness(colour,2)));
        textField.setForeground(WHITE);
        textField.setCaretColor(WHITE);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setBackground(colour);
        textField.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(textField);
        components.add(textField);
        componentNames.add(name);
        return textField;
    }
    
    public final JLabel createLabel(String name, String defaultValue, String info, int direction, int fontSize, Color colour){
        JLabel label = new JLabel(defaultValue, direction);
        if(info != null){
            label.addMouseListener(new MouseListener(){
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setBackground(brightness(colour, 2));
                    getSimulationPanel().setInfoText(info);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setBackground(brightness(colour, 1));
                    getSimulationPanel().setInfoText("");
                }
            });
        }
        label.setForeground(WHITE);
        label.setBackground(colour);
        label.setBorder(new AdvancedBevelBorder(label, 3));
        label.setFont(new Font(getDefaultFont(), 0, fontSize));
        parent.add(label);
        components.add(label);
        componentNames.add(name);
        return label;
    }
    
    public final JSlider createSlider(String name, String text, String info, int lowerBound, int upperBound, int defaultValue, int numValues, boolean paintLabels, ButtonVariable updateBool, Color background){
        JSlider slider = new JSlider(JSlider.HORIZONTAL, lowerBound,upperBound, defaultValue);
        slider.setForeground(WHITE);
        slider.setMajorTickSpacing(upperBound-lowerBound);
        slider.setMinorTickSpacing((int)(((upperBound-lowerBound)/numValues)));
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(paintLabels);
        slider.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                getSimulationPanel().setInfoText(info);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                getSimulationPanel().setInfoText("");
            }
        });
        slider.setBackground(background);
        slider.setFocusable(false);
        slider.addChangeListener((ChangeEvent e) -> {
            updateBool.setValue(true);
        });
        slider.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(slider);
        components.add(slider);
        componentNames.add(name);
        return slider;
    }
    
    public final JComboBox createComboBox(String name){
        JComboBox comboBox = new JComboBox<>();
        comboBox.setBounds(500,500,200,200);
        comboBox.setForeground(WHITE);
        comboBox.setBackground(new Color(0, 0, 0));
        comboBox.setMaximumRowCount(6);
        comboBox.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(comboBox);
        components.add(comboBox);
        componentNames.add(name);
        return comboBox;
    }
    
    public final JScrollPane createDataPlot(String name){
        DataPlot panel = new DataPlot(name);
        panel.setVisible(true);
        panel.setOpaque(false);
        panel.setBackground(new Color(0,0,0,0));
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(panel);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(new Color(0,0,0,50));
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        scrollPane.setForeground(WHITE);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        parent.add(scrollPane);
        components.add(scrollPane);
        componentNames.add(name);
        return scrollPane;
    }
    
    public final JComponent getComponent(String name){
        int ID = componentNames.indexOf(name);
        return components.get(ID);
    }
    
    public void setCurrentTab(String tab){
        currentTab = tab;
    }
    
    public boolean isMouseOver(int mouseX, int mouseY){
        return (mouseX > startX && mouseX < startX + menuWidth && mouseY > startY && mouseY < startY + menuHeight);
    }

    public class DataPlot extends JPanel{
        private String name;
        
        public DataPlot(String name){
            super();
            this.name = name;
        }
        
        @Override
        public void paintComponent(Graphics g){
            getSimulationPanel().drawDataPlot(g, name);
        }
        
    }
}
