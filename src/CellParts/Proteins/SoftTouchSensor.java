/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CellParts.Proteins;

import CellParts.CellPartData;
import Physics.Point;
import static Physics.Point.constrainToAngle;
import static Physics.Point.constrainToLength;
import CellParts.ProteinInstance;
import CellParts.ProteinType;
import CellParts.SegmentInstance;
import CellParts.SegmentType;
import GUI.GUIManager;
import static GUI.GUIManager.addAlpha;
import static GUI.GUIManager.brightness;
import static GUI.GUIManager.getSimulationPanel;
import static GUI.GUIManager.interpolate;
import GeneticAlgorithm.Creature;
import static GeneticAlgorithm.Creature.getBuildScale;
import static Physics.PhysicsManager.newBlueprintPositions;
import static Physics.PhysicsManager.newPositions;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class SoftTouchSensor extends ProteinInstance{
    
    private ArrayList<Point> points = new ArrayList<>();
    private SegmentInstance overlapping;
    
    public SoftTouchSensor(Creature creature, CellPartData type, SegmentInstance parent) {
        super(creature, type, parent);
        double[] start = parent.getPointAtAngle(getAdjustedAngleOnBody());
        startPoint.setX(start[0]);
        startPoint.setY(start[1]);
        startPoint.lastX = startPoint.x;
        startPoint.lastY = startPoint.y;
        double parentAngle = parent.getRealAngle();
        double a = (parentAngle + radAngle);
            
        double length = 0.1 + 4*getAdjustedSize();
           
        double xChange = FastMath.cos(a)*length;
        double yChange = FastMath.sin(a)*length;
        for(int i = 0; i < 8; i++){
            points.add(new Point(startPoint.getX() + xChange*(i+1), startPoint.getY() + yChange*(i+1)));
        }
        creature.addInput(this);
    } 
    
    @Override
    public void simulate(){
        super.simulate();
        double[] start = getRotatedPoint();
        startPoint.simulate();
        startPoint.constrainToPos(null, null, start);
        
        double length = 0.1 + 4*getAdjustedSize();
            
        Point lastPoint = startPoint;
        for(int i = 0; i < points.size(); i++){
            startPoint.constrainToPos(null, null, start);
            Point point = points.get(i);
            point.simulate();
            constrainToLength(point, lastPoint, length);

            double targetAngle = (parent.getRealAngle() + radAngle);
            realAngle = FastMath.atan2(startPoint.getY() - points.get(i).getY(), startPoint.getX() - points.get(i).getX());
            double difference = FastMath.abs(targetAngle - realAngle)*getSimulationPanel().getSpeed();
            constrainToAngle(lastPoint, point, length*0.5, targetAngle, 0.0001*difference);
            lastPoint = point;
            point.constrainToMap();
        }
        checkForOverlap();
        parent.setBounds(startPoint.getX(), startPoint.getY());
        parent.setBounds(points.get(points.size()-1).getX(), points.get(points.size()-1).getY());
    }
    
    public void checkForOverlap(){
        QuadRect range = new QuadRect(points.get(points.size()-1).getX(),points.get(points.size()-1).getY(), 1.25, 1.25);
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        overlapping = null;
        for(QuadNode node : segments){
            if(node instanceof SegmentInstance){
                if(((SegmentInstance)node).getCreature() != this.getCreature()){
                    double a = ((SegmentInstance) node).getRealAngle();
                    double cosa = FastMath.cos(a);
                    double sina = FastMath.sin(a);
                    double w = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getWidth()/2;
                    double l = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getLength()/2;
                    double nodeX = (((SegmentInstance) node).getStartPoint().getX() + ((SegmentInstance) node).getEndPoint().getX())*0.5;
                    double nodeY = (((SegmentInstance) node).getStartPoint().getY() + ((SegmentInstance) node).getEndPoint().getY())*0.5;
                    double dist = FastMath.pow((cosa*(points.get(points.size()-1).getX() - nodeX) + sina*(points.get(points.size()-1).getY() - nodeY))/(l), 2) + 
                                  FastMath.pow((sina*(points.get(points.size()-1).getX() - nodeX) + cosa*(points.get(points.size()-1).getY() - nodeY))/(w), 2);
                    if(dist <= 1){
                        overlapping = (SegmentInstance) node;
                    }
                }
            }
        }
    }
    
    public double getBuildCost(){
        return getBuildScale()*0.1*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    public double getEnergyContent(){
        return getBuildScale()*0.1*((ProteinType)cellData.getType()).getSize()*parent.getSize();
    }
    
    @Override
    public void calibrateBlueprint(Creature blueprintCreature){
        blueprintAngle = parent.getBlueprintAngle() + realAngle - parent.getRealAngle();
        double[] startPos = parent.getBlueprintPointAtAngle((getAdjustedAngleOnBody()));
        blueprintStartX = startPos[0];
        blueprintStartY = startPos[1];
        creature.setIconBounds(startPos[0], startPos[1]);
    }
    
    @Override
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        //draw startPoint
        double[] lastPosition = newPositions(startPoint.getX(),startPoint.getY(),cameraX,cameraY,scale);
        double totalSize = (0.02 + 0.2*getAdjustedSize())*scale;
        
        Color endColour = addAlpha(brightness(cellData.getType().getColour(), 0.5), 200);
        Color startColour = addAlpha(brightness(cellData.getType().getColour(), 1.5), 200);
        g.setColor(startColour);
        g.fillOval((int)(lastPosition[0] - totalSize), (int)(lastPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        double[] newPosition = {0,0};
        for(int p = 0; p < points.size(); p++){
            Point point = points.get(p);
            newPosition = newPositions(point.getX(),point.getY(),cameraX,cameraY,scale);
            g.setStroke(new BasicStroke((float) (totalSize*(1 - ((double)p/points.size())*0.5))));  
            g.setColor(interpolate(startColour, endColour, (double)p/points.size()));
            g.drawLine((int)(lastPosition[0]), (int)(lastPosition[1]), (int)newPosition[0], (int)newPosition[1]);
            lastPosition = newPosition;
        }
        g.setColor(endColour);
        g.fillOval((int)(newPosition[0] - totalSize), (int)(newPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        
        g.setStroke(new BasicStroke(1));
    }
    
    @Override
    public void drawBlueprint(Graphics2D g, double cameraX, double cameraY, double scale){
        //draw startPoint
        double[] originalPosition = newBlueprintPositions(blueprintStartX,blueprintStartY,cameraX,cameraY,scale);
        double[] lastPosition = originalPosition;
        double totalSize = (0.02 + 0.2*getAdjustedSize())*scale;
        
        Color endColour = brightness(cellData.getType().getColour(), 0.5);
        endColour = GUIManager.addAlpha(endColour, 100);
        Color startColour = brightness(cellData.getType().getColour(), 1);
        startColour = GUIManager.addAlpha(startColour, 100);
        g.setColor(startColour);
        g.fillOval((int)(lastPosition[0] - totalSize), (int)(lastPosition[1] - totalSize), (int)(totalSize*2), (int)(totalSize*2));
        
        AffineTransform tx = new AffineTransform();
        tx.rotate(FastMath.toRadians(creature.getAngle()), originalPosition[0], originalPosition[1]);
            
        g.setStroke(new BasicStroke((float) (totalSize), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4f}, 0));  
        double[] newPosition = {0,0};
        for(int p = 0; p < points.size(); p++){
            Point point = points.get(p);
            newPosition = newBlueprintPositions((point.getX()-startPoint.getX()) + blueprintStartX,
                    (point.getY()-startPoint.getY()) + blueprintStartY,cameraX,cameraY,scale);
            g.setColor(interpolate(startColour, endColour, (double)p/points.size()));
            
            Line2D line = new Line2D.Double((int)(lastPosition[0]), (int)(lastPosition[1]), (int)newPosition[0], (int)newPosition[1]);
            Shape newShape = tx.createTransformedShape(line);
            g.draw(newShape);
            lastPosition = newPosition;
        }
        g.setStroke(new BasicStroke(1));
    }
}
