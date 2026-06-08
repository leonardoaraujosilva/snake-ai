package com.snakeai.evolution.fitness;

public class BalancedFitnessStrategy implements FitnessStrategy {

    // Constantes da nova fórmula
    private static final double FOOD_REWARD = 10000.0;
    private static final double STEP_REWARD = 0.1;
    private static final double CLOSER_REWARD = 1.0;
    private static final double AWAY_PENALTY = 1.5;

    @Override
    public double calculateFitness(GameSimulationResult result) {
        double fitness = 0.0;

        // 1. RECOMPENSA PRINCIPAL: Comer Comida
        // Garante que uma cobra com N comidas sempre terá fitness maior que uma com N-1.
        fitness += result.score() * FOOD_REWARD;

        // 2. BÔNUS DE EFICIÊNCIA
        // Se a cobra come a comida mais rápido, ela ganha os pontos que sobraram no contador.
        fitness += result.totalEfficiencyScore();

        // 3. RECOMPENSA DE SOBREVIVÊNCIA (Muito menor agora)
        // Serve apenas de desempate minúsculo para cobras que comeram a mesma quantia.
        fitness += result.totalSteps() * STEP_REWARD;

        // 4. GUIAMENTO INICIAL (Aproximação vs Afastamento)
        // Ajuda nas primeiras gerações a entender que ir para a maçã é bom,
        // mas não pontua o suficiente para superar o FOOD_REWARD.
        fitness += result.stepsMovingCloserToFood() * CLOSER_REWARD;
        fitness -= result.stepsMovingAwayFromFood() * AWAY_PENALTY;

        // 5. PENALIDADES
        if (result.cycleDetected()) {
            fitness -= 150.0;
        }

        if (result.timeLimitExceeded()) {
            fitness -= 100.0;
        }

        // Penalidade por morrer logo de cara sem comer nada
        if (result.score() == 0 && (result.cycleDetected() || result.timeLimitExceeded() || result.totalSteps() < 10)) {
            fitness -= 50.0;
        }

        // Evitar fitness negativo no algoritmo genético
        return Math.max(0.1, fitness);
    }
}
