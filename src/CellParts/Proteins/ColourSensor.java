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
import CellParts.SegmentType;
import static GUI.GUIManager.getSimulationPanel;
import static GUI.GUIManager.interpolate;
import GeneticAlgorithm.Creature;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import net.jafama.FastMath;
import static GeneticAlgorithm.Creature.getBuildScale;
import static Physics.PhysicsManager.lineIntersectsEllipse;
import static Physics.PhysicsManager.newPositions;
import static Physics.PhysicsManager.rotatePoint;
import static Physics.PhysicsManager.rotatePoint;

/**
 *
 * @author seanjhardy
 */
public class ColourSensor extends ProteinInstance{
    
    private Color colour;
    
    public ColourSensor(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        creature.addInput(this);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] point = getRotatedPoint();
        startPoint.setX(point[0]);
        startPoint.setY(point[1]);
        if(getSimulationPanel().getGlobalStep() % 10 == 0){
            getLightLevel();
        }
    }
    
    public void getLightLevel(){
        double viewDistance = (0.5 + 20*getAdjustedSize());
        double parentAngle = parent.getRealAngle();
        double angle = parentAngle + radAngle;
        double endPointX = startPoint.getX() + FastMath.cos(angle)*viewDistance;
        double endPointY = startPoint.getY() + FastMath.sin(angle)*viewDistance;
        
        QuadRect range = new QuadRect((startPoint.getX() + endPointX)*0.5, 
                (startPoint.getY() + endPointY*0.5), 
                FastMath.abs(startPoint.getX() - endPointX)*0.5 + 1.25, 
                FastMath.abs(startPoint.getY() - endPointY)*0.5 + 1.25);
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        colour = new Color(0,0,0);
        double minDist = -1;
        for(QuadNode node : segments){
            if(node instanceof SegmentInstance){
                if(((SegmentInstance)node).getCreature() != this.getCreature()){
                    double a = ((SegmentInstance)node).getRealAngle();
                    double w = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getWidth()*((SegmentInstance) node).getSize()/2;
                    double l = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getLength()*((SegmentInstance) node).getSize()/2;
                    double nodeX = (((SegmentInstance)node).getStartPoint().getX() + ((SegmentInstance) node).getEndPoint().getX())*0.5;
                    double nodeY = (((SegmentInstance)node).getStartPoint().getY() + ((SegmentInstance) node).getEndPoint().getY())*0.5;
                    
                    double[] start = rotatePoint(startPoint.getX(), startPoint.getY(), nodeX, nodeY, -a);
                    double[] end = rotatePoint(endPointX, endPointY, nodeX, nodeY, -a);
                    ArrayList<Point2D> intersections = lineIntersectsEllipse(start[0],end[0],start[1],end[1], nodeX, nodeY, l, w);
                    for(Point2D point : intersections){
                        double dist = FastMath.hypot(startPoint.getX() - point.getX(), startPoint.getY() - point.getY());
                        if(dist < minDist || minDist == -1 && dist < viewDistance){
                            minDist = dist;
                            colour = interpolate(new Color(0,0,0), ((SegmentInstance)node).getCellData().getType().getColour(), 1 - (dist/viewDistance));
                        }
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
        
        double[] startPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);

        if(getSimulationPanel().getSelectedSegment() != null && 
           creature == getSimulationPanel().getSelectedSegment().getCreature()){
            double viewDistance = (0.5 + 20*getAdjustedSize());
            double angle = parent.getRealAngle() + radAngle;
            double endPointX = startPoint.getX() + FastMath.cos(angle)*viewDistance;
            double endPointY = startPoint.getY() + FastMath.sin(angle)*viewDistance;
            double[] end = newPositions(endPointX, endPointY, cameraX,cameraY,scale);
            Line2D range = new Line2D.Double(startPosition[0],startPosition[1], end[0],end[1]);

            g.setColor(new Color(255,255,255,100));
            g.setStroke(new BasicStroke((float) (0.03*scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{(float)(9)}, 0));
            g.draw(range);
            g.setStroke(new BasicStroke(1));
        }
        
        double totalSize = (0.01 + 0.3*getAdjustedSize())*scale;
        
        g.setColor(colour);
        if(scale > 50){
            g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }else{
            g.fillRect((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }
        g.setColor(new Color(100,100,100));
        if(scale > 50){
            g.drawOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }else{
            g.drawRect((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }
        totalSize *= 0.5;
        
        g.setColor(cellData.getType().getColour());
        if(scale > 50){
            g.fillOval((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }else{
            g.fillRect((int)(startPosition[0] - totalSize), (int)(startPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        }
    }
}
