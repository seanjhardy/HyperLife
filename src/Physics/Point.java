/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

import static GUI.GUIManager.getSimulationPanel;
import static GUI.SimulationPanel.FRICTION;
import static GUI.SimulationPanel.GRAVITY;
import java.io.Serializable;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class Point{
    public double x, y, lastX, lastY;
    public double xBuffer, yBuffer;
    
    public Point(){
    }
    
    public Point(double x, double y){
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
    }
    
    public final void simulate(){
        //calculate point velocity and position
        if(getSimulationPanel().getViewModeBool().getValue()){
            lastY -= GRAVITY;
        }
        double vx1 = (x - lastX)*FRICTION;
        double vy1 = (y - lastY)*FRICTION;
        lastX = x;
        lastY = y;
        x += vx1;
        y += vy1;
        x += xBuffer;
        y += yBuffer;
        xBuffer = 0;
        yBuffer = 0;
    } 
    
    public void constrainToPos(Point parentStart, Point parentEnd, double[] target){
        //calculate start point change
        double dx = x - target[0];
        double dy = y - target[1];
        if(FastMath.abs(dx) > 0.001 || FastMath.abs(dy) > 0.001){
            double dist = FastMath.hypot(dx,dy);
            double diff = 0-dist;
            double percent = (diff/dist)*0.5;
            double offsetX = dx*percent;
            double offsetY = dy*percent;
            x += offsetX;
            y += offsetY;
            if(parentStart != null){
                double dist1 = FastMath.hypot(parentStart.getX() - x, parentStart.getY() - y);
                double dist2 = FastMath.hypot(parentEnd.getX() - x, parentEnd.getY() - y);
                double r = dist2/(dist1 + dist2);
                //work out movement by percentage
                parentStart.addXBuffer(-offsetX*r);
                parentStart.addYBuffer(-offsetY*r);
                parentEnd.addXBuffer(-offsetX*(1.0-r));
                parentEnd.addYBuffer(-offsetY*(1.0-r));
            }
        }  
    }
    public void constrainToMap(){
        double width = getSimulationPanel().getXSize();
        double height = getSimulationPanel().getYSize();
        if(x > width/2){
            x = width/2 - 0.01;
            lastX = x + (x - lastX);
        }if(x < -width/2){
            x = -width/2 + 0.01;
            lastX = x + (x - lastX);
        }if(y > height/2){
            y = height/2 - 0.01;
            lastY = y + (y - lastY);
        }if(y < -height/2){
            y = -height/2 + 0.01;
            lastY = y + (y - lastY);
        }
    }
    public static void constrainToLength(Point pointA, Point pointB, double length){
        //constrain distance between points
        double dx = pointA.getX() - pointB.getX();
        double dy = pointA.getY() - pointB.getY();
        double dist = FastMath.hypot(dx,dy);
        double diff = length-dist;
        if(FastMath.abs(diff) > 0.002){
            if(dist == 0) dist = 0.01;
            double percent = (diff/dist)*0.5;
            double offsetX = dx*percent;
            double offsetY = dy*percent;
            pointA.addX(offsetX);
            pointA.addY(offsetY);

            pointB.addX(-offsetX);
            pointB.addY(-offsetY);
        }
    }
    public static void constrainToAngle(Point pointA, Point pointB, double length, double angle, double stiffness){
        if(false){
            length *= 0.5;
            double midX = (pointA.getX() + pointB.getX())*0.5;
            double midY = (pointA.getY() + pointB.getY())*0.5;
            double newX = midX - FastMath.cos(angle)*length;
            double newY = midY - FastMath.sin(angle)*length;
            double newX2 = midX + FastMath.cos(angle)*length;
            double newY2 = midY + FastMath.sin(angle)*length;
            stiffness *= 0.2;
            //System.out.println(pointA.getX() + " " + newX + " " +(newX - pointA.getX())*stiffness);
            pointA.addX((newX - pointA.getX())*stiffness);
            pointA.addY((newY - pointA.getY())*stiffness);
            
            pointB.addX((newX2 - pointB.getX())*stiffness);
            pointB.addY((newY2 - pointB.getY())*stiffness);
            
            stiffness *= 0.8;
            pointA.addLastX((newX - pointA.getLastX())*stiffness);
            pointA.addLastY((newY - pointA.getLastY())*stiffness);
            
            pointB.addLastX((newX2 - pointB.getLastX())*stiffness);
            pointB.addLastY((newY2 - pointB.getLastY())*stiffness);
            
        }else{
            double len = FastMath.hypot(pointA.getY() - pointB.getY(), pointA.getX() - pointB.getX());
            double newX2 = pointA.getX() + FastMath.cos(angle)*len;
            double newY2 = pointA.getY() + FastMath.sin(angle)*len;
            pointB.setLastX(pointB.getLastX() + (newX2 - pointB.getX())*stiffness);
            pointB.setLastY(pointB.getLastY() + (newY2 - pointB.getY())*stiffness);
            pointB.setX(pointB.getX() + (newX2 - pointB.getX())*stiffness);
            pointB.setY(pointB.getY() + (newY2 - pointB.getY())*stiffness);
        }
    }
    
    
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getLastX(){
        return lastX;
    }
    public double getLastY(){
        return lastY;
    }
    public void addX(double x){
        this.x += x;
    }
    public void addY(double y){
        this.y += y;
    }
    public void addXBuffer(double x){
        this.xBuffer += x;
    }
    public void addYBuffer(double y){
        this.yBuffer += y;
    }
    public void addLastX(double x){
        this.lastX += x;
    }
    public void addLastY(double y){
        this.lastY += y;
    }
    public void setX(double value){
        x = value;
    }
    public void setY(double value){
        y = value;
    }
    public void setLastX(double value){
        lastX = value;
    }
    public void setLastY(double value){
        lastY = value;
    }
}

