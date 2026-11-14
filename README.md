# 🚀 Space Defender

Jogo mobile desenvolvido em **Kotlin para Android**, utilizando o controle por movimento do dispositivo para movimentação de uma nave espacial em um ambiente 2D.

O projeto utiliza arquitetura MVVM, banco de dados local, integração com sensores do dispositivo Android e funcionalidade baseada em mapas.

---

## 1. Visão geral do sistema

O Space Defender é um jogo 2D no qual o jogador controla uma nave espacial por meio da inclinação do celular. O objetivo é destruir meteoros utilizando disparos, evitar colisões e acumular pontos durante as partidas.

A aplicação será organizada em módulos responsáveis pela interface, lógica do jogo, captura dos dados do sensor, persistência das pontuações e visualização de mapas.

### Principais funcionalidades

- Controle da nave por meio do acelerômetro;
- Movimentação e geração de meteoros;
- Sistema de disparos;
- Detecção de colisões;
- Gerenciamento de vidas;
- Cálculo e exibição da pontuação;
- Armazenamento e consulta de recordes;
- Visualização de mapa com localização de missões;
- Gerenciamento do estado da partida utilizando MVVM.

---

## 2. Arquitetura do sistema — MVVM

O aplicativo utilizará o padrão arquitetural **Model-View-ViewModel (MVVM)** para separar a interface gráfica, as regras de funcionamento e o gerenciamento dos dados.

A arquitetura será organizada em três componentes principais:

### View

Responsável pela apresentação visual e interação com o jogador.

Classes previstas:
- `MainActivity.kt`: tela principal e navegação;
- `GameActivity.kt`: tela da partida;
- `GameView.kt`: renderização dos elementos gráficos do jogo;
- `MapActivity.kt`: apresentação da funcionalidade de mapas.

### ViewModel

Responsável pelo gerenciamento do estado da aplicação e pela comunicação entre a interface e as regras do sistema.

Classe principal:
- `GameViewModel.kt`

Responsabilidades:
- Gerenciar o início e o encerramento das partidas;
- Atualizar a posição lógica da nave;
- Controlar vidas e pontuação;
- Gerenciar os eventos do jogo;
- Solicitar o armazenamento dos resultados;
- Disponibilizar os dados atualizados para a interface.

### Model

Representa os dados e componentes responsáveis pelas regras e operações do aplicativo.

Classes previstas:
- `Spaceship.kt`;
- `Meteor.kt`;
- `Bullet.kt`;
- `GameRepository.kt`;
- `ScoreEntity.kt`;
- `ScoreDao.kt`;
- `GameDatabase.kt`.

### Fluxo de comunicação

```text
             VIEW
       MainActivity
       GameActivity
        GameView
             |
             v
          VIEWMODEL
       GameViewModel
             |
       +-----+------+
       |            |
       v            v
    MODEL        REPOSITORY
       |            |
       |            v
       |       BANCO ROOM
       |
       +-------------------
       |
       v
   REGRAS DO JOGO
```

---

## 3. Estrutura de diretórios e classes

A organização inicial do código será:

```text
SpaceDefender/
│
├── app/
│   │
│   ├── src/
│   │   │
│   │   ├── main/
│   │   │   │
│   │   │   ├── java/com/example/spacedefender/
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   ├── GameDatabase.kt
│   │   │   │   │   ├── ScoreDao.kt
│   │   │   │   │   ├── ScoreEntity.kt
│   │   │   │   │   └── GameRepository.kt
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── Spaceship.kt
│   │   │   │   │   ├── Meteor.kt
│   │   │   │   │   ├── Bullet.kt
│   │   │   │   │   └── GameState.kt
│   │   │   │   │
│   │   │   │   ├── view/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── GameActivity.kt
│   │   │   │   │   ├── GameView.kt
│   │   │   │   │   └── MapActivity.kt
│   │   │   │   │
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── GameViewModel.kt
│   │   │   │   │
│   │   │   │   ├── sensor/
│   │   │   │   │   └── AccelerometerManager.kt
│   │   │   │   │
│   │   │   │   ├── game/
│   │   │   │   │   ├── GameEngine.kt
│   │   │   │   ├── CollisionManager.kt
│   │   │   │   └── MeteorSpawner.kt
│   │   │   │
│   │   │   └── map/
│   │   │       └── MissionMapManager.kt
│   │   │
│   │   └── res/
│   │       ├── drawable/
│   │       ├── layout/
│   │       ├── mipmap/
│   │       └── values/
│   │
│   └── src/test/
│       └── java/com/example/spacedefender/
│           ├── GameViewModelTest.kt
│           ├── CollisionManagerTest.kt
│           └── ScoreTest.kt
│
└── README.md
```

