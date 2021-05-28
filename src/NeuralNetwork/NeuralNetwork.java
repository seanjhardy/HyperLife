package NeuralNetwork;

import NeuralNetwork.activationfunctions.*;
import NeuralNetwork.utilities.FileReaderAndWriter;
import NeuralNetwork.utilities.MatrixUtilities;

import java.util.Arrays;
import java.util.Random;
import org.ejml.simple.SimpleMatrix;

/**
 * Created by KimFeichtinger on 04.03.18.
 */
public class NeuralNetwork {

    private ActivationFunctionFactory activationFunctionFactory = new ActivationFunctionFactory();

    private Random random = new Random();

    // Dimensions of the neural network
    private int[] layers;

    private SimpleMatrix[] weights;
    private SimpleMatrix[] biases;

    private double learningRate;

    private String activationFunctionKey;

    // Constructor
    // Generate a new neural network with a given amount of hidden layers with the given amount of nodes in the individual layers
    // Every hidden layer will have the same amount of nodes
    public NeuralNetwork(int[] layers) {
        this.layers = layers;
        initializeDefaultValues();
        initializeWeights();
        initializeBiases();
    }

    // Copy constructor
    public NeuralNetwork(NeuralNetwork nn) {
        this.layers = nn.layers;

        this.weights = new SimpleMatrix[layers.length-1];
        this.biases = new SimpleMatrix[layers.length-1];

        for (int i = 0; i < nn.weights.length; i++) {
            this.weights[i] = nn.weights[i].copy();
        }

        for (int i = 0; i < nn.biases.length; i++) {
            this.biases[i] = nn.biases[i].copy();
        }

        this.learningRate = nn.learningRate;

        this.activationFunctionKey = nn.activationFunctionKey;
    }

    private void initializeDefaultValues() {
        this.setLearningRate(0.1);

        // Sigmoid is the default ActivationFunction
        this.setActivationFunction(ActivationFunction.RELU);
    }

    private void initializeWeights() {
        weights = new SimpleMatrix[layers.length-1];

        // Initialize the weights between the layers and fill them with random values
        for (int i = 0; i < weights.length; i++) {
            weights[i] = SimpleMatrix.random_DDRM(layers[i+1], layers[i], -1, 1, random);
        }
    }

    private void initializeBiases() {
        biases = new SimpleMatrix[layers.length - 1];

        // Initialize the biases and fill them with random values
        for (int i = 0; i < biases.length; i++) {
            biases[i] = SimpleMatrix.random_DDRM(layers[i+1], 1, -1, 1, random);
        }
    }

    // Guess method, input is a one column matrix with the input values
    public double[] guess(double[] input) {
        if (input.length != layers[0]){
            throw new WrongDimensionException(input.length, layers[0], "Input");
        } else {
            // Get ActivationFunction-object from the map by key
            ActivationFunction activationFunction = activationFunctionFactory.getActivationFunctionByKey(activationFunctionKey);

            // Transform array to matrix
            SimpleMatrix output = MatrixUtilities.arrayToMatrix(input);

            for (int i = 0; i < layers.length - 1; i++) {
                output = calculateLayer(weights[i], biases[i], output, activationFunction);
            }

            return MatrixUtilities.getColumnFromMatrixAsArray(output, 0);
        }
    }

    public void train(double[] inputArray, double[] targetArray) {
        if (inputArray.length != layers[0]) {
            throw new WrongDimensionException(inputArray.length, layers[0], "Input");
        } else if (targetArray.length != layers[layers.length-1]) {
            throw new WrongDimensionException(targetArray.length, layers[layers.length-1], "Output");
        } else {
            // Get ActivationFunction-object from the map by key
            ActivationFunction activationFunction = activationFunctionFactory.getActivationFunctionByKey(activationFunctionKey);

            // Transform 2D array to matrix
            SimpleMatrix input = MatrixUtilities.arrayToMatrix(inputArray);
            SimpleMatrix target = MatrixUtilities.arrayToMatrix(targetArray);

            // Calculate the values of every single layer
            SimpleMatrix layerValues[] = new SimpleMatrix[layers.length];
            layerValues[0] = input;
            for (int j = 1; j < layers.length; j++) {
                layerValues[j] = calculateLayer(weights[j - 1], biases[j - 1], input, activationFunction);
                input = layerValues[j];
            }

            for (int n = layers.length-1; n > 0; n--) {
                // Calculate error
                SimpleMatrix errors = target.minus(layers[n]);

                // Calculate gradient
                SimpleMatrix gradients = calculateGradient(layerValues[n], errors, activationFunction);

                // Calculate delta
                SimpleMatrix deltas = calculateDeltas(gradients, layerValues[n - 1]);

                // Apply gradient to bias
                biases[n - 1] = biases[n - 1].plus(gradients);

                // Apply delta to weights
                weights[n - 1] = weights[n - 1].plus(deltas);

                // Calculate and set target for previous (next) layer
                SimpleMatrix previousError = weights[n - 1].transpose().mult(errors);
                target = previousError.plus(layers[n - 1]);
            }
        }
    }

