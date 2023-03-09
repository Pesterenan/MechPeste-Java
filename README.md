# **MechPeste**

Este aplicativo utiliza o mod KRPC como ponte de conexão para o game Kerbal Space
Program de modo a poder controlar os foguetes e naves utilizando scripts de automação e navegação.

É possivel decolar seus foguetes automaticamente, além de realizar pousos automáticos, executar
manobras, e pilotar rovers.

## **Requisitos:**

---
É necessário ter o [Java](https://java.com/pt-BR/) instalado para utilizar o "MechPeste.jar".
Uma versão mais atualizada do
mod [KRPC](https://github.com/krpc/krpc/releases/download/v0.5.1/krpc-0.5.1.zip)
pode ser instalada diretamente do aplicativo MechPeste pelo menu Arquivo > Instalar KRPC.

## **Como instalar:**

---
Baixe a versão mais recente na aba de Releases ao lado >> e depois abra o arquivo "MechPeste.jar".

### Primeiro Uso:

O MechPeste funciona em modo Janela, se o jogo estiver aberto ele tentará se conectar ao mod KRPC.
Caso ainda não tenha criado um server, clique em **"Add Server"** e depois em **"Start Server"**
dentro
do jogo no mod KRPC, assim o MechPeste conseguirá se conectar.

> Habilite também a auto-conexão nas opções avançadas:
> Clique em "Show advanced settings", e habilite "Auto-start server" e "Auto-accept new clients",
> assim a conexão será automática.

## *Funções*

---

### *Decolagem Orbital*

Digite a altitude final do apoastro que o script tentará alcançar, e também a direção da curva
gravitacional em volta do planeta (de 0 a 359 graus, 90 sendo a direção Leste). Clique em iniciar e

A Rolagem indica pra qual direção do foguete irá virar para a direção estabelecida.
Escolha o modelo de curva, sendo do mais brando (Sinusoidal) até o mais extremo (Exponencial).

Escolha abrir painéis, e utilizar coifas, e se quer soltar os estágios durante a decolagem.
Cliquem em Decolagem e aguarde o lançamento e execução das manobras.

### *Pouso Automático*

Inicie o Script e o aplicativo tentará primeiro baixar sua órbita, até o periastro zerar, e após
isso, ficará calculando a distância até o último momento para começar a desacelerar.
Durante o pouso, o foguete se orientará automaticamente pra cima, e pousará em baixa velocidade.

Para sobrevoar a área, digite a altitude que o MechPeste tentará manter, e clique em "Sobrevoar",
após isso, para descer, clique em "Pousar".

### *Manobras*

Essa função consegue executar a próxima manobra criada, ou então tenta circularizar a órbita atual
no Apoastro ou no Periastro.
Não tente circularizar caso a órbita atual sejá hiperbólica, ou parabólica.

~~O botão ajustar tenta ajustar a inclinação da órbita atual, de acordo com algum alvo, facilitando
o rendezvous.~~

### *Auto Rover*

Esse script faz um Rover seguir uma rota até um determinado Marcador ou Alvo selecionado pelo
jogador.

Escolha qual o tipo de alvo a seguir, e no caso de Marcador digite o nome dele na caixa de
texto e também a velocidade máxima que o rover pode alcançar.

A velocidade mínima permitida é de 3m/s. Clique em Pilotar e ele comecará a se
mover para o alvo, desviando dos obstáculos à frente.
