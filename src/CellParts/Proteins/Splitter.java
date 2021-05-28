package CellParts.Proteins;

import CellParts.CellPartData;
import CellParts.ProteinInstance;
import CellParts.SegmentInstance;
import static GUI.GUIManager.getSimulationPanel;
import GeneticAlgorithm.Creature;
import static Physics.PhysicsManager.newPositions;
import java.awt.Graphics2D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author seanjhardy
 */
public class Splitter extends ProteinInstance{
    
    
    public Splitter(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        creature.addOutput(this);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        startPoint.setLastX(point[0]);
        startPoint.setLastY(point[1]);
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        double[] startPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);
        
        double totalSize = (0.05 + 0.4*getAdjustedSize())*scale;
        g.setColor(cellData.getType().getColour());
        g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
    }
}
