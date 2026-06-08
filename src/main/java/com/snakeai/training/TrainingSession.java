package com.snakeai.training;

import com.snakeai.config.TrainingConfig;
import com.snakeai.domain.board.Position;
import com.snakeai.domain.game.Direction;
import com.snakeai.domain.game.GameEngine;
import com.snakeai.domain.game.GameState;
import com.snakeai.evolution.agent.SnakeAgent;
import com.snakeai.evolution.encoder.GameStateEncoder;
import com.snakeai.evolution.encoder.HeadCenteredLocalVisionEncoder;
import com.snakeai.evolution.fitness.BalancedFitnessStrategy;
import com.snakeai.evolution.fitness.FitnessEvaluatorAdapter;
import com.snakeai.evolution.fitness.FitnessStrategy;
import com.snakeai.evolution.fitness.IndividualEvaluationResult;
import com.snakeai.genetic.algorithm.GeneticAlgorithm;
import com.snakeai.genetic.algorithm.Individual;
import com.snakeai.genetic.algorithm.Population;
import com.snakeai.neural.network.NeuralNetwork;
import com.snakeai.neural.network.NeuralNetworkFactory;
import com.snakeai.persistence.io.TrainingPersistence;
import com.snakeai.persistence.model.SavedEliteIndividual;
import com.snakeai.persistence.model.TrainingCheckpoint;
import com.snakeai.persistence.model.TrainingMetadata;
import com.snakeai.replay.ReplayPersistence;
import com.snakeai.replay.ReplayRecord;
import com.snakeai.statistics.GenerationStatistics;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrainingSession implements Runnable {
    private final String name;
    private final TrainingConfig config;
    private final String creationDate;
    private final List<TrainingSessionListener> listeners;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private final GenerationStatistics statistics;
    private final Random random;

    private int currentGeneration;
    private double bestFitnessEver;
    private int bestScoreEver;
    private int bestScoreGeneration;
    private Population currentPopulation;

    // Adapters / Strategies
    private final GameStateEncoder encoder;
    private final FitnessStrategy fitnessStrategy;

    public TrainingSession(String name, TrainingConfig config) {
        this.name = name;
        this.config = config;
        this.creationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.listeners = new ArrayList<>();
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.statistics = new GenerationStatistics();
        this.random = new Random();
        this.currentGeneration = 0;
        this.bestFitnessEver = 0.0;
        this.bestScoreEver = 0;
        this.bestScoreGeneration = 0;

        this.encoder = new HeadCenteredLocalVisionEncoder();
        this.fitnessStrategy = new BalancedFitnessStrategy();

        initializeNewTraining();
    }

    public TrainingSession(TrainingCheckpoint checkpoint) {
        TrainingMetadata metadata = checkpoint.metadata();
        this.name = metadata.name();
        this.config = metadata.config();
        this.creationDate = metadata.creationDate();
        this.listeners = new ArrayList<>();
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.statistics = new GenerationStatistics(checkpoint.statisticsHistory());
        this.random = new Random();
        this.currentGeneration = metadata.currentGeneration();
        this.bestFitnessEver = metadata.bestFitness();
        this.bestScoreEver = metadata.bestScore();
        this.bestScoreGeneration = metadata.bestScoreGeneration();

        this.encoder = new HeadCenteredLocalVisionEncoder();
        this.fitnessStrategy = new BalancedFitnessStrategy();

        initializeFromElite(checkpoint.elite());
    }

    private void initializeNewTraining() {
        int inputSize = encoder.getInputSize(config);
        List<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < config.populationSize(); i++) {
            NeuralNetwork nn = NeuralNetworkFactory.createRandomNetwork(inputSize, config, random);
            individuals.add(new Individual(nn.getWeights()));
        }
        this.currentPopulation = new Population(individuals);
    }

    private void initializeFromElite(List<SavedEliteIndividual> eliteList) {
        // We restore the elite individuals
        List<Individual> parents = new ArrayList<>();
        for (SavedEliteIndividual saved : eliteList) {
            Individual ind = new Individual(saved.genome());
            ind.setFitness(saved.fitness());
            parents.add(ind);
        }
        Population parentsPopulation = new Population(parents);

        // Perform crossover and mutation on these parents to reconstruct the full population
        GeneticAlgorithm ga = new GeneticAlgorithm(
                config.populationSize(),
                config.elitismCount(),
                Math.min(config.tournamentSize(), parents.size()),
                config.mutationRate(),
                config.mutationAmplitude(),
                config.evolutionMode(),
                config.eliteSelectionMode(),
                random
        );
        this.currentPopulation = ga.evolve(parentsPopulation);
    }

    public void addListener(TrainingSessionListener listener) {
        this.listeners.add(listener);
    }

    public String getName() {
        return name;
    }

    public TrainingConfig getConfig() {
        return config;
    }

    public GenerationStatistics getStatistics() {
        return statistics;
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public double getBestFitnessEver() {
        return bestFitnessEver;
    }

    public int getBestScoreEver() {
        return bestScoreEver;
    }

    public int getBestScoreGeneration() {
        return bestScoreGeneration;
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void stop() {
        this.running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        running.set(true);

        try {
            while (running.get()) {
                if (paused.get()) {
                    Thread.sleep(100);
                    continue;
                }

                long genStart = System.nanoTime();

                // --- Phase 1: Fitness Evaluation ---
                long evalStart = System.nanoTime();
                evaluateGeneration();
                long evalMs = (System.nanoTime() - evalStart) / 1_000_000;

                // --- Phase 2: Persistence (checkpoint to disk) ---
                long saveStart = System.nanoTime();
                saveCheckpoint();
                long saveMs = (System.nanoTime() - saveStart) / 1_000_000;

                currentGeneration++;

                // --- Phase 3: Evolution (crossover + mutation) ---
                long evoStart = System.nanoTime();
                GeneticAlgorithm ga = new GeneticAlgorithm(
                        config.populationSize(),
                        config.elitismCount(),
                        config.tournamentSize(),
                        config.mutationRate(),
                        config.mutationAmplitude(),
                        config.evolutionMode(),
                        config.eliteSelectionMode(),
                        random
                );
                currentPopulation = ga.evolve(currentPopulation);
                long evoMs = (System.nanoTime() - evoStart) / 1_000_000;

                long totalMs = (System.nanoTime() - genStart) / 1_000_000;

                // --- Timing Report ---
                System.out.printf(
                    "[GEN %04d] Total: %dms | Fitness: %dms (%.1f%%) | Persist: %dms (%.1f%%) | Evolution: %dms (%.1f%%)%n",
                    currentGeneration - 1,
                    totalMs,
                    evalMs,  (totalMs > 0 ? evalMs * 100.0 / totalMs : 0),
                    saveMs,  (totalMs > 0 ? saveMs * 100.0 / totalMs : 0),
                    evoMs,   (totalMs > 0 ? evoMs  * 100.0 / totalMs : 0)
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            notifyListenersOnError("Error during training loop", e);
        } finally {
            running.set(false);
            notifyListenersOnFinished();
        }
    }


    private void evaluateGeneration() {
        long seed = random.nextLong();
        FitnessEvaluatorAdapter evaluator = new FitnessEvaluatorAdapter(config, fitnessStrategy, seed);

        List<Individual> individuals = currentPopulation.getIndividuals();
        int numThreads = Math.max(1, config.evaluationThreads());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Build one Callable per individual — each is fully independent
        List<Callable<IndividualEvaluationResult>> tasks = new ArrayList<>(individuals.size());
        for (Individual individual : individuals) {
            tasks.add(() -> evaluator.evaluate(individual));
        }

        // Submit all tasks and collect futures
        List<Future<IndividualEvaluationResult>> futures;
        try {
            futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            throw new RuntimeException("Evaluation interrupted", e);
        } finally {
            executor.shutdown();
        }

        // Collect results — iterate in same order as individuals list
        double sumFitness = 0.0;
        double bestFitness = -1.0;
        double worstFitness = Double.MAX_VALUE;
        Individual bestIndividual = null;
        int bestScore = 0;

        for (int i = 0; i < individuals.size(); i++) {
            Individual individual = individuals.get(i);
            IndividualEvaluationResult result;
            try {
                result = futures.get(i).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Result collection interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Error evaluating individual", e.getCause());
            }

            double fitness = result.averageFitness();
            individual.setFitness(fitness);

            sumFitness += fitness;
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestIndividual = individual;
            }
            if (fitness < worstFitness) {
                worstFitness = fitness;
            }

            // Score comes directly from the first evaluation run — no extra simulation needed
            int score = result.scoreFromFirstRun();
            if (score > bestScore) {
                bestScore = score;
            }
        }

        double averageFitness = sumFitness / config.populationSize();

        currentPopulation.sortByFitnessDescending();
        double sumEliteFitness = 0.0;
        int numElites = Math.min(config.elitismCount(), individuals.size());
        for (int i = 0; i < numElites; i++) {
            sumEliteFitness += individuals.get(i).getFitness();
        }
        double averageEliteFitness = numElites > 0 ? sumEliteFitness / numElites : 0.0;

        boolean newFitnessRecord = false;
        if (bestFitness > bestFitnessEver) {
            bestFitnessEver = bestFitness;
            newFitnessRecord = true;
        }
        if (bestScore > bestScoreEver) {
            bestScoreEver = bestScore;
            bestScoreGeneration = currentGeneration;
        }

        GenerationSummary summary = new GenerationSummary(
                currentGeneration,
                bestFitness,
                averageFitness,
                worstFitness,
                bestScore,
                bestFitnessEver,
                averageEliteFitness
        );
        statistics.addSummary(summary);

        // Somente salva replay se houve NOVO recorde de FITNESS.
        if (newFitnessRecord && bestIndividual != null) {
            saveBestIndividualReplay(bestIndividual, seed, bestScore);
        }

        notifyListenersOnGeneration(summary);
    }


    private void saveBestIndividualReplay(Individual individual, long seed, int finalScore) {
        NeuralNetwork brain = NeuralNetworkFactory.createNetwork(
                encoder.getInputSize(config),
                config.hiddenLayerSizes(),
                4
        );
        brain.setWeights(individual.getGenome());
        SnakeAgent agent = new SnakeAgent(brain, encoder, config);

        GameEngine engine = new GameEngine(config.boardWidth(), config.boardHeight(), seed);
        GameState state = engine.getGameState();

        Position initialFood = state.getFood();
        List<Direction> actions = new ArrayList<>();

        while (!state.isGameOver()) {
            Direction dir = agent.chooseDirection(state);
            actions.add(dir);
            engine.step(dir);
        }

        ReplayRecord record = new ReplayRecord(
                name,
                currentGeneration,
                seed,
                initialFood,
                actions,
                finalScore
        );

        try {
            Path replayFile = TrainingPersistence.getReplayDirectory(name)
                    .resolve(String.format("gen-%04d-best.replay", currentGeneration));
            ReplayPersistence.saveReplay(record, replayFile);
        } catch (IOException e) {
            System.err.println("Failed to save replay: " + e.getMessage());
        }
    }

    private void saveCheckpoint() {
        currentPopulation.sortByFitnessDescending();

        List<SavedEliteIndividual> elite = new ArrayList<>();
        int count = Math.min(config.elitismCount(), currentPopulation.getIndividuals().size());
        for (int i = 0; i < count; i++) {
            Individual ind = currentPopulation.getIndividuals().get(i);
            elite.add(new SavedEliteIndividual(ind.getGenome().clone(), ind.getFitness()));
        }

        TrainingMetadata metadata = new TrainingMetadata(
                name,
                creationDate,
                currentGeneration,
                bestFitnessEver,
                bestScoreEver,
                bestScoreGeneration,
                config
        );

        TrainingCheckpoint checkpoint = new TrainingCheckpoint(
                metadata,
                elite,
                statistics.getHistory()
        );

        try {
            TrainingPersistence.saveCheckpoint(checkpoint);
        } catch (IOException e) {
            notifyListenersOnError("Failed to save checkpoint", e);
        }
    }

    private void notifyListenersOnGeneration(GenerationSummary summary) {
        for (TrainingSessionListener listener : listeners) {
            listener.onGenerationCompleted(summary);
        }
    }

    private void notifyListenersOnFinished() {
        for (TrainingSessionListener listener : listeners) {
            listener.onTrainingFinished();
        }
    }

    private void notifyListenersOnError(String msg, Throwable t) {
        for (TrainingSessionListener listener : listeners) {
            listener.onTrainingError(msg, t);
        }
    }
}
