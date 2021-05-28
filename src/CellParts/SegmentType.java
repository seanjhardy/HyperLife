/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import static GUI.GUIManager.brightness;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author seanjhardy
 */
public class SegmentType extends CellPart{
    
    private final ArrayList<CellPartData> children = new ArrayList<>();
    private double width, length;
    private boolean muscle, nerve, fat;
    private double boneDensity, muscleStrength;

    public SegmentType(Creature creature, int ID) {
        super(creature, ID, true);
    }
    
    public void addChild(CellPartData child){
        children.add(child);
    }
    public ArrayList<CellPartData> getChildren(){
        return children;
    } 
    
    public void setWidth(double width){
        this.width = width;
    }
    public void setLength(double length){
        this.length = length;
    }
    public void setMuscle(boolean muscle){
        this.muscle = muscle;
    }
    public void setBoneDensity(double bone){
        this.boneDensity = bone;
    }
    public void setNerve(boolean nerve){
        this.nerve = nerve;
    }
    public void setFat(boolean fat){
        this.fat = fat;
    }
    public void setMuscleStrength(double strength){
        this.muscleStrength = strength;
    }
    
    @Override
    public double getBuildCost(){
        double buildCost = getBuildScale()*length*width*(1 + 0.2*(boneDensity) + 0.5*(muscle?1.0:0.0) + 0.1*(nerve?1.0:0.0) + 0.01*(fat?1.0:0.0));
        return buildCost;
    }
    
    public double getWidth(){
        return width;
    }
    public double getLength(){
        return length;
    }
    public double getBoneDensity(){
        return boneDensity;
    }
    public boolean getMuscle(){
        return muscle;
    }
    public boolean getNerve(){
        return nerve;
    }
    public boolean getFat(){
        return fat;
    }
    public double getMuscleStrength(){
        return muscleStrength;
    }
}

