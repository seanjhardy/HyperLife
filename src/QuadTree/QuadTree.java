/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuadTree;

import static GUI.GUIManager.getSimulationPanel;
import static Physics.PhysicsManager.newPositions;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author seanjhardy
 */

public class QuadTree {
    private QuadTree parent = null, quadTreeRoot = null;
    private int capacity, maxDepth;
    private int depth = 0;
    private QuadRect boundary;
    private ArrayList<QuadNode> points = new ArrayList<>();
    private int numPoints = 0;
    private boolean divided = false;
    QuadTree NW;
    QuadTree NE;
    QuadTree SW;
    QuadTree SE;
    
    public QuadTree(QuadTree parentTree, QuadRect bounds, int capacity, int maxDepth){
        this.boundary = bounds;
        this.parent = parentTree;
        this.capacity = capacity;
        this.maxDepth = maxDepth;
        if(parentTree != null){
            this.quadTreeRoot = parentTree.getQuadTreeRoot();
            this.depth = parent.depth+1;
        }else{
            this.quadTreeRoot = null;
        }
    }
    
    public boolean insert(QuadNode P){
        if(!this.boundary.contains(P)){
            return false;
        }
        if(numPoints < capacity){
            this.points.add(P);
            increaseNumPoints();
            P.setParent(this);
            return true;
        }else if(!divided){
            if(depth < maxDepth){
                subdivide();
            }
        }
        if(divided){
            return (NE.insert(P) || NW.insert(P) || SE.insert(P) || SW.insert(P));
        }else{
            this.points.add(P);
            increaseNumPoints();
            P.setParent(this);
            return true;
        }
    }
    private void subdivide(){
        double x = this.boundary.getX();
        double y = this.boundary.getY();
        double width = this.boundary.getWidth()/2;
        double height = this.boundary.getHeight()/2;
        QuadRect ne = new QuadRect(x + width, y - height, width, height);
        this.NE = new QuadTree(this, ne, capacity, maxDepth);
        QuadRect nw = new QuadRect(x - width, y - height, width, height);
        this.NW = new QuadTree(this, nw, capacity, maxDepth);
        QuadRect se = new QuadRect(x + width, y + height, width, height);
        this.SE = new QuadTree(this, se, capacity, maxDepth);
        QuadRect sw = new QuadRect(x - width, y + height, width, height);
        this.SW = new QuadTree(this, sw, capacity, maxDepth);
        divided = true;
        Iterator<QuadNode> pointIterator = points.iterator();
        while(pointIterator.hasNext()){
            decreaseNumPoints();
            QuadNode p = pointIterator.next();
            if(!NE.insert(p)){
                if(!NW.insert(p)){
                    if(!SE.insert(p)){
                        SW.insert(p);
                    }
                }
            }
            pointIterator.remove();
        }
    }
    
    public void recalculatePosition(QuadNode p){
        QuadTree pTree = p.getParentQuadTree();
        if(pTree == null){
            double width = getSimulationPanel().getXSize();
            double height = getSimulationPanel().getYSize();
            if(p.x >= width/2){
                p.x = width/2 - 0.01;
            }if(p.x <= -width/2){
                p.x = -width/2 + 0.01;
            }
            if(p.y >= height/2){
                p.y = height/2 - 0.01;
            }if(p.y <= -height/2){
                p.y = -height/2 + 0.01;
            }
            getQuadTreeRoot().insert(p);
            pTree = p.getParentQuadTree();
        }
        if(pTree.isDivided()){
            pTree.removeQuadNode(p);
            getQuadTreeRoot().insert(p);
            pTree = p.getParentQuadTree();
        }
        if(pTree.parent != null){
            if(!pTree.parent.isDivided()){
                pTree.removeQuadNode(p);
                getQuadTreeRoot().insert(p);
                pTree = p.getParentQuadTree();
            }
        }
        if(!pTree.boundary.contains(p)){
            pTree.removeQuadNode(p);
            getQuadTreeRoot().insert(p);
        }
    }
    
    public void recalibrate(){
        if(divided){
            NE.recalibrate(); 
            NW.recalibrate(); 
            SE.recalibrate(); 
            SW.recalibrate();
            if(numPoints < capacity){
                this.divided = false;
                NE = null;
                NW = null;
                SE = null;
                SW = null;
            }
        }
    }
    
    public ArrayList<QuadNode> query(QuadObject range, ArrayList<QuadNode> found){
        if(!range.intersects(this.boundary)){
            return found;
        }
        if(!divided){
            for(QuadNode P : this.points){
                if(range.contains(P)){
                    found.add(P);
                }
            }
        }else{
            this.NE.query(range,found);
            this.NW.query(range,found);
            this.SE.query(range,found);
            this.SW.query(range,found);
        }
        return found;
    }
    
    public void removeQuadNode(QuadNode p){
        points.remove(p);
        p.parentQuadTree = null;
        numPoints -= 1;
        if(parent != null){
            parent.decreaseNumPoints();
        }
    }
    
    private void decreaseNumPoints(){
        numPoints -= 1;
        if(parent != null){
            parent.decreaseNumPoints();
        }
    }
    
    private void increaseNumPoints(){
        numPoints += 1;
        if(parent != null){
            parent.increaseNumPoints();
        }
    }
    
    public void draw(Graphics2D g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        if(divided){
            NW.draw(g);
            NE.draw(g);
            SW.draw(g);
            SE.draw(g);
        }else{
            double x = this.boundary.getX();
            double y = this.boundary.getY();
            double width = this.boundary.getWidth();
            double height = this.boundary.getHeight();
            g.setColor(new Color(0,255,0,255));
            double[] newCoords = newPositions(x,y, cameraX, cameraY, scale);
            g.setStroke(new BasicStroke((float) (0.02*scale)));
            g.drawRect((int)(newCoords[0]-width*scale),(int)(newCoords[1]-height*scale),(int)(width*2*scale),(int)(height*2*scale));
            g.setStroke(new BasicStroke(1));
            //g.drawString("" +numPoints, (int)newCoords[0], (int)newCoords[1]);

        }
    }
    public int getNumPoints(){
        return numPoints;
    }
    public boolean isDivided(){
        return divided;
    }
    public QuadTree getQuadTreeRoot(){
        if(quadTreeRoot == null){
            return this;
        }else{
            return quadTreeRoot;
        }
    }
    
}