A estrutura poderá receber ajustes conforme a implementação das funcionalidades.

---

## 4. Funcionamento e implementação das funcionalidades

### 4.1. Controle da nave por acelerômetro

O controle da nave será realizado utilizando o acelerômetro do dispositivo Android, responsável por fornecer dados de aceleração nos eixos X, Y e Z.

A classe `AccelerometerManager.kt` será responsável por acessar o sensor por meio da API `SensorManager` do Android e receber os eventos de movimento.

O valor identificado no eixo X será utilizado para determinar a direção do deslocamento horizontal da nave.

**Fluxo de execução:**

```text
Movimento do celular
        |
        v
AccelerometerManager
        |
        v
Leitura do eixo X
        |
        v
GameViewModel
        |
        v
Atualização da posição
        |
        v
GameView
        |
        v
Renderização da nave
```

A movimentação deverá considerar limites horizontais para impedir que a nave ultrapasse as extremidades da tela.

**Classe responsável:** `AccelerometerManager.kt`

**Integração:** `GameViewModel.kt` e `GameView.kt`.

### 4.2. Renderização e movimentação dos elementos

A classe `GameView.kt` será responsável pela representação gráfica do ambiente de jogo.

Ela utilizará recursos de desenho do Android, como `Canvas` e `Paint`, para representar:

- Nave espacial;
- Meteoros;
- Disparos;
- Cenário e elementos visuais;
- Pontuação e vidas.

A classe `GameEngine.kt` será responsável pela atualização da lógica da partida, incluindo posições, movimentação e atualização dos objetos.

**Classes responsáveis:**
- `GameView.kt`;
- `GameEngine.kt`;
- `Spaceship.kt`;
- `Meteor.kt`;
- `Bullet.kt`.

### 4.3. Geração e movimentação dos meteoros

A geração dos meteoros será controlada pela classe `MeteorSpawner.kt`.

Essa classe será responsável por criar novos meteoros em posições horizontais variadas, definindo suas características e velocidade.

A classe `Meteor.kt` representará cada meteoro individualmente, armazenando informações como:

- Posição horizontal;
- Posição vertical;
- Velocidade;
- Dimensões;
- Estado de existência no jogo.

Os meteoros serão movimentados pelo motor do jogo até saírem da área visível ou serem destruídos.

**Classes responsáveis:** `MeteorSpawner.kt`, `Meteor.kt` e `GameEngine.kt`.

### 4.4. Sistema de disparos

A nave poderá realizar disparos para destruir os meteoros.

A classe `Bullet.kt` representará cada projétil, armazenando sua posição e velocidade.

O `GameEngine.kt` atualizará a movimentação dos disparos e a `CollisionManager.kt` verificará se houve contato com algum meteoro.

Quando ocorrer uma colisão válida, o meteoro e o disparo serão removidos da partida, e a pontuação será atualizada.

**Classes responsáveis:** `Bullet.kt`, `GameEngine.kt` e `CollisionManager.kt`.

### 4.5. Detecção de colisões

A classe `CollisionManager.kt` será responsável por verificar as interações entre os elementos do jogo.

Serão consideradas duas situações principais:

**Colisão entre disparo e meteoro:**
- Identificar a interseção entre os elementos;
- Remover o disparo;
- Destruir o meteoro;
- Atualizar a pontuação.

**Colisão entre nave e meteoro:**
- Identificar o contato;
- Reduzir uma vida;
- Atualizar o estado da partida.

**Classes responsáveis:** `CollisionManager.kt`, `GameEngine.kt` e `GameViewModel.kt`.

### 4.6. Gerenciamento da partida e pontuação

O `GameViewModel.kt` será responsável por gerenciar as informações principais da partida.

A classe `GameState.kt` representará o estado atual do jogo, contendo informações como:

- Pontuação atual;
- Quantidade de vidas;
- Estado da partida;
- Posição e informações necessárias para atualização da interface.

A pontuação será incrementada quando um meteoro for destruído.

Quando a quantidade de vidas chegar a zero, o estado da partida será alterado para encerrado.

**Classes responsáveis:** `GameState.kt`, `GameViewModel.kt` e `GameEngine.kt`.

---

## 5. Persistência de dados — Room

O aplicativo utilizará a biblioteca **Room**, que fornece uma camada de abstração sobre o SQLite para armazenamento local de informações.

O banco de dados será utilizado para armazenar as pontuações obtidas nas partidas e permitir a consulta dos recordes.

A implementação será organizada em três componentes principais:

### ScoreEntity.kt

