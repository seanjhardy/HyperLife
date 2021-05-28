/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HyperLife;

import GUI.GUIManager;
import GeneticAlgorithm.GeneticAlgorithm;
import java.util.Random;

/**
 *
 * @author seanjhardy
 */
public class HyperLife {
    private static GUIManager frame;
    private static GeneticAlgorithm geneticAlgorithm;
    
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        geneticAlgorithm = new GeneticAlgorithm();
        frame = new GUIManager();
    }
    
    public static GUIManager getGUIManager(){
        return frame;
    }
    
    public static Random getRand(){
        return random;
    }
    
    public static GeneticAlgorithm getGA(){
        return geneticAlgorithm;
    }
    
}
