/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics;

import CellParts.SegmentInstance;
import CellParts.SegmentType;
import GUI.GUIManager;
import static GUI.GUIManager.getScreenSize;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class PhysicsManager {
    //coordinate manipulaton
    public static double[] newPositions(double x, double y, double cameraX, double cameraY, double scale){
        double width = getScreenSize().getWidth();
        double height = getScreenSize().getHeight();
        double newX = ((x - cameraX + (width/(scale*2.0)))*scale);
        double newY = ((y - cameraY + (height/(scale*2.0)))*scale);
        double[] newValues = {newX,newY};
        return newValues;
    }
    
    public static double[] newBlueprintPositions(double x, double y, double cameraX, double cameraY, double scale){
        double width = GUIManager.getSimulationPanel().getSimulationToolbar().getCreatureIconWidth();
        double height = GUIManager.getSimulationPanel().getSimulationToolbar().getCreatureIconHeight();
        double newX = ((x - cameraX + (width/(scale*2.0)))*scale);
        double newY = ((y - cameraY + (height/(scale*2.0)))*scale);
        double[] newValues = {newX,newY};
        return newValues;
    }
    
    public static double[] inversePositions(double x, double y, double cameraX, double cameraY, double scale){
        double width = getScreenSize().getWidth();
        double height = getScreenSize().getHeight();
        double newX = (x/scale) - width/(2.0*scale) + cameraX;
        double newY = (y/scale) - height/(2.0*scale) + cameraY;
        double[] newValues = {newX,newY};
        return newValues;
    }
    
    public static double[] rotatePoint(double x, double y, double axisX, double axisY, double rads){
        double cosR = FastMath.cos(rads);
        double sinR = FastMath.sin(rads);
        double x2 = (x - axisX)*cosR - (y - axisY)*sinR;
        double y2 = (x - axisX)*sinR + (y - axisY)*cosR;
        return new double[]{x2 + axisX, y2 + axisY};
    }
    
    public static double[] rotatePoint(double x, double y, double rads){
        //double[] pt = {x,y};
        //AffineTransform.getRotateInstance(rads).transform(pt, 0, pt, 0, 1); // specifying to use this double[] to hold coords
        //return pt;
        double cosR = FastMath.cos(rads);
        double sinR = FastMath.sin(rads);
        double x2 = x*cosR - y*sinR;
        double y2 = x*sinR + y*cosR;
        return new double[]{x2, y2};
    }
    
    public static double interpolate(double a, double b, double percent){
        double inversePercent = 1 - percent;
        double result = a * inversePercent + b * percent;
        return result;
    }
    
    public static ArrayList<Point2D> lineIntersectsEllipse(double x1, double x2, double y1, double y2, double midX, double midY, double w, double l) {
        ArrayList<Point2D> points = new ArrayList();
        x1 -= midX;
        y1 -= midY;

        x2 -= midX;
        y2 -= midY;
        if (x1 == x2) { 
            double y = (l/w)*FastMath.hypot(w, x1);
            if (FastMath.min(y1, y2) <= y && y <= FastMath.max(y1, y2)) {
                points.add(new Point2D.Double(x1+midX, y+midY));
            }
            if (FastMath.min(y1, y2) <= -y && -y <= FastMath.max(y1, y2)) {
                points.add(new Point2D.Double(x1+midX, -y+midY));
            }
        }
        else {
            double a = (y2 - y1) / (x2 - x1);
            double b = (y1 - a*x1);

            double r = a*a*w*w + l*l;
            double s = 2*a*b*w*w;
            double t = w*w*b*b - w*w*l*l;

            double d = s*s - 4*r*t;

            if (d > 0) {
                double xi1 = (-s+FastMath.sqrt(d))/(2*r);
                double xi2 = (-s-FastMath.sqrt(d))/(2*r);

                double yi1 = a*xi1+b;
                double yi2 = a*xi2+b;

                if (isPointInLine(x1, x2, y1, y2, xi1, yi1)) {
                    points.add(new Point2D.Double(xi1+midX, yi1+midY));
                }
                if (isPointInLine(x1, x2, y1, y2, xi2, yi2)) {
                    points.add(new Point2D.Double(xi2+midX, yi2+midY));
                }
            }
            else if (d == 0) {
                double xi = -s/(2*r);
                double yi = a*xi+b;

                if (isPointInLine(x1, x2, y1, y2, xi, yi)) {
                    points.add(new Point2D.Double(xi+midX, yi+midY));
                }
            }
        }
        return points;
    }
    
    public static ArrayList<Point2D> lineIntersectsSegment(double x1, double x2, double y1, double y2, SegmentInstance node) {
        double nodeAngle = ((SegmentInstance) node).getRealAngle() + Math.PI;
        double w = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getWidth()/2;
        double l = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getLength()/2;
        double nodeX = (((SegmentInstance) node).getStartPoint().getX() + ((SegmentInstance) node).getEndPoint().getX())*0.5;
        double nodeY = (((SegmentInstance) node).getStartPoint().getY() + ((SegmentInstance) node).getEndPoint().getY())*0.5;
        double[] lineStart = rotatePoint(x1, y1, nodeX, nodeY, -nodeAngle);
        double[] lineEnd = rotatePoint(x2, y2, nodeX, nodeY, -nodeAngle);
        return lineIntersectsEllipse(lineStart[0], lineStart[1], lineEnd[0], lineEnd[1], nodeX, nodeY, w, l);
    }
    
    public static boolean pointIntersectsSegment(double x, double y, SegmentInstance node){
        double nodeAngle = ((SegmentInstance) node).getRealAngle() + Math.PI;
        double w = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getWidth()/2;
        double l = ((SegmentType)((SegmentInstance)node).getCellData().getType()).getLength()/2;
        double nodeX = (((SegmentInstance) node).getStartPoint().getX() + ((SegmentInstance) node).getEndPoint().getX())*0.5;
        double nodeY = (((SegmentInstance) node).getStartPoint().getY() + ((SegmentInstance) node).getEndPoint().getY())*0.5;          
        double cosa = FastMath.cos(nodeAngle);
        double sina = FastMath.sin(nodeAngle);
        double dist = FastMath.pow((cosa*(x - nodeX) + sina*(y - nodeY))/l, 2) + 
                      FastMath.pow((sina*(x - nodeX) - cosa*(y - nodeY))/w, 2);
        return dist <= 1;
    }

    public static boolean isPointInLine(double x1, double x2, double y1, double y2, double px, double py) {
        double xMin = FastMath.min(x1, x2);
        double xMax = FastMath.max(x1, x2);

        double yMin = FastMath.min(y1, y2);
        double yMax = FastMath.max(y1, y2);

        return (xMin <= px && px <= xMax) && (yMin <= py && py <= yMax);
    }
    
    public static double[] getProjectionOfSegmentOnLine(SegmentInstance node, double x1, double y1, double x2, double y2){
        double minPercent = 1.0, maxPercent = 0.0;
        double p;
        double w = ((SegmentType)node.getCellData().getType()).getLength()*0.5;
        Point startPoint = node.getStartPoint(), endPoint = ((SegmentInstance)node).getEndPoint();
        double angle = node.getRealAngle()+Math.PI;
        p = projectionOfPointOnLine(startPoint.getX()+FastMath.cos(angle)*w, startPoint.getY()+FastMath.sin(angle)*w, x1, y1, x2, y2);
        minPercent = FastMath.min(minPercent, p); maxPercent = FastMath.max(maxPercent, p);
        p = projectionOfPointOnLine(startPoint.getX()-FastMath.cos(angle)*w, startPoint.getY()-FastMath.sin(angle)*w, x1, y1, x2, y2);
        minPercent = FastMath.min(minPercent, p); maxPercent = FastMath.max(maxPercent, p);
        p = projectionOfPointOnLine(endPoint.getX()+FastMath.cos(angle)*w, endPoint.getY()+FastMath.sin(angle)*w, x1, y1, x2, y2);
        minPercent = FastMath.min(minPercent, p); maxPercent = FastMath.max(maxPercent, p);
        p = projectionOfPointOnLine(endPoint.getX()+FastMath.cos(angle)*w, endPoint.getY()+FastMath.sin(angle)*w, x1, y1, x2, y2);
        minPercent = FastMath.min(minPercent, p); maxPercent = FastMath.max(maxPercent, p);
        return new double[]{minPercent, maxPercent};
    }
    
    public static double projectionOfPointOnLine(double x, double y, double x1, double y1, double x2, double y2){
        return 0.0;
    }
    
    public static double approxArea(PathIterator i) {
        double a = 0.0;
        double[] coords = new double[6];
        double startX = 0, startY = 0;
        Line2D segment = new Line2D.Double(0, 0, 0, 0);
        while (! i.isDone()) {
            int segType = i.currentSegment(coords);
            double x = coords[0], y = coords[1];
            switch (segType) {
            case PathIterator.SEG_CLOSE:
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);
                a += hexArea(segment);
                startX = startY = 0;
                segment.setLine(0, 0, 0, 0);
                break;
            case PathIterator.SEG_LINETO:
                segment.setLine(segment.getX2(), segment.getY2(), x, y);
                a += hexArea(segment);
                break;
            case PathIterator.SEG_MOVETO:
                startX = x;
                startY = y;
                segment.setLine(0, 0, x, y);
                break;
            default:
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            i.next();
        }
        if (Double.isNaN(a)) {
            throw new IllegalArgumentException("PathIterator contains an open path");
        } else {
            return 0.5 * Math.abs(a);
        }
    }
    
    private static double hexArea(Line2D seg) {
        return seg.getX1() * seg.getY2() - seg.getX2() * seg.getY1();
    }
}
