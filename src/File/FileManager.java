/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package File;

import GUI.SimulationPanel;
import GeneticAlgorithm.Creature;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

/**
 *
 * @author seanjhardy
 */
public class FileManager {
    private SimulationPanel parent;
    private final File fileDirectory = new File("saves/Simulations");
    private String fileName;
    private final JFileChooser fileChooser;
    
    public FileManager(SimulationPanel parent, String fileName){
        this.fileName = fileName;
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }
    
    public String getFileName(){
        return fileName;
    }
    
    public void renameFile(String newName){
        //add code to import file
        
        fileName = newName;
    }
    
    public void deleteFile(){
        //add code to delete file
    }
    
    public void saveFile(){
    
    }

    public boolean fileExists(String fileName){
        for(File file: fileDirectory.listFiles()){
            if(file.getName().equals(fileName)){
                return true;
            }
        }
        return false;
    }    
    
    public void refreshComboBox(JComboBox comboBox){
        for(File file: fileDirectory.listFiles()){
            comboBox.addItem(file.getName());
        }
    }
    
    public void importFile(){
        //add code to import file
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            // user selects a file
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println(selectedFile.getName());
        }
    }
    
    public void saveCreature(Creature creature){
        
    }
    
}
