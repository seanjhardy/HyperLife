/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuadTree;

import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class QuadLine implements QuadObject{
    private double startX, startY, endX, endY;
    
    public QuadLine(double startX, double startY, double endX, double endY){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }
    
    @Override
    public double getX() {
        return startX;
    }

    @Override
    public double getY() {
        return startY;
    }

    @Override
    public boolean contains(QuadNode p) {
        return true;
    }

    @Override
    public boolean intersects(QuadRect r) {
        boolean left = checkLineIntersection(startX, startY, endX, endY, 
                                            r.getX()-r.getWidth(),r.getY()-r.getHeight(),
                                            r.getX()-r.getWidth(),r.getY()+r.getHeight());
        if(left) return true;
        boolean right = checkLineIntersection(startX, startY, endX, endY, 
                                            r.getX()+r.getWidth(),r.getY()-r.getHeight(),
                                            r.getX()+r.getWidth(),r.getY()+r.getHeight());
        if(right) return true;
        boolean top = checkLineIntersection(startX, startY, endX, endY, 
                                            r.getX()-r.getWidth(),r.getY()-r.getHeight(),
                                            r.getX()+r.getWidth(),r.getY()-r.getHeight());
        if(top) return true;
        boolean bottom = checkLineIntersection(startX, startY, endX, endY, 
                                            r.getX()-r.getWidth(),r.getY()+r.getHeight(),
                                            r.getX()+r.getWidth(),r.getY()+r.getHeight());
        return bottom;
    }
    
    public boolean checkLineIntersection(double x1, double y1, double x2, double y2, 
                                         double x3, double y3, double x4, double y4) {
        // calculate the direction of the lines
        double uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
        double uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
        // if uA and uB are between 0-1, lines are colliding
        return (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1);
      }
    
}
