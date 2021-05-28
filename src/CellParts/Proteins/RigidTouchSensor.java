/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts.Proteins;

import CellParts.CellPartData;
import Physics.Point;
import CellParts.ProteinInstance;
import CellParts.ProteinType;
import CellParts.SegmentInstance;
import static GUI.GUIManager.getSimulationPanel;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import static Physics.PhysicsManager.newPositions;
import static Physics.PhysicsManager.pointIntersectsSegment;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.ArrayList;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class RigidTouchSensor extends ProteinInstance{
    
    private Point endPoint;
    private SegmentInstance overlapping;
    
    public RigidTouchSensor(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        endPoint = new Point();
        creature.addInput(this);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        double length = 5*getAdjustedSize();
        double angle = parent.getRealAngle() + radAngle;
        double endX = startPoint.getX() + FastMath.cos(angle)*length;
        double endY = startPoint.getY() + FastMath.sin(angle)*length;
        endPoint.setX(endX);
        endPoint.setY(endY);
        if(getSimulationPanel().getGlobalStep() % 10 == 0){
            checkForOverlap();
        }
    }
    
    public void checkForOverlap(){
        QuadRect range = new QuadRect(endPoint.getX(),endPoint.getY(), 1.25, 1.25);
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        overlapping = null;
        for(QuadNode node : segments){
            if(node instanceof SegmentInstance){
                if(((SegmentInstance)node).getCreature() != this.getCreature()){
                    if(pointIntersectsSegment(endPoint.getX(), endPoint.getY(), (SegmentInstance)node)){
                        overlapping = (SegmentInstance) node;
                    }
                }
            }
        }
    }
    
    public double getBuildCost(){
        return getBuildScale()*0.05*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*0.05*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        double totalSize = 0.3*getAdjustedSize()*scale;
        if(totalSize > 0.5){
            double[] startPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);
            double[] endPosition = newPositions(endPoint.getX(),endPoint.getY(),cameraX,cameraY,scale);
            g.setColor(cellData.getType().getColour());
            g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
            g.setStroke(new BasicStroke((float) (0.5*totalSize)));
            g.drawLine((int)(startPosition[0]), (int)(startPosition[1]), (int)endPosition[0], (int)endPosition[1]);
            g.setStroke(new BasicStroke(1));
        }
    }
}
