/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts.Proteins;

import CellParts.CellPartData;
import CellParts.ProteinInstance;
import CellParts.ProteinType;
import CellParts.SegmentInstance;
import static GUI.GUIManager.getSimulationPanel;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import static Physics.PhysicsManager.newPositions;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author seanjhardy
 */
public class Myelin extends ProteinInstance{ 
    
    public Myelin(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        parent.addMyelin(0.5);
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
    
    public double getBuildCost(){
        return getBuildScale()*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*2*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();

        double angle = parent.getRealAngle() + radAngle + Math.PI/2;
        double[] newPositions = newPositions(startPoint.getX(), startPoint.getY(), cameraX, cameraY, scale);

        double w = (0.1+0.3*getAdjustedSize())*scale;
        double l = (0.1+0.3*getAdjustedSize())*scale;

        RoundRectangle2D  shape = new RoundRectangle2D.Double(-l/2,-w/2,l,w, w*0.5, w*0.5);
        AffineTransform tx = new AffineTransform();
        tx.rotate(angle);
        Shape newShape = tx.createTransformedShape(shape);

        AffineTransform at = new AffineTransform();
        at.translate(newPositions[0], newPositions[1]);
        newShape = at.createTransformedShape(newShape);

        g.setColor(((ProteinType)cellData.getType()).getColour());
        g.fill(newShape);
    }
}


