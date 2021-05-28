/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuadTree;

/**
 *
 * @author seanjhardy
 */
public class QuadNode {
    protected QuadTree parentQuadTree;
    protected double x,y;
    
    public void setParent(QuadTree parent){
        this.parentQuadTree = parent;
    }
    
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public QuadTree getParentQuadTree(){
        return parentQuadTree;
    }
    
    
    public void setX(double x){
        this.x = x;
    }
    public void setY(double y){
        this.y = y;
    }
    public void addX(double x){
        this.x += x;
    }
    public void addY(double y){
        this.y += y;
    }
    public void setParentQuadTree(QuadTree t){
        parentQuadTree = t;
    }
    
    
}
