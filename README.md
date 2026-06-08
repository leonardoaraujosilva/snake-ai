# Snake AI — Algoritmos Genéticos & Redes Neurais

Este é um projeto de estudo completo, escrito em **Java 21** e estruturado com **Maven**, projetado para servir como um guia educacional prático sobre a evolução de agentes autônomos.

O objetivo do sistema é treinar uma Inteligência Artificial para jogar o clássico jogo **Snake** utilizando uma **Rede Neural Feed-Forward** e um **Algoritmo Genético**, sem o uso de bibliotecas externas de Machine Learning ou Reinforcement Learning.

---

## 🏗️ Arquitetura do Software

A estrutura do projeto foi desenhada sob os princípios do **Clean Code** e da **Separação de Responsabilidades (SoC)**. O motor genético e a rede neural são totalmente desacoplados do jogo Snake, facilitando a reutilização de componentes em outros problemas.

```
src/main/java/com/snakeai/
├── config/                  # Configurações centralizadas imutáveis
├── domain/                  # Lógica de negócio do jogo Snake pura
│   ├── board/               # Abstrações do tabuleiro (Board, Position, Cell)
│   └── game/                # Motor de jogo e regras (Snake, GameEngine, GameState)
├── neural/                  # Rede neural Feed-Forward genérica
│   ├── activation/          # ReLU e Softmax
│   ├── layer/               # DenseLayer com mapeamento unidimensional de pesos
│   └── network/             # NeuralNetwork e NeuralNetworkFactory
├── genetic/                 # Algoritmo genético agnóstico
│   ├── algorithm/           # Individual, Population e o motor principal
│   ├── selection/           # Seleção por Torneio
│   ├── crossover/           # Crossover Uniforme
│   └── mutation/            # Mutação Gaussiana
├── evolution/               # Adaptadores para conectar o Genético ao Snake
│   ├── agent/               # SnakeAgent guiado pela rede neural
│   ├── encoder/             # GameStateEncoder (HeadCenteredLocalVisionEncoder)
│   └── fitness/             # Estratégia de avaliação (BalancedFitnessStrategy)
├── training/                # Orquestração assíncrona do treinamento
├── persistence/             # Persistência de checkpoints JSON e estatísticas CSV
├── replay/                  # Gravação e reprodução determinística de replays
├── statistics/              # Métricas e histórico de evolução
└── ui/                      # Interface desktop Swing moderna
```

---

## 🧬 Fluxo do Algoritmo Genético

O treinamento segue um fluxo evolutivo de gerações:

1. **População Inicial**: Criada com $1000$ indivíduos com pesos e biases de conexões neurais inicializados com distribuição Gaussiana.
2. **Avaliação (Fitness)**: Cada indivíduo executa o jogo até colidir, atingir um loop ou estourar o limite de tempo.
3. **Elitismo**: Os $20$ melhores indivíduos são preservados intactos para a próxima geração.
4. **Seleção**: É realizado o **Torneio de tamanho 5** para escolher os pais.
5. **Cruzamento (Crossover Uniforme)**: Cada peso/bias do descendente tem $50\%$ de chance de vir de qualquer um dos pais.
6. **Mutação Gaussiana**: Cada peso possui $2\%$ de chance de sofrer uma mutação somando um valor baseado em `Gaussian * Amplitude (0.3)`.

---

## 🧠 Rede Neural do Agente

Cada cobra possui um cérebro implementado como uma Rede Neural Artificial Feed-forward:
* **Entradas (121)**: Visão local de $11\times11$ centralizada na cabeça da cobra.
  * `0.00` = Vazio | `0.25` = Parede (incluindo fora do tabuleiro) | `0.50` = Corpo | `0.75` = Cabeça | `1.00` = Comida
* **Camadas Ocultas**: $[128, 64, 32]$ utilizando ativação **ReLU**.
* **Saídas (4)**: Mapeado via **Softmax** representando as direções: `UP`, `RIGHT`, `DOWN`, `LEFT`.

---

## ⚙️ Persistência, Estatísticas e Replays

* **Checkpoints**: Salvam apenas a elite da população e os metadados necessários no diretório `trainings/<nome>/checkpoint.json`. Ao continuar um treinamento, os indivíduos da elite são usados como pais para re-gerar a população de 1000 indivíduos.
* **Estatísticas**: Gravadas em `statistics.csv` contendo os dados de `generation,bestFitness,averageFitness,worstFitness,bestScore` a cada geração.
* **Replays**: Sempre que um novo recorde de fitness ou score é quebrado, as ações e a seed geradora são salvas no diretório `replays/` para reprodução determinística posterior.

---

## 🚀 Como Executar

### Pré-requisitos
* Java JDK 21+ instalado e configurado.
* Maven instalado.

### Passos
1. No diretório raiz do projeto, execute a compilação:
   ```bash
   mvn clean package
   ```
2. Inicie a aplicação Swing:
   ```bash
   java -cp target/snake-ai-1.0-SNAPSHOT.jar com.snakeai.ui.SnakeAIApplication
   ```

---

## 🛠️ Como adaptar o motor genético para outros projetos

O módulo `genetic` é completamente agnóstico e opera apenas com arrays de `double[]` (genes). Se você deseja treinar um modelo para outro problema (como robôs virtuais ou jogos de plataforma):
1. Crie uma classe de modelo que herde/utilize o módulo `genetic`.
2. Implemente a interface `FitnessEvaluator`. Dentro dela:
   * Converta o genoma (`double[]`) para os pesos de seu modelo neural ou lógica de decisão.
   * Execute a simulação do seu problema.
   * Retorne a pontuação de fitness.
3. Instancie o `GeneticAlgorithm` passando os parâmetros de população e mutação desejados.
4. Execute `ga.evolve(populacao)` a cada geração.
