# **MechPeste**

Esse pequeno Mod de funções utiliza o mod KRPC como ponte de conexão para o game Kerbal Space Program de modo a poder controlar os foguetes e naves utilizando scripts de automação e navegação.
Há funções de decolagem orbital, pouso automático (suicide burn), manobras, e pilotagem automática de rovers.

## **Requisitos**
---
É necessário ter o [Java](https://java.com/pt-BR/) atualizado para rodar o arquivo "MechPeste.jar" e também ter o mod [KRPC](https://github.com/krpc/krpc/releases/download/v0.4.8/krpc-0.4.8.zip) instalado na pasta do Kerbal.

## **Como utilizar:**
---
Abra o arquivo "MechPeste.jar" e clique numa das funções para utilizar com a nave após a conexão com o servidor do KRPC dentro do jogo (adicione um novo server pelo KRPC).

### *Decolagem Orbital*
Digite a altura de órbita final que o script tentará alcançar, e também a direção da curva gravitacional em volta do planeta (de 0 a 359 graus, 90 sendo a direção Leste). Clique em iniciar e aguarde o lançamento e execução das manobras.

### *Suicide Burn*
Esse script tem parâmetros dos controladores PID que podem ser modificados, mas não é recomendado. Inicie o Script assim que estiver pretendendo pousar o foguete. Ele inicia a primeira ignição abaixo de 10km e depois calcula e pousa suavamente próximo ao solo no último momento.

### *Manobras*
Esse script apenas executa a manobra disponível atualmente na nave.

### *Auto Rover*
Esse script faz um Rover seguir uma rota até um determinado Marcador ou então Alvo selecionado pelo jogador. Escolha qual o tipo de alvo a seguir, e no caso de Marcador digite o nome dele na caixa de texto e também a velocidade máxima que o rover pode alcançar. Clique em iniciar e ele comecará a se mover para o alvo.