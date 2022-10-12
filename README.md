# **MechPeste**

Esse pequeno Mod de funções utiliza o mod KRPC como ponte de conexão para o game Kerbal Space
Program de modo a poder controlar os foguetes e naves utilizando scripts de automação e navegação.
Há funções de decolagem orbital, pouso automático (suicide burn), manobras, e pilotagem automática
de rovers.

## **Requisitos**
---
É necessário ter o [Java](https://java.com/pt-BR/) atualizado para rodar o arquivo "MechPeste.jar" e
também ter o mod [KRPC](https://github.com/krpc/krpc/releases/download/v0.4.8/krpc-0.4.8.zip)
instalado na pasta do Kerbal.

## **Como utilizar:**
---
Baixe a versão mais recente na aba de Releases ao lado >> e depois é so abrir o arquivo "
MechPeste.jar".
Se o jogo estiver aberto ele tentará se conectar ao mod KRPC e então é só clicar numa das funções
para utilizar com a nave.

Caso ainda não tenha criado um server, clique em "Add Server" e depois em "Start Server" dentro do
jogo no mod KRPC, assim o MechPeste conseguirá se conectar.

## *Funções*

### *Decolagem Orbital*

Digite a altura de órbita final que o script tentará alcançar, e também a direção da curva
gravitacional em volta do planeta (de 0 a 359 graus, 90 sendo a direção Leste). Clique em iniciar e
aguarde o lançamento e execução das manobras.

### *Pouso Automático*

Inicie o Script assim que estiver pretendendo pousar o foguete. Ele inicia a primeira ignição abaixo
de 10km e depois calcula e pousa suavamente próximo ao solo no último momento.
A função "Sobrevoar área" tenta fazer com que a nave sobrevoe onde está na altura estipulada.

### *Manobras*

Essa função consegue executar a próxima manobra criada, ou então tenta circularizar a órbita atual
no Apoastro ou no Periastro.

### *Auto Rover*

Esse script faz um Rover seguir uma rota até um determinado Marcador ou então Alvo selecionado pelo
jogador. Escolha qual o tipo de alvo a seguir, e no caso de Marcador digite o nome dele na caixa de
texto e também a velocidade máxima que o rover pode alcançar. Clique em iniciar e ele comecará a se
mover para o alvo.