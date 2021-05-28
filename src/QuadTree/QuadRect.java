/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuadTree;

import static GUI.GUIManager.getSimulationPanel;
import static Physics.PhysicsManager.newPositions;
import java.awt.Graphics;

/**
 *
 * @author seanjhardy
 */
public class QuadRect implements QuadObject{
    private final double x,y,width,height;
    
    public QuadRect(double x, double y, double width, double height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getWidth(){
        return width;
    }
    public double getHeight(){
        return height;
    }
    
    @Override
    public boolean contains(QuadNode P){
        return (P.getX() >= x - width && P.getY() >= y - height && P.getX() <= x + width && P.getY() <= y + height);
    }
    
    @Override
    public boolean intersects(QuadRect range){
        return !(range.getX() - range.getWidth() > this.x + this.width ||
          range.getX() + range.getWidth() < this.x - this.width ||
          range.getY() - range.getHeight() > this.y + this.height ||
          range.getY() + range.getHeight() < this.y - this.height);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }
    
    public void draw(Graphics g){
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        
        double[] AA = newPositions(x-width/2,y-height/2, cameraX, cameraY, scale);
        double[] BB = newPositions(x+width/2,y+height/2, cameraX, cameraY, scale);
        g.drawRect((int)AA[0],(int)AA[1],(int)(BB[0]-AA[0]),(int)(BB[1]-AA[1]));
    }
}
