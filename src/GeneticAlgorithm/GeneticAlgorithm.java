/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneticAlgorithm;

import CellParts.SegmentInstance;
import static GUI.GUIManager.getScreenSize;
import static GUI.GUIManager.getSimulationPanel;
import static GeneticAlgorithm.Creature.getBase;
import static GeneticAlgorithm.Creature.getCellDataSize;
import static GeneticAlgorithm.Creature.getHeaderSize;
import static HyperLife.HyperLife.getGA;
import static HyperLife.HyperLife.getRand;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class GeneticAlgorithm {
     
    private ArrayList<Creature> creatures = new ArrayList<>();
    private ArrayList<Creature> creaturesToBeAdded = new ArrayList<>();
    private ArrayList<SegmentInstance> segments = new ArrayList<>();    
    private ArrayList<Object[]> segmentsToAdd = new ArrayList<>(); 
    private int maxCreatures = -1;
    
    private ArrayList<Species> ancestors;
    private ArrayList<Species> species;
    private int totalSpecies, currentSpecies;
    
    private int speciesID = -1;
    private int geneID = -1;
    private int creatureID = -1;
    
    private int maxChromosomes = 40, maxSize = 200;

    private double geneDifferenceScalar = 0.5f;
    private double baseDifferenceScalar = 0.1f;
    private double compatabilityDistanceThreshold = 5.0f;
    
    private double insertGeneChance = 0.004;
    private double cloneGeneChance = 0.01;
    private double deleteGeneChance = 0;//0.00005;
    
    private double cloneBaseChance = 0;//0.002;
    private double alterSectionLocationChance = 0.05;
    private double alterBaseChance = 0.005;
    private double insertBaseChance = 0;//0.0003;
    private double deleteBaseChance = 0;//0.00005;
    
    private double crossoverCellDataChance = 0.2;// less than = mix bases, more than = use one parent
    
    public GeneticAlgorithm(){
        species = new ArrayList<>();
        ancestors = new ArrayList<>();
    }
    
    public void simulate(Graphics2D g){
        boolean paused = getSimulationPanel().getPausedBool().getValue();
        double cameraX = getSimulationPanel().getCameraX();
        double cameraY = getSimulationPanel().getCameraY();
        double scale = getSimulationPanel().getScale();
        double width = getScreenSize().getWidth()/(2*scale);
        double height = getScreenSize().getHeight()/(2*scale);
        
        if(!paused){
            creatures.addAll(creaturesToBeAdded);
            for(Object[] segment : segmentsToAdd){
                int index = FastMath.max(FastMath.min((int)segment[0], getGA().getSegments().size()), 0);
                segments.add(index, (SegmentInstance)segment[1]);
            }
            if(!creaturesToBeAdded.isEmpty()){  //assign the species of the new creatures
                assignSpecies();
            }
            creaturesToBeAdded = new ArrayList<>();
            segmentsToAdd = new ArrayList<>();
            Iterator<Creature> it = creatures.iterator();
            while(it.hasNext()){
                Creature creature = it.next();
                creature.simulate();
                if(creature.getEnergy() <= 0){
                    it.remove();
                    killCreature(creature);
                    assignSpecies();
                }
            }
            Iterator<SegmentInstance> segmentIterator = segments.iterator();
            while(segmentIterator.hasNext()){
                SegmentInstance segment = segmentIterator.next();
                segment.simulate();
                if(segment.isDead()){
                    segmentIterator.remove();
                }
            } 
        }
        for(SegmentInstance segment : segments){
            if((segment.getX() + segment.getMinX()) < cameraX + width && (segment.getX() + segment.getMaxX()) > cameraX - width &&
               (segment.getY() + segment.getMinY()) < cameraY + height && (segment.getY() + segment.getMaxY()) > cameraY - height){
                segment.draw(g);
            }
        }
        //kill creatures over limit
        if(maxCreatures != -1 && creatures.size() > 0){
            if(creatures.size() > maxCreatures){
                ArrayList<Creature> newList = new ArrayList<>();
                newList.addAll(creatures);
                Collections.sort(newList, (Creature c1, Creature c2) -> (Double.compare(c2.getEnergy(), c1.getEnergy())));
                while(newList.size() > maxCreatures){
                    int index = (int)FastMath.max(FastMath.min(newList.size() + getRand().nextGaussian()*newList.size()/3, newList.size()-1),0);
                    Creature randomCreature = newList.get(index);
                    removeCreature(randomCreature);
                    newList.remove(randomCreature);
                    killCreature(randomCreature);
                }
                creatures = newList;
            }
        }
        //assignSpecies();
        if(!ancestors.isEmpty()){
            int totalChildren = 0;
            for(Species ancestor : ancestors){
                totalChildren += ancestor.getTotalChildren()+1;
            }
            double newX = 0, ancestorWidth = 500;
            for(Species ancestor : ancestors){
                double xChange = ancestorWidth*(ancestor.getTotalChildren()+1)/totalChildren;
                ancestor.draw(g, (int) newX, 10, (int) newX + xChange, 200);
                newX += xChange;
            }
        }
    }
    
    public void cloneCreature(Creature creature, double x, double y, double angle, boolean mutate){
        Creature clone = creature.createClone();
        double speed = (0.05 + (getRand().nextDouble())*0.1);
        double creatureAngle = getRand().nextDouble()*360;
        if(mutate) mutateCreature(clone);
        clone.setX(x);
        clone.setY(y);
        clone.getHead().getStartPoint().setX(x);
        clone.getHead().getStartPoint().setY(y);
        clone.getHead().getStartPoint().setLastX(x);
        clone.getHead().getStartPoint().setLastY(y);
        clone.getHead().getEndPoint().setX(x+1);
        clone.getHead().getEndPoint().setY(y+1);
        clone.getHead().getEndPoint().setLastX(x+1);
        clone.getHead().getEndPoint().setLastY(y+1);
        clone.getHead().addX(FastMath.cos(angle)*speed);
        clone.getHead().addY(FastMath.sin(angle)*speed);
        clone.getHead().addAngle(creatureAngle);
        creatures.add(clone);
        assignSpecies();
        creature.addChild();
    }
    
    public void crossover(Creature c1, Creature c2){
        //Creature child = new Creature();
        HashMap<Integer, String> childGenome = new HashMap<>();
        
        for (Map.Entry<Integer, String> entry : c1.getGenome().entrySet()) {
            int key = entry.getKey();
            String gene1 = entry.getValue();
            if (c2.getGenome().containsKey(key)) { // matching gene
                String childConGene = crossoverGene(gene1, c2.getGenome().get(key));
                childGenome.put(key, childConGene);
            }else{ // disjoint or excess gene
                String childConGene = gene1;
                childGenome.put(key, childConGene);
            }
        }
        for (Map.Entry<Integer, String> entry : c2.getGenome().entrySet()) {
            int key = entry.getKey();
            String gene2 = entry.getValue();
            if(!childGenome.containsKey(key)){
                if (c1.getGenome().containsKey(key)) { // matching gene
                    String childConGene = crossoverGene(gene2, c1.getGenome().get(key));
                    childGenome.put(key, childConGene);
                }else{ // disjoint or excess gene
                    String childConGene = gene2;
                    childGenome.put(key, childConGene);
                }
            }
        }
        double x = (c1.getX() + c2.getX())*0.5;
        double y = (c1.getY() + c2.getY())*0.5;
        double angle = getRand().nextDouble()*Math.PI*2;
        double speed = (getRand().nextDouble())*0.01;
        Creature child = new Creature(childGenome, x, y);
        //crossover neural networks
        double energyRatio = FastMath.min(FastMath.max(c2.getEnergy()/c1.getEnergy(), 0.0), 1.0);
        if(c1.getNeuralNetwork() != null && c2.getNeuralNetwork() != null){
            child.setNeuralNetwork(c1.getNeuralNetwork().copy());
            child.getNeuralNetwork().merge(c2.getNeuralNetwork(), energyRatio);
        }else if(c1.getNeuralNetwork() != null){
            child.setNeuralNetwork(c1.getNeuralNetwork().copy());
        }else if(c2.getNeuralNetwork() != null){
            child.setNeuralNetwork(c2.getNeuralNetwork().copy());
        }
        //mutate creature
        mutateCreature(child);
        child.getHead().addAngle(angle);
        child.getHead().addX(FastMath.cos(angle)*speed);
        child.getHead().addY(FastMath.sin(angle)*speed);
        double energyLoss1 = c1.getChildEnergy()*c1.getEnergy();
        double energyLoss2 = c2.getChildEnergy()*c2.getEnergy();
        c1.removeEnergy(energyLoss1);
        c2.removeEnergy(energyLoss2);
        child.setEnergy(energyLoss1 + energyLoss2);
        creaturesToBeAdded.add(child);
        c1.addChild();
        c2.addChild();
    }
    
    public String crossoverGene(String gene1, String gene2){
        String newGene = "";
        int headerLength = FastMath.min(FastMath.max(gene1.length(), gene2.length()), Creature.getHeaderSize());
        boolean crossover = getRand().nextDouble() < crossoverCellDataChance;
        boolean parent = getRand().nextBoolean();
        for(int i = 0; i < headerLength; i++){
            if(crossover){
                if(i < gene1.length() && i < gene2.length()){
                    newGene += getRand().nextBoolean() ? gene1.substring(i,i+1) : gene2.substring(i,i+1);
                }else if(i < gene1.length() && i >= gene2.length()){
                    newGene += gene1.substring(i,i+1);
                }else if(i >= gene1.length() && i < gene2.length()){
                    newGene += gene2.substring(i,i+1);
                }
            }else{
                if(parent){
                    if(i < gene1.length()){
                        newGene += gene1.substring(i,i+1);
                    }else{
                        newGene += gene2.substring(i,i+1);
                    }
                }else{
                    if(i < gene2.length()){
                        newGene += gene2.substring(i,i+1);
                    }else{
                        newGene += gene1.substring(i,i+1);
                    }
                }
            }
        }
        int geneLength = FastMath.max(gene1.length(), gene2.length());
        for(int i = Creature.getHeaderSize(); i + Creature.getCellDataSize() - 1 < geneLength; i += Creature.getCellDataSize()){
            crossover = getRand().nextDouble() <= crossoverCellDataChance;
            if(crossover){
                int j = i; // loop through bases in cell data
                while(j < i+Creature.getCellDataSize()-1){
                    if(j < gene1.length() && j < gene2.length()){
                        newGene += getRand().nextBoolean() ? gene1.substring(j,j+1) : gene2.substring(j,j+1);
                    }else if(j < gene1.length() && j >= gene2.length()){
                        newGene += gene1.substring(j,j+1);
                    }else if(j >= gene1.length() && j < gene2.length()){
                        newGene += gene2.substring(j,j+1);
                    }
                    j += 1;
                }
            }else{
                parent = getRand().nextBoolean();
                if(parent){
                    if(i+ Creature.getCellDataSize() - 1 < gene1.length()){
                        newGene += gene1.substring(i,i+ Creature.getCellDataSize());
                    }else{
                        newGene += gene2.substring(i,i+ Creature.getCellDataSize());
                    }
                }else{
                    if(i+ Creature.getCellDataSize() - 1 < gene2.length()){
                        newGene += gene2.substring(i,i+ Creature.getCellDataSize());
                    }else{
                        newGene += gene1.substring(i,i+ Creature.getCellDataSize());
                    }
                }
            }
        }
        return newGene;
    }
    
    public void mutateCreature(Creature creature){
        HashMap<Integer, String> tempGenome;
        HashMap<Integer, String> genome = new HashMap<>();
        double random;
        //clone gene 
        for (Map.Entry<Integer, String> entry : creature.getGenome().entrySet()) {
            genome.put(entry.getKey(), entry.getValue());
            random = getRand().nextDouble();
            if(random < cloneGeneChance){
                genome.put(nextGeneID(), entry.getValue());
            }
        }
        //insert gene
        double insertGeneRand = getRand().nextDouble();
        if(insertGeneRand < insertGeneChance){
            String chromosome = "";
            for(int base = 0; base < maxSize; base++){
                chromosome += Integer.toString(getRand().nextInt(4));
            }
            genome.put(nextGeneID(), chromosome);
        }
        //delete gene 
        tempGenome = new HashMap<>();
        for (Map.Entry<Integer, String> entry : genome.entrySet()) {
            random = getRand().nextDouble();
            if(random > deleteGeneChance || entry.getKey() == 0){
                tempGenome.put(entry.getKey(), entry.getValue());
            }
        }
        genome = tempGenome;
        for (Map.Entry<Integer, String> entry : genome.entrySet()) {
            String gene = entry.getValue();
            String newGene = "";
            for(int i = 0; i < gene.length(); i++){
                int base = getBase(gene, i);
                double deletion = getRand().nextDouble();
                double insertion = getRand().nextDouble();
                double alter = getRand().nextDouble();
                double alterParentGene = getRand().nextDouble();
                double adjustedIndex = i-getHeaderSize()-(getCellDataSize()-1);
                if(alter < alterBaseChance){
                    base = getRand().nextInt(4);
                }
                if(deletion > deleteBaseChance && 
                        !(alterParentGene < alterSectionLocationChance && 
                        i >= getHeaderSize() && 
                        adjustedIndex % getCellDataSize() == 0)){
                    newGene += base;
                }
                if(i >= getHeaderSize() && adjustedIndex % getCellDataSize() == 0){
                    int sectionSize = getCellDataSize();
                    //insert new section
                    if(insertion < insertBaseChance){
                        for(int j = 0; j < sectionSize; j ++){
                            newGene += Integer.toString(getRand().nextInt(4));
                        }
                    }
                    //change location of section
                    if(alterParentGene < alterSectionLocationChance){
                        String section = gene.substring(i-(getCellDataSize()-1),i+1);
                        ArrayList<Integer> keysAsArray = new ArrayList<>(genome.keySet());
                        int newKey = keysAsArray.get(getRand().nextInt(keysAsArray.size()));
                        if(newKey < entry.getKey()){
                            genome.put(newKey, genome.get(newKey)+section);
                        }else if(newKey == entry.getKey()){
                            newGene += section;
                        }else{
                            creature.getGenome().put(newKey, genome.get(newKey)+section);
                        }
                    }
                }
                genome.put(entry.getKey(),newGene);
            }
        }
        //add cloned genes to the end of the whole chromosome
        for (Map.Entry<Integer, String> entry : genome.entrySet()) {
            String gene = entry.getValue();
            for(int i = 0; i < gene.length(); i++){
                double adjustedIndex = i-getHeaderSize()-(getCellDataSize()-1);
                double cloning = getRand().nextDouble();
                if(cloning < cloneBaseChance && i >= getHeaderSize() && adjustedIndex % getCellDataSize() == 0){
                    String lastItems = gene.substring(i-(getCellDataSize()-1),i+1);
                    gene += lastItems;
                }
            }
            genome.put(entry.getKey(), gene);
        }
        creature.setGenome(genome);
        creature.resetInputs();
        creature.resetOutputs();
        creature.removeFromQuadTree();
        if(creature.getNeuralNetwork() != null){
            creature.mutateNetworkMutationProbability();
            creature.getNeuralNetwork().mutate(creature.getNetworkMutationProbability());
        }
        double oldEnergy = creature.getCurrentGrowthEnergy();
        creature.setCurrentGrowthEnergy(0);
        creature.readDNA();
        boolean result = true;
        while(creature.getCurrentGrowthEnergy() < oldEnergy && result){
            result = creature.grow();
        }
        assignSpecies();
    }
    
    public void killCreature(Creature creature){
        if(creature != null){
            creature.removeFromQuadTree();
            //creature.removeSegments();    //segments are destroyed
            creature.getHead().killSegment(); //segments are detatched from creature host (becoming waste biomass)
            creature.setDeleted(true);
            getSimulationPanel().getQuadTree().recalibrate();
            if(getSimulationPanel().getSelectedSegment() != null){
                if(creature == getSimulationPanel().getSelectedSegment().getCreature()){
                    getSimulationPanel().getSimulationToolbar().setCurrentTab("simulation");
                    getSimulationPanel().setSelectedSegment(null);
                }
            }
        }
    }
    
    public void killRandomCreature(){
        if(creatures.size() > 0){
            Creature randomCreature = getRandomCreature();
            removeCreature(randomCreature);
            killCreature(randomCreature);
        }
    }
    
    public void killHalfCreatures(){
        int killSize = (int)(creatures.size()/2);
        while(killSize > 0 && creatures.size() > 0){
            Creature randomCreature = getBiasedRandomCreature();
            removeCreature(randomCreature);
            killCreature(randomCreature);
            killSize -= 1;
        }
    }
    
    public void removeCreature(Creature creature){
        creatures.remove(creature);
        assignSpecies();
    }
    
    public void createRandomCreature(){
        double width = getSimulationPanel().getXSize();
        double height = getSimulationPanel().getYSize();
        double angle = getRand().nextDouble()*FastMath.PI*2;
        double x = getRand().nextDouble()*width - (double)width*0.5;
        double y = getRand().nextDouble()*height - (double)height*0.5;
        
        double speed = (getRand().nextDouble())*0.01;
        
        HashMap<Integer, String> genome = getRandomGenome();
        Creature creature = new Creature(genome, x, y);
        creature.setEnergy(100);
        creatures.add(creature);
        creature.getHead().addAngle(angle);
        creature.getHead().addX(FastMath.cos(angle)*speed);
        creature.getHead().addY(FastMath.sin(angle)*speed);
        assignSpecies();
    }
    
    public HashMap<Integer, String> getRandomGenome(){
        if(false){
            int numChromosomes = getRand().nextInt(maxChromosomes) + 2;
            int size = getRand().nextInt(maxSize) + getHeaderSize();
            HashMap<Integer, String> genome = new HashMap<>();
            geneID = 0;
            for(int c = 0; c < numChromosomes; c++){
                String chromosome = "";
                for(int base = 0; base < size; base++){
                    chromosome += Integer.toString(getRand().nextInt(4));
                }
                if(!genome.isEmpty()){
                    geneID = nextGeneID();
                }
                genome.put(geneID, chromosome);
            }
            return genome;
        }else{
            double rand = getRand().nextDouble();
            if(rand < 0.2){
                return getViper();
            }else if(rand < 0.4){
                return getTripod();
            }else if(rand < 0.6){
                return getTrilobyte();
            }else if(rand < 0.8){
                return getCrab();
            }else{
                return getAlgae();
            }
        }
    }
    
    public Creature getRandomCreature(){
        if(creatures.size() > 0){
            return creatures.get(getRand().nextInt(creatures.size()));
        }else{
            return null;
        }
    }
    
    public Creature getBiasedRandomCreature(){
        if(creatures.size() > 0){
            int randomIndex = (int)FastMath.max(FastMath.min(creatures.size() + getRand().nextGaussian()*creatures.size()/3, creatures.size()-1),0);
            return creatures.get(randomIndex);
        }else{
            return null;
        }
    }
    
    public void assignSpecies() {
        // Place genomes into species
        boolean speciesCreated = false;
        for (Creature creature : creatures) {
            if(creature.updatingSpecies() != 0 || speciesCreated){
                creature.updateSpecies();
                boolean foundSpecies = false;
                Species closestSpecies = null;
                double minCompatibility = compatabilityDistanceThreshold*5;
                for (Species s : species) {
                    if(s.deathTime == -1){
                        double compatibility = Creature.getCompatibility(creature, s.mascot, geneDifferenceScalar, baseDifferenceScalar);
                        if (compatibility < compatabilityDistanceThreshold){ // compatibility distance is less than DT, so genome belongs to this species
                            foundSpecies = true;
                            s.addCreature(creature);
                            break;
                        }else if(compatibility < minCompatibility){
                            minCompatibility = compatibility;
                            closestSpecies = s;
                        }
                    }
                }

                if (!foundSpecies) {
                    Species newSpecies;
                    if(closestSpecies == null){
                        newSpecies = new Species(creature);
                        ancestors.add(newSpecies);
                    }else{
                        newSpecies = new Species(closestSpecies, creature);
                        speciesCreated = true;
                    }
                    species.add(newSpecies);
                    currentSpecies += 1;
                    totalSpecies += 1;
                }
            }
        }
        if(speciesCreated){
            assignSpecies();
        }
    }

    
    //creature archetypes
    public HashMap<Integer, String> getViper(){
        HashMap<Integer, String> genome = new HashMap<>();
        String c1 = "0000" + "1212" + "2121";
        String c2 = "0110" + "1322" + "2121";
        String c3 = "0221" + "2033" + "2121";
        String c4 = "0331" + "2203" + "2121";
        String c5 = "1102" + "2320" + "2121";
        String c6 = "1212" + "3030" + "2121";

        genome.put(0,"20100100200300200");
        String geneA = "3" + c1 + "1221220030020";
        //tails
        geneA += "0023332020";
        geneA += "0133331233";
        //proteins
        geneA += "1023330020";
        geneA += "1023330122";
        geneA += "1033330200";
        genome.put(1,geneA);

        //centre body construcutor
        String geneB = "3" + c1 + "2211313330020";
        geneB += "0031112020";
        geneB += "0133332203";
        geneB += "1113333030";
        genome.put(2,geneB);

        String geneC = "3" + c2 + "1212111330020";
        geneC += "0043332020";
        geneC += "1111113020";
        genome.put(3,geneC);

        String geneD = "3" + c3 + "1202110330020";
        geneD += "0113332020";
        geneD += "1111113000";
        genome.put(4,geneD);

        String geneE = "3" + c4 + "1002310030020";
        geneE += "0123332020";
        geneE += "1013331020";
        genome.put(5,geneE);

        String geneF = "3" + c5 + "0323130030020";
        geneF += "0123332020";
        genome.put(6,geneF);
        
        //tail gene constructor
        String geneG = "3" + c2 + "1012320330100";
        geneG += "0201112020";
        genome.put(7, geneG);
        String geneH = "3" + c3 + "0332220030020";
        geneH += "0211112020";
        genome.put(8, geneH);
        String geneI = "3" + c4 + "0232120030020";
        geneI += "0211112020";
        genome.put(9, geneI);


        //proteins
        String geneJ = "0" + c1 + "011333333100001";//gametangium
        genome.put(17, geneJ);
        String geneK = "0" + c1 + "013200";//jaws
        genome.put(18,geneK);
        String geneL = "0" + c6 + "002333";//colour sensor
        genome.put(19,geneL);
        String geneM = "0" + c3 + "001333";//soft sensor
        genome.put(21,geneM);
        return genome;
    }
    
    public HashMap<Integer, String> getTripod(){
        HashMap<Integer, String> genome = new HashMap<>();
        String c1 = "1212" + "0000" + "0000";
        String c2 = "2030" + "0000" + "0000";
        String c3 = "2303" + "0000" + "0000";
        String c4 = "3121" + "0000" + "0000";
        String c5 = "3333" + "0000" + "0000";
        
        genome.put(0,"30200333200300333");
        String geneA = "3" + c1 + "0201003030020";
        //tails
        geneA += "0023332020";
        geneA += "0133332200";
        //jaws
        geneA += "1023330020";
        //spikes
        geneA += "1103330120";
        //eyes
        geneA += "1033331130";
        geneA += "1033331020";
        geneA += "1033330310";
        geneA += "1033330200";
      genome.put(1,geneA);

        //centre constructor
        String geneB = "3" + c2 + "0121022030010";
        geneB += "0030002020";
        geneB += "1113331020";
        genome.put(2,geneB);
        String geneC = "3" + c3 + "0111101030002";
        geneC += "0101112020";
        //proteins
        geneC += "1011111020";
        genome.put(3,geneC);
        String geneD = "3" + c4 + "0101201030001";
        geneD += "0101112020";
        genome.put(4,geneD);

        //arm construcutor
        String geneE = "3" + c1 + "0131001030020";
        geneE += "0203332010";
        geneE += "1123331222";
        //jet
        geneE += "1133332100";
        genome.put(7,geneE);

        String geneF = "3" + c2 + "0121020330010";
        geneF += "0213332020";
        genome.put(8,geneF);

        String geneG = "3" + c3 + "0111100330003";
        geneG += "0223332020";
        genome.put(9,geneG);

        String geneH = "3" + c4 + "0101200330002";
        geneH += "0233332020";
        genome.put(10,geneH);

        String geneI = "3" + c5 + "0031300030001";
        genome.put(11,geneI);
        
        //proteins
        String geneJ = "0" + c2 + "011100333100001";// gamentangium
        genome.put(17,geneJ);
        String geneK = "0" + c1 + "013200";//jaws
        genome.put(18,geneK);
        String geneL = "0" + c3 + "002200";//eye
        genome.put(19,geneL);
        String geneM = "0" + c2 + "010333100";// spike
        genome.put(20,geneM);
        String geneN = "0" + c2 + "001200";//soft sensor
        genome.put(21,geneN);
        String geneO = "0" + c5 + "001100";// mini soft sensor
        genome.put(22,geneO);
        String geneP = "0" + c2 + "020333";// jet
        genome.put(23,geneP);
        return genome;
    }
    
    public HashMap<Integer, String> getTrilobyte(){
        HashMap<Integer, String> genome = new HashMap<>();
        String c1 = "1013" + "0022" + "0000";
        String c2 = "1130" + "0110" + "0000";
        String c3 = "1300" + "0133" + "0000";
        String c4 = "2011" + "0211" + "0000";
        String c5 = "2121" + "0303" + "0000";
        
        //head
        genome.put(0,"30100333200300020");
        String geneA = "3" + c1 + "3331203030020";
        //centre body
        geneA += "0021112020";
        //arm
        geneA += "0133331133";   
        //proteins
        geneA += "1023330020";
        geneA += "1123330220";
        geneA += "1033331000";
        //leg
        geneA += "0223331233";
        genome.put(1, geneA);
        
        //centre body
        String geneB = "3" + c2 + "2201000030020";
        geneB += "0031112020";
        geneB += "0303331233";
        geneB += "0303331133";
        genome.put(2, geneB);
        
        String geneC = "3" + c3 + "2000300030020";
        geneC += "0101112020";
        geneC += "0303331233";
        geneC += "0303331133";
        genome.put(3, geneC);
        
        String geneD = "3" + c4 + "1200200030020";
        geneD += "0111112020";
        geneD += "0303331233";
        geneD += "0303331133";
        genome.put(4, geneD);
        
        String geneE = "3" + c5 + "1100200030020";
        geneE += "0121112020";
        geneE += "0303331233";
        geneE += "0303331133";
        genome.put(5, geneE);
        
        String geneF = "3" + c5 + "1000200030020";
        geneF += "1013332020";
        geneE += "0303331233";
        genome.put(6, geneF);
        
        //sides
        String geneG = "3" + c2 + "1100300030020";
        geneG += "0203332120";
        genome.put(7, geneG);
        
        String geneH = "3" + c3 + "0300223030020";
        //geneH += "1103332220";
        geneH += "1133332020";
        genome.put(8, geneH);
        
        String geneJ = "3" + c5 + "0101000330200";//leg
        genome.put(12, geneJ);
        
        String geneN = "0" + c1 + "011200333100001";//gametangium
        genome.put(17, geneN);
        String geneP = "0" + c2 + "013333";//jaws
        genome.put(18, geneP);
        String geneK = "0" + c3 + "002200";//eye
        genome.put(19,geneK);
        //String geneI = "0" + c5 + "010300";//spike
        //genome.put(20, geneI);
        //String geneL = "0" + c3 + "010100";// mini soft sensor
        //genome.put(21,geneL);
        String geneO = "0" + c5 + "000100";//rigid touch sensor
        genome.put(22, geneO);
        String geneM = "0" + c5 + "020333";// jet
        genome.put(23,geneM);
        return genome;
    }
    
    public HashMap<Integer, String> getCrab(){
        HashMap<Integer, String> genome = new HashMap<>();
        /*String c1 = "1231" + "1122" + "2101";
        String c2 = "2001" + "1301" + "2133";
        String c3 = "2110" + "2020" + "2230";
        String c4 = "2220" + "2132" + "2322";
        String c5 = "2323" + "2311" + "3013";*/
        String c1 = "1311" + "1022" + "2200";
        String c2 = "2023" + "1202" + "2311";
        String c3 = "2202" + "1321" + "3023";
        String c4 = "2320" + "2100" + "3200";
        String c5 = "1132" + "0303" + "2022";
        
        genome.put(0,"30100333200300200");
        //body
        String geneA = "3" + c1 + "3303000030020";
        geneA += "0023330320";
        geneA += "0133331020";
        geneA += "0133331120";
        geneA += "0133331220";
        //proteins
        //jaws
        geneA += "0303330020";
        //eyes
        geneA += "0223330230";
        //spikes
        geneA += "0213330120";
        geneA += "0213331320";
        //gametangium
        geneA += "0233332020";
        genome.put(1, geneA);
        
        //front pincer
        String geneB = "3" + c2 + "0221200330002";
        geneB += "0033331310";
        genome.put(2, geneB);
        //connector
        String geneC = "3" + c3 + "0220301030002";
        geneC += "0103331300";
        genome.put(3, geneC);
        //2nd segment
        String geneD = "3" + c4 + "0311201330002";
        geneD += "0123331220";
        geneD += "0113332010";
        genome.put(4, geneD);    
        //claws
        String geneE = "3" + c3 + "0131200330100";
        genome.put(5, geneE);   
        String geneF = "3" + c3 + "0111000330300";
        genome.put(6, geneF);
        
        //legs
        String geneG = "3" + c3 + "0101200330200";
        geneG += "0203332030";
        //spikes
        //geneG += "0213330120";
        //geneG += "0213331320";
        
        //geneG += "0213332120";
        //geneG += "0213333320";
        genome.put(7, geneG);
        String geneH = "3" + c4 + "0101200330100";
        //geneH += "0213330120";
        //geneH += "0213331320";
        
        //geneH += "0213332120";
        //geneH += "0213333320";
        genome.put(8, geneH);
        
        //spike
        String geneI = "0" + c1 + "010300020";
        genome.put(9, geneI);
        //eye
        String geneJ = "0" + c1 + "002200";
        genome.put(10,geneJ);
        //gametangium
        String geneK = "0" + c1 + "011333333100001";
        genome.put(11,geneK);
        //jaws
        String geneL = "0" + c2 + "013100";
        genome.put(12, geneL);
        
        return genome;
    }
      
    public HashMap<Integer, String> getAlgae(){
        HashMap<Integer, String> genome = new HashMap<>();
        String c1 = "1202" + "2320" + "0323";
        String c2 = "1111" + "2201" + "0303";
        String c3 = "1021" + "2022" + "0230";
        String c4 = "0330" + "1303" + "0210";
        
        genome.put(0,"00100100000200333");
        
        //body
        String geneA = "3" + c1 + "2002000003000";
        geneA += "0023330020";
        geneA += "0023331020";
        geneA += "0023332020";
        geneA += "0023333020";
        genome.put(1, geneA);
        
        String geneB = "3" + c2 + "1003000003000";
        geneB += "0033332020";
        geneB += "0043330220";
        geneB += "0043331220";
        geneB += "0043332220";
        geneB += "0043333220";
        genome.put(2, geneB);
        
        String geneC = "3" + c3 + "2002000003000";
        geneC += "0023331220";
        geneC += "0023332220";
        geneC += "0053332020";
        genome.put(3, geneC);
        
        //chloroplast
        String geneD = "0" + c4 + "003333";
        genome.put(4, geneD);
        
        String geneE = "0" + c1 + "011200000333001";//gametangium
        genome.put(5, geneE);
        
        return genome;
    }
    
    
    //getter/setter methods
    public void clear(){
        speciesID = -1;
        geneID = -1;
        creatureID = -1;
        creatures = new ArrayList<>();
        segments = new ArrayList<>();
        species = new ArrayList<>();
        ancestors = new ArrayList<>();
    }
    public void addSegment(int index, SegmentInstance segment){
        segmentsToAdd.add(new Object[]{index, segment});
    }
    public void removeSegment(SegmentInstance segment){
        segment.removeFromQuadTree();
        segment.detatchFromCreature();
        segment.setDead(true);
    }
    public void setMaxCreatures(int maxCreatures){
        this.maxCreatures = maxCreatures;
    }
    public void removeSpecies(){
        currentSpecies -= 1;
    }
    
    public ArrayList<Creature> getCreatures(){
        return creatures;
    }
    public ArrayList<SegmentInstance> getSegments(){
        return segments;
    }
    public int getTotalSpecies() {
        return totalSpecies;
    }
    public int getCurrentSpecies() {
        return currentSpecies;
    }
    public int getNumCreatures() {
        return creatures.size();
    }
    public int nextSpeciesID(){
        speciesID += 1;
        return speciesID;
    }
    public int nextGeneID(){
        geneID += 1;
        return geneID;
    }
    public int nextCreatureID(){
        creatureID += 1;
        return creatureID;
    }
}