Representará a tabela de pontuações do banco de dados.

A entidade poderá conter:
- `id`: identificador do registro;
- `score`: pontuação obtida;
- `date`: data da partida.

### ScoreDao.kt

Será responsável pelas operações de acesso aos dados.

Operações previstas:
- Inserir uma nova pontuação;
- Consultar as pontuações;
- Recuperar os melhores resultados.

### GameDatabase.kt

Será responsável por definir e disponibilizar a instância do banco de dados Room.

### GameRepository.kt

Será responsável por intermediar o acesso aos dados, conectando o ViewModel às operações do DAO.

**Fluxo de persistência:**

```text
Fim da partida
      |
      v
GameViewModel
      |
      v
GameRepository
      |
      v
ScoreDao
      |
      v
GameDatabase
      |
      v
Armazenamento SQLite
```

Dessa forma, os recordes poderão ser recuperados posteriormente, inclusive após o encerramento do aplicativo.

---

## 6. Integração com mapas

O aplicativo contará com uma funcionalidade complementar de mapas, utilizando a biblioteca MapLibre.

A proposta é disponibilizar uma tela de missões, na qual o mapa será utilizado para apresentar pontos de exploração relacionados às fases do jogo.

A classe `MapActivity.kt` será responsável pela interface da funcionalidade, enquanto `MissionMapManager.kt` concentrará a configuração e o gerenciamento dos elementos do mapa.

A implementação poderá incluir:

- Exibição do mapa;
- Marcadores representando missões;
- Identificação dos pontos disponíveis;
- Seleção de uma missão pelo usuário.

**Fluxo de execução:**

```text
Tela principal
      |
      v
MapActivity
      |
      v
MissionMapManager
      |
      v
MapLibre
      |
      v
Exibição do mapa
      |
      v
Seleção de missão
```

A funcionalidade de mapas será integrada ao aplicativo como um módulo independente da lógica principal da partida.

---

## 7. Testes unitários

Os testes unitários serão utilizados para validar as regras de funcionamento implementadas nas classes de lógica do aplicativo.

A estrutura de testes ficará localizada em `src/test/`.

As principais verificações serão:

| Classe de teste | Funcionalidade validada |
|---|---|
| `GameViewModelTest.kt` | Gerenciamento da partida, vidas e pontuação |
| `CollisionManagerTest.kt` | Identificação de colisões |
| `ScoreTest.kt` | Cálculo e atualização da pontuação |

Os testes serão implementados utilizando JUnit, verificando os resultados esperados para diferentes condições de execução.

---

## 8. Tecnologias e bibliotecas

| Tecnologia | Aplicação no projeto |
|---|---|
| Kotlin | Linguagem de programação |
| Android Studio | Ambiente de desenvolvimento |
| Android SDK | Recursos da plataforma Android |
| MVVM | Organização arquitetural |
| Room | Persistência de dados |
| SQLite | Armazenamento local |
| SensorManager | Acesso ao acelerômetro |
| MapLibre | Implementação dos mapas |
| JUnit | Testes unitários |
| Git e GitHub | Versionamento e documentação |

---

## 9. Fluxo de funcionamento integrado

O fluxo abaixo representa a interação entre os principais componentes do aplicativo:

```text
                MAIN ACTIVITY
                      |
            +---------+---------+
            |                   |
            v                   v
        GAME ACTIVITY       MAP ACTIVITY
            |                   |
            v                   v
         GAME VIEW         MISSION MAP
            |               MANAGER
            |                   |
            v                   v
       GAME VIEWMODEL         MAPLIBRE
            |
     +------+-------+
     |              |
     v              v
 ACCELEROMETER   GAME ENGINE
 MANAGER             |
     |          +----+------+
     |          |           |
     v          v           v
  SENSOR     METEORS     COLLISIONS
                |           |
                +-----+-----+
                      |
                      v
                  SCORE
                      |
                      v
                 REPOSITORY
                      |
                      v
                    ROOM
                      |
                      v
               RECORDES SALVOS
```

---

## 10. Considerações técnicas

O desenvolvimento do Space Defender será baseado na separação de responsabilidades entre os componentes da aplicação.

A arquitetura MVVM organizará a comunicação entre interface e gerenciamento dos dados. O motor do jogo ficará responsável pela atualização dos elementos e pelas regras de movimentação e colisão. O acelerômetro fornecerá os dados necessários para o controle da nave, enquanto o Room permitirá persistir as pontuações. A integração com o MapLibre disponibilizará a funcionalidade de mapas em uma tela complementar.

Essa organização busca facilitar a manutenção, a compreensão e a evolução do código-fonte.
