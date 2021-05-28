/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts;

import static GUI.GUIManager.brightness;
import GeneticAlgorithm.Creature;
import java.awt.Color;
import java.io.Serializable;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class CellPart{
    protected Creature creature;
    protected final int ID;
    protected final boolean isSegment;
    protected double lastOverlapPercent = 0.0;
    protected Color colour;
    protected Color overlappedColour, lightColour, shadowColour;
    
    public CellPart(Creature creature, int ID, boolean isSegment){
        this.creature = creature;
        this.ID = ID;
        this.isSegment = isSegment;
    }
    
    public final int getID(){
        return ID;
    }
    public double getBuildCost(){
        return 0;
    }
    public boolean isSegment(){
        return isSegment;
    }
    public Color getColour(){
        if(creature != null){
            if(FastMath.abs(creature.getOverlapPercent() - lastOverlapPercent) > 0.05){
                lastOverlapPercent = creature.getOverlapPercent();
                overlappedColour = brightness(colour, 1.0-FastMath.min(creature.getOverlapPercent(),0.9));
            }
        }
        return overlappedColour;
    }
    public Color getLightColour(){
        return lightColour;
    }
    public Color getShadowColour(){
        return shadowColour;
    }
    public Color getOriginalColour(){
        return colour;
    }
    
    
    public void setColour(Color colour){
        this.colour = colour;
        this.overlappedColour = colour;
        lightColour = brightness(colour, 2);
        shadowColour = brightness(colour, 0.25);
    }
}