    // Generates an exact copy of a NeuralNetwork
    public NeuralNetwork copy(){
        return new NeuralNetwork(this);
    }

    // Merges the weights and biases of two NeuralNetworks and returns a new object
    // Merge-ratio: 50:50 (half of the values will be from nn1 and other half from nn2)
    public NeuralNetwork merge(NeuralNetwork nn){
        return this.merge(nn, 0.5);
    }

    // Merges the weights and biases of two NeuralNetworks and returns a new object
    // Everything besides the weights and biases will be the same
    // of the object on which this method is called (Learning Rate, activation function, etc.)
    // Merge-ratio: defined by probability
    public NeuralNetwork merge(NeuralNetwork nn, double probability){
        // Check whether the nns have the same dimensions
        if(!Arrays.equals(this.layers, nn.getLayers())){
            throw new WrongDimensionException(this.layers, nn.getLayers());
        }else{
            NeuralNetwork result = this.copy();

            for (int i = 0; i < result.weights.length; i++) {
                result.weights[i] = MatrixUtilities.mergeMatrices(this.weights[i], nn.weights[i], probability);
            }

            for (int i = 0; i < result.biases.length; i++) {
                result.biases[i] = MatrixUtilities.mergeMatrices(this.biases[i], nn.biases[i], probability);
            }
            return result;
        }
    }

    // Gaussian mutation with given probability, Slightly modifies values (weights + biases) with given probability
    // Probability: number between 0 and 1
    // Depending on probability more/ less values will be mutated (e.g. prob = 1.0: all the values will be mutated)
    public void mutate(double probability) {
        applyMutation(weights, probability);
        applyMutation(biases, probability);
    }

    // Adds a randomly generated gaussian number to each element of a Matrix in an array of matrices
    // Probability: determines how many values will be modified
    private void applyMutation(SimpleMatrix[] matrices, double probability) {
        for (SimpleMatrix matrix : matrices) {
            for (int j = 0; j < matrix.getNumElements(); j++) {
                if (random.nextDouble() < probability) {
                    double offset = random.nextGaussian() / 2;
                    matrix.set(j, matrix.get(j) + offset);
                }
            }
        }
    }

    // Generic function to calculate one layer
    private SimpleMatrix calculateLayer(SimpleMatrix weights, SimpleMatrix bias, SimpleMatrix input, ActivationFunction activationFunction) {
        // Calculate outputs of layer
        SimpleMatrix result = weights.mult(input);
        // Add bias to outputs
        result = result.plus(bias);
        // Apply activation function and return result
        return applyActivationFunction(result, false, activationFunction);
    }

    private SimpleMatrix calculateGradient(SimpleMatrix layer, SimpleMatrix error, ActivationFunction activationFunction) {
        SimpleMatrix gradient = applyActivationFunction(layer, true, activationFunction);
        gradient = gradient.elementMult(error);
        return gradient.scale(learningRate);
    }

    private SimpleMatrix calculateDeltas(SimpleMatrix gradient, SimpleMatrix layer) {
        return gradient.mult(layer.transpose());
    }

    // Applies an activation function to a matrix
    // An object of an implementation of the ActivationFunction-interface has to be passed
    // The function in this class will be  to the matrix
    private SimpleMatrix applyActivationFunction(SimpleMatrix input, boolean derivative, ActivationFunction activationFunction) {
        // Applies either derivative of activation function or regular activation function to a matrix and returns the result
        return derivative ? activationFunction.applyDerivativeOfActivationFunctionToMatrix(input)
                          : activationFunction.applyActivationFunctionToMatrix(input);
    }

    public void writeToFile() {
        FileReaderAndWriter.writeToFile(this, null);
    }

    public void writeToFile(String fileName) {
        FileReaderAndWriter.writeToFile(this, fileName);
    }

    public static NeuralNetwork readFromFile() {
        return FileReaderAndWriter.readFromFile(null);
    }

    public static NeuralNetwork readFromFile(String fileName) {
        return FileReaderAndWriter.readFromFile(fileName);
    }

    public String getActivationFunctionName() {
        return activationFunctionKey;
    }

    public void setActivationFunction(String activationFunction) {
        this.activationFunctionKey = activationFunction;
    }

    public void addActivationFunction(String key, ActivationFunction activationFunction){
        activationFunctionFactory.addActivationFunction(key, activationFunction);
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }
    public SimpleMatrix[] getWeights() {
        return weights;
    }

    public void setWeights(SimpleMatrix[] weights) {
        this.weights = weights;
    }

    public SimpleMatrix[] getBiases() {
        return biases;
    }

    public void setBiases(SimpleMatrix[] biases) {
        this.biases = biases;
    }

    public int[] getLayers(){
        return layers;
    }
}
