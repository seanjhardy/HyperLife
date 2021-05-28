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
import CellParts.SegmentType;
import static GUI.GUIManager.getSimulationPanel;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import static Physics.PhysicsManager.newPositions;
import static Physics.PhysicsManager.pointIntersectsSegment;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import java.awt.Graphics2D;
import java.util.ArrayList;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class Spike extends ProteinInstance{
    
    private Point endPoint;
    
    public Spike(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        this.endPoint = new Point();
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setLastX(startPoint.getX());
        startPoint.setLastY(startPoint.getY());
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        double length = (0.2 + getAdjustedSize());
        double angle = parent.getRealAngle() + radAngle;
        double endX = startPoint.getX() + FastMath.cos(angle)*length;
        double endY = startPoint.getY() + FastMath.sin(angle)*length;
        endPoint.setLastX(endPoint.getX());
        endPoint.setLastY(endPoint.getY());
        endPoint.setX(endX);
        endPoint.setY(endY);
        if(getSimulationPanel().getGlobalStep() % 5 == 0){
            checkForIntersection(angle);
        }
    }
    
    public void checkForIntersection(double angle){
        QuadRect range = new QuadRect(endPoint.getX(),endPoint.getY(), 1.25,1.25);
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        for(QuadNode node : segments){
            if(node instanceof SegmentInstance){
                if(((SegmentInstance)node).getCreature() != this.getCreature()){
                    if(pointIntersectsSegment(endPoint.getX(), endPoint.getY(), (SegmentInstance)node)){
                        
                        double nodeAngle = ((SegmentInstance) node).getRealAngle();
                        double nodeL = ((SegmentType)((SegmentInstance) node).getCellData().getType()).getLength();
                        double nodeW = ((SegmentType)((SegmentInstance) node).getCellData().getType()).getWidth();
                        //dot product of the direction vectors
                        double anglePercent = 1 - FastMath.abs(FastMath.cos(angle)*FastMath.cos(nodeAngle) + 
                                                           FastMath.sin(angle)*FastMath.sin(nodeAngle));
                        //speed of intersection
                        double spikeVel = FastMath.hypot(endPoint.getX() - endPoint.getLastX(), 
                                                         endPoint.getY() - endPoint.getLastY());
                        double sharpness = 1.0 - (0.05 + ((ProteinType)cellData.getType()).getParameter(0));
                        double spikeForce = anglePercent*spikeVel*sharpness*getAdjustedSize();
                        double nodeStrength = FastMath.min(nodeW/nodeL, nodeL/nodeW)*0.005;
                        if(spikeForce > nodeStrength){
                            if(((SegmentInstance) node).getCreature() != null && ((SegmentInstance) node).getCreature().getHead() != node){
                                ((SegmentInstance) node).detatchFromCreature();
                            }
                        }
                    }
                }
            }
        }
    }
    
    public double getBuildCost(){
        return getBuildScale()*0.1*(0.05+((ProteinType)cellData.getType()).getParameter(0))*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*0.1*(0.05+((ProteinType)cellData.getType()).getParameter(0))*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        double[] startPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);
        double[] endPosition = newPositions(endPoint.getX(),endPoint.getY(),cameraX,cameraY,scale);
        double sharpness = 0.05 + 0.25*((ProteinType)cellData.getType()).getParameter(0);
        double baseWidth = (0.2 + getAdjustedSize())*sharpness*scale;
        
        g.setColor(cellData.getType().getColour());
        //g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        double angle = parent.getRealAngle() + radAngle + FastMath.PI/2;
        double xChange = Math.cos(angle)*baseWidth;
        double yChange = FastMath.sin(angle)*baseWidth;
        double[] start = {startPosition[0]-xChange,startPosition[1]-yChange};
        double[] end = {startPosition[0]+xChange,startPosition[1]+yChange};
        int[] xList = {(int)start[0], (int)endPosition[0], (int)end[0]};
        int[] yList = {(int)start[1], (int)endPosition[1], (int)end[1]};
        g.fillPolygon(xList, yList, 3); 
    }
}

