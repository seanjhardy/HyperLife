/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneticAlgorithm;

import File.SerialisableImage;
import CellParts.CellPartData;
import CellParts.SegmentType;
import CellParts.SegmentInstance;
import CellParts.CellPart;
import CellParts.CellPartInstance;
import CellParts.ProteinInstance;
import CellParts.ProteinType;
import CellParts.Proteins.Gametangium;
import static GUI.GUIManager.format;
import static GUI.GUIManager.getSimulationPanel;
import static HyperLife.HyperLife.getGA;
import static HyperLife.HyperLife.getRand;
import NeuralNetwork.NeuralNetwork;
import static Physics.PhysicsManager.getProjectionOfSegmentOnLine;
import Physics.Point;
import Physics.Signal;
import QuadTree.QuadNode;
import QuadTree.QuadRect;
import QuadTree.QuadTree;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.JLabel;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class Creature extends QuadNode{
    
    private int index;
    private HashMap<Integer, String> genome;
    private HashMap<Integer, CellPart> cellParts = new HashMap<>();
    private ArrayList<CellPartInstance> cellPartInstances = new ArrayList<>();
    private ArrayList<Gametangium> gametangii = new ArrayList<>();
    private SerialisableImage icon = new SerialisableImage();
    private SegmentInstance head; 
    private Species species = null;
    private double overlapPercent = 0.0;
    private boolean deleted = false;
    private int updateSpecies = 3;
    
    private static final int HEADER_SIZE = 26, CELL_DATA_SIZE = 10, BUILD_COST_SCALE = 5;
    private boolean reproductionType = false;
    private double size, angle;
    private double randomTime = 100.0;
    private double energy = 0, growthEnergy = 0, currentGrowthEnergy = 0, growthPriority = 0, regenerationFraction = 0, childEnergy = 0;
    private int symmetryType = 0;// no symmetry, global symmetry, local symmetry, radial symmetry
    private int children = 0;
    
    private double lastIconMaxX = 1, lastIconMaxY = 1, lastIconMinX = -1, lastIconMinY = -1;
    private double iconMaxX, iconMaxY, iconMinX, iconMinY;
    
    //neural network
    private ArrayList<ProteinInstance> inputParts = new ArrayList<>();
    private ArrayList<CellPartInstance> outputParts = new ArrayList<>();
    private NeuralNetwork neuralNetwork;
    private double networkMutationProbability = 0.05;
    private int[] networkLayers = {50, 100, 50};
    private double sinVal = 0;
    
            
    public Creature(HashMap<Integer, String> genome, double x, double y){
        index = getGA().nextCreatureID();
        this.genome = genome;
        this.x = x;
        this.y = y;
        readDNA();
    }
    
    protected Creature createClone(){
        HashMap<Integer, String> copiedGenome = (HashMap<Integer, String>) genome.clone();
        Creature clone = new Creature(copiedGenome, x,y);
        if(neuralNetwork != null){
            clone.neuralNetwork = neuralNetwork.copy();
        }
        double energyChange = energy*childEnergy;
        energy -= energyChange;
        clone.energy = energyChange;
        clone.currentGrowthEnergy = 0;
        
        //reading the dna
        clone.readDNA();
        return clone; 
    }
    
    public final void readDNA(){
        cellParts = new HashMap<>();
        //read header genome for global variables
        String header = genome.get(0);
        symmetryType = getBase(header, 0);
        reproductionType = getBase(header, 1) > 2;
        size = FastMath.max(getBaseRange(header, 2, 4)*5,0.5);
        growthEnergy = FastMath.max(getBaseRange(header, 5, 7)*100,5);
        if(growthEnergy > 80 && growthEnergy < 90){
            growthEnergy += 25;
        }else if(growthEnergy < 95){
            growthEnergy += 100;
        }else{
            growthEnergy += 100;
        }
        growthPriority = FastMath.max(getBaseRange(header, 8, 10),0.1);
        childEnergy = FastMath.max(getBaseRange(header, 11, 13)*0.9, 0.05);
        regenerationFraction = FastMath.min(getBaseRange(header, 14, 16), 0.9);
        
        //read segment genomes
        for (Map.Entry<Integer, String> entry : genome.entrySet()) {
            int innovationNumber = entry.getKey();
            if(innovationNumber != 0){
                String chromosome = entry.getValue();
                boolean isSegment = (getBase(chromosome, 0) > 1 || innovationNumber == 1);
                if(isSegment){
                    SegmentType cellPart = new SegmentType(this, innovationNumber);
                    int R = (int)(getBaseRange(chromosome, 1, 4)*255);
                    int G = (int)(getBaseRange(chromosome, 5, 8)*255);
                    int B = (int)(getBaseRange(chromosome, 9, 12)*255);
                    Color colour = new Color(R,G,B);

                    cellPart.setColour(colour);
                    cellPart.setWidth(FastMath.max(getBaseRange(chromosome, 13, 15)*size, 0.05));
                    cellPart.setLength(FastMath.max(getBaseRange(chromosome, 16, 18)*size, 0.05));
                    cellPart.setBoneDensity(getBase(chromosome, 19));
                    cellPart.setMuscle(getBase(chromosome, 20) > 1);
                    cellPart.setNerve(getBase(chromosome, 21) > 1);
                    cellPart.setFat(getBase(chromosome, 22) > 1);
                    cellPart.setMuscleStrength(getBaseRange(chromosome, 23, 25));
                    cellParts.put(innovationNumber, cellPart);
                }else{
                    ProteinType cellPart = new ProteinType(this, innovationNumber);
                    int R = (int)(getBaseRange(chromosome, 1, 4)*255);
                    int G = (int)(getBaseRange(chromosome, 5, 8)*255);
                    int B = (int)(getBaseRange(chromosome, 9, 12)*255);
                    Color colour = new Color(R,G,B);
                    cellPart.setColour(colour);
                    cellPart.setType((int)(getBaseRange(chromosome, 13, 15)*64));
                    cellPart.setSize(FastMath.max(getBaseRange(chromosome, 16, 18)*size, 0.05));
                    for(int b = 19; b + 2 < chromosome.length(); b += 3){
                        cellPart.addParameter(getBaseRange(chromosome, b, b+2));
                    }
                    cellParts.put(innovationNumber, cellPart); 
                }
            }
        }
        
        for (Map.Entry<Integer, String> entry : genome.entrySet()) {
            int innovationNumber = entry.getKey();
            if(innovationNumber != 0){
                String chromosome = entry.getValue();
                for(int b = HEADER_SIZE; b + CELL_DATA_SIZE-1 < chromosome.length(); b += CELL_DATA_SIZE){
                    //part to add
                    CellPart partToBuildFrom = cellParts.get(innovationNumber);

                    int partID = (int)(getBaseRange(chromosome, b, b+2)*64);
                    int buildPriority = (int)(getBaseRange(chromosome, b+3, b+5)*64);
                    int angleOnBody = (int)(getBaseRange(chromosome, b+6, b+7)*360);
                    int angleFromBody = (int)(getBaseRange(chromosome, b+8, b+9)*60 - 30);
                    if(cellParts.get(partID) != null && partToBuildFrom != null && partToBuildFrom instanceof SegmentType){
                        boolean validAngle = true;
                        for(CellPartData data : ((SegmentType)partToBuildFrom).getChildren()){
                            if(data.getAngleOnBody() == angleOnBody){
                                validAngle = false;
                            }
                        }
                        if(validAngle){
                            CellPart partToAdd = cellParts.get(partID);
                            CellPartData cellPartData = new CellPartData(partToAdd, false, buildPriority, angleOnBody, angleFromBody);
                            //if the parent is a segment (can be connected to)
                            ((SegmentType)partToBuildFrom).addChild(cellPartData);
                            if(!(angleOnBody == 180 && angleFromBody == 0) && !(angleOnBody == 0 && angleFromBody == 0)){
                                if(symmetryType > 0 ){//global symmetry
                                    CellPartData symmetricalPartData = new CellPartData(partToAdd, true, buildPriority, 360-angleOnBody, -angleFromBody);
                                    ((SegmentType)partToBuildFrom).addChild(symmetricalPartData);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(head != null){
            head.killSegment();
            getGA().removeSegment(head);
            head = null;
        }
        boolean isSegment = false;
        for (Map.Entry<Integer, String> entry : genome.entrySet()) {
            if(!isSegment){
                int innovationNumber = entry.getKey();
                if(cellParts.get(innovationNumber) instanceof SegmentType){
                    CellPartData data = new CellPartData(cellParts.get(innovationNumber), false, 100, 0, 0);
                    head = new SegmentInstance(this, data, null);
                    head.setSize(0.4);
                    head.setCentred(true);
                    isSegment = true;
                }
            }
        }
    }
    
    public void simulate(){
        if(getSimulationPanel().getGlobalStep() % 10 == 0){
            calculateOverlapPercent();
        }
        x = head.getX();
        y = head.getY();
        //simulate electricity
        randomTime -= 1*getSimulationPanel().getSpeed();
        if(randomTime <= 0){
            boolean result = grow();
            if(result == false){
                randomTime = 5;
            }else{
                randomTime = 50;
            }
            if(outputParts.size() >= 2){
                if(neuralNetwork == null){
                    neuralNetwork = new NeuralNetwork(networkLayers);
                }
                double[] input = getInputs();
                double[] outputs = neuralNetwork.guess(input);
                for(int i = 0; i < outputs.length; i++){
                    if(i < outputParts.size()){
                        CellPartInstance target = outputParts.get(i);
                        Queue<CellPartInstance> targets = new LinkedList<>();
                        targets.add(target);
                        while(target.getParent() != null){
                            target = target.getParent();
                            targets.add(target);
                        }
                        Collections.reverse((List<CellPartInstance>) targets);
                        targets.poll();
                        head.addSignal(new Signal(outputs[i], false, targets));
                    }
                }
            }
        }
    }
    
    public double[] getInputs(){
        sinVal += 0.05;
        double[] inputs = new double[50];
        inputs[0] = 1;
        inputs[1] = -1;
        inputs[2] = 0;
        inputs[3] = FastMath.sin(sinVal);
        inputs[4] = FastMath.cos(sinVal);
        inputs[5] = FastMath.sin(sinVal*2);
        for(int i = 0; i < inputParts.size(); i++){
            if(i+5 < 50){
                inputs[5+i] = inputParts.get(i).getInput();
            }
        }
        return inputs;
    }
    
    public boolean grow(){
        if(currentGrowthEnergy < growthEnergy && head != null){
            return head.grow();
        }
        return false;
    }
    
    public BufferedImage createIcon(){
        int width = getSimulationPanel().getSimulationToolbar().getCreatureIconWidth();
        int height = getSimulationPanel().getSimulationToolbar().getCreatureIconHeight();
        icon.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.image.createGraphics();
        g.setColor(new Color(0,0,0));
        g.fillRect(0,0,width,height);
        iconMinX = -1; iconMaxX = 1; iconMinY = -1; iconMaxY = 1;
        head.calibrateBlueprint(this);
        lastIconMinX = lastIconMinX*0.99 + iconMinX*0.01;
        lastIconMaxX = lastIconMaxX*0.99 + iconMaxX*0.01;
        lastIconMinY = lastIconMinY*0.99 + iconMinY*0.01;
        lastIconMaxY = lastIconMaxY*0.99 + iconMaxY*0.01;
        
        double camX = (lastIconMaxX + lastIconMinX)*0.5;
        double camY = (lastIconMaxY + lastIconMinY)*0.5;
        double scale = FastMath.abs(FastMath.min(width/(lastIconMaxX - lastIconMinX), height/(lastIconMaxY - lastIconMinY)));
        head.drawBlueprint(g, camX, camY, scale);
        g.dispose();
        return icon.image;
    }
    
    public void updateCreatureData(){
        String data = "<html>";
        data += "Species: " + (species.getName().substring(0, 1).toUpperCase() + species.getName().substring(1)) + "<br>";
        data += (reproductionType ? "Sexual" : "Asexual") + "<br>";
        data += "Energy: " + format(energy) + "<br>";
        data += "Children: " + children + "<br>";
        data += "Inputs: " + inputParts.size() + "<br>";
        data += "Outputs: " + outputParts.size() + "<br>";
        data += "</html>";
        ((JLabel)getSimulationPanel().getSimulationToolbar().getComponent("creatureData")).setText(data);
    }
    
    public void removeFromQuadTree(){
        if(head != null){
            head.removeFromQuadTree();
        }
    }
    
    public void insertIntoQuadTree(QuadTree quadTree){
        if(head != null){
            head.insertIntoQuadTree(quadTree);
        }
    }
    
    public void calculateOverlapPercent(){
        QuadRect range = new QuadRect(x, y, 2.5, 2.5);
        overlapPercent = 0;
        ArrayList<QuadNode> segments = getSimulationPanel().getQuadTree().query(range, new ArrayList<>());
        for(QuadNode node : segments){
            if(node instanceof SegmentInstance){
                if(((SegmentInstance) node).getCreature() != null){
                    if(((SegmentInstance) node).getCreature().getIndex() <= index){
                        if(((SegmentInstance)node).getCreature() != this){
                            double distance = FastMath.hypot(x-((SegmentInstance) node).getStartPoint().getX(), y - ((SegmentInstance) node).getStartPoint().getY());
                            double s = ((SegmentType)((SegmentInstance) node).getCellData().getType()).getLength()*
                                    ((SegmentType)((SegmentInstance) node).getCellData().getType()).getWidth()*
                                    ((SegmentInstance) node).getCreature().getSize();
                            overlapPercent += 0.05*s*(2.5/distance);
                        }
                    }
                }
            }
        }
        overlapPercent = FastMath.min(overlapPercent, 0.8);
    }
    
    public void calculateDrag(){
        double x = head.getX();
        double y = head.getY();
        double lastX = (head.getStartPoint().getLastX() + head.getEndPoint().getLastX())*0.5;
        double lastY = (head.getStartPoint().getLastX() + head.getEndPoint().getLastX())*0.5;
        double xVel = x - lastX;
        double yVel = y - lastY;
        double angle = FastMath.atan2(y,x)+FastMath.PI;
        
        double startX = x + FastMath.cos(angle)*100;
        double startY = y + FastMath.sin(angle)*100;
        double endX = x - FastMath.cos(angle)*100;
        double endY = y - FastMath.sin(angle)*100;
        
        double minPercent = 1.0, maxPercent = 0.0;
        for(CellPartInstance part : cellPartInstances){
            if(part instanceof SegmentInstance){
                double[] p = getProjectionOfSegmentOnLine((SegmentInstance)part, startX, startY, endX, endY);
                minPercent = FastMath.min(minPercent, p[0]); maxPercent = FastMath.max(maxPercent, p[1]);
            }
        }
        double dragStartX = startX + minPercent*(endX - startX);
        double dragStartY = startY + minPercent*(endY - startY);
        
        double dragEndX = startX + maxPercent*(endX - startX);
        double dragEndY = startY + maxPercent*(endY - startY);
        
        double randomOffset = getRand().nextDouble();
        double numDragSamples = 7;
        for(int i = 0; i < numDragSamples; i++){
            double sampleX = dragStartX + (((double)i + randomOffset)/numDragSamples)*(dragEndX - dragStartX);
            double sampleY = dragStartY + (((double)i + randomOffset)/numDragSamples)*(dragEndY - dragStartY);
        }
    }
    
    //calculate genetic difference between creatures
    public static double getCompatibility(Creature creature1, Creature creature2, double geneDifferenceScalar, double baseDifferenceScalar) {
        int geneDiff = 0;
        int baseDiff = 0;

        ArrayList<Integer> geneKeys1 = sortGenes(creature1.getGenome().keySet());
        ArrayList<Integer> geneKeys2 = sortGenes(creature2.getGenome().keySet());

        int highestInnovation1 = geneKeys1.get(geneKeys1.size()-1);
        int highestInnovation2 = geneKeys2.get(geneKeys2.size()-1);
        int indices = FastMath.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {
            String node1 = creature1.getGenome().get(i);
            String node2 = creature2.getGenome().get(i);
            if (node1 == null && node2 != null) {
                geneDiff++;
            } else if (node2 == null && node1 != null) {
                geneDiff++;
            }else if(node2 != null && node1 != null){
                baseDiff += compareGeneBases(creature1.getGenome().get(i), creature2.getGenome().get(i));
            }
        }
        
        return geneDiff * geneDifferenceScalar + baseDiff * baseDifferenceScalar;
    }
    
    public static int compareGeneBases(String gene1, String gene2){
        int baseDiff = 0;
        int maxLength = FastMath.max(gene1.length(), gene2.length());
        for(int i = 0; i < maxLength; i++){
            int base1 = getBase(gene1, i);
            int base2 = getBase(gene2, i);
            if(base1 == -1 || base2 == -1){//if either gene has excess bases
                baseDiff += 1;
            }else if(base1 != base2){
                baseDiff += 1;
            }
        }
        return baseDiff;
    }
    
    //sort gene innovation numbers by ascending order
    private static ArrayList<Integer> sortGenes(Collection<Integer> c) {
        ArrayList<Integer> array = new ArrayList<>();
        array.addAll(c);
        java.util.Collections.sort(array);
        return array;
    }
    
    //genome getters
    public static int getBase(String gene, int base){
        if(base < gene.length()){
            return Integer.parseInt(gene.substring(base,base+1));
        }
        return -1;
    }
    public static double getBaseRange(String gene, int start, int end){
        double result = 0;
        //inclusive
        for(int i = start; i <= end; i++){
            if(i < gene.length()){
                //increase accuracy of value as the number of bases increases
                result += (double)getBase(gene, i)*(FastMath.pow(0.25,i-start + 1));
            }
        }
        return result;
    }
    public CellPart getCellPart(int ID){
        return cellParts.get(ID);
    }
    public static int getHeaderSize(){
        return HEADER_SIZE;
    } 
    public static int getCellDataSize(){
        return CELL_DATA_SIZE;
    }
    public static int getBuildScale(){
        return BUILD_COST_SCALE;
    }
    
    //getter methods
    public HashMap<Integer, String> getGenome(){
        return genome;
    }
    public ArrayList<CellPartInstance> getCellPartInstances(){
        return cellPartInstances;
    }
    public ArrayList<Gametangium> getGametangii(){
        return gametangii;
    }
    public NeuralNetwork getNeuralNetwork(){
        return neuralNetwork;
    }
    public double getNetworkMutationProbability(){
        return networkMutationProbability;
    }
    public SegmentInstance getHead(){
        return head;
    }
    public double getEnergy(){
        return energy;
    }
    public double getChildEnergy(){
        return childEnergy;
    }
    public double getGrowthEnergy(){
        return growthEnergy;
    }
    public double getGrowthPriority(){
        return growthPriority;
    }
    public double getCurrentGrowthEnergy(){
        return currentGrowthEnergy;
    }
    public double getRegenerationFraction(){
        return regenerationFraction;
    }
    public double getOverlapPercent(){
        return overlapPercent;
    }
    public int getIndex(){
        return index;
    }
    public double getSize(){
        return size;
    }
    public double getAngle(){
        return angle;
    }
    public boolean isDeleted(){
        return deleted;
    }
    public boolean getReproductionType(){
        return reproductionType;
    }
    public int updatingSpecies(){
        return updateSpecies;
    }
    public Species getSpecies(){
        return species;
    }
    public BufferedImage getLastIcon(){
        return icon.image;
    }
    
    //setter methods
    public void setNeuralNetwork(NeuralNetwork n){
        this.neuralNetwork = n;
    }
    public void mutateNetworkMutationProbability(){
        networkMutationProbability += (getRand().nextDouble()-0.5)*2*0.01;
        networkMutationProbability = FastMath.min(FastMath.max(networkMutationProbability, 0.0), 1.0);
    }
    public void addCellPartInstance(CellPartInstance p){
        cellPartInstances.add(p);
    }
    public void addGametangium(Gametangium g){
        gametangii.add(g);
    }
    public void removeEnergy(double energy){
        this.energy -= energy;
    }
    public void addCurrentGrowthEnergy(double energy){
        currentGrowthEnergy += energy;
    }
    public void setCurrentGrowthEnergy(double energy){
        this.currentGrowthEnergy = energy;
    }
    public void setEnergy(double energy){
        this.energy = energy;
    }
    public void addEnergy(double energy){
        this.energy += energy;
    }
    public void addChild(){
        children += 1;
    }
    public void addForce(double[] force, double[] coords){
        /*double magnitude = FastMath.hypot(force[0],force[1]);
        double distance = FastMath.hypot(coords[0],coords[1]);

        double forceAngle = FastMath.atan2(force[1],force[0]);
        double coordAngle;
        if(distance != 0){
            coordAngle = FastMath.atan2(coords[1],coords[0]);
        }else{
            coordAngle = forceAngle;
        }
        //calculate rotational acceleration
        double angleYComp = FastMath.sin(forceAngle-coordAngle)*magnitude*100;
        rotationalAccel += angleYComp*distance;

        //calculate linear acceleration
        double angleXComp = FastMath.cos(forceAngle-coordAngle)*magnitude;
        double linearX = FastMath.cos(coordAngle)*angleXComp;
        double linearY = FastMath.sin(coordAngle)*angleXComp;
        xAccel += linearX;
        yAccel += linearY;*/
    }
    public void setHead(SegmentInstance head){
        this.head = head;
        if(head == null){
        
        }
    }
    public void setDeleted(boolean deleted){
        this.deleted = deleted;
        if(species != null){
            species.removeCreature(this);
        }
    }
    public void setSpecies(Species s){
        this.species = s;
    }
    public void updateSpecies(){
        updateSpecies -= 1;
        if(updateSpecies <= 0){
            updateSpecies = 0;
        }
    }
    
    public final void addInput(ProteinInstance protein){
        inputParts.add(protein);
    }
    public void addOutput(CellPartInstance cellPart){
        outputParts.add(cellPart);
    }
    public void resetInputs(){
        inputParts = new ArrayList<>();
    }
    public void resetOutputs(){
        outputParts = new ArrayList<>();
    }
    
    public void setIconBounds(double newX, double newY){
        iconMinX = FastMath.min(iconMinX, (newX)-0.2);
        iconMinY = FastMath.min(iconMinY, (newY)-0.2);
        iconMaxX = FastMath.max(iconMaxX, (newX)+0.2);
        iconMaxY = FastMath.max(iconMaxY, (newY)+0.2); 
    }
    public void setGenome(HashMap<Integer, String> genome){
        this.genome = genome;
    }
}
