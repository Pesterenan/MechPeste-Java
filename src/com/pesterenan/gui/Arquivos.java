package com.pesterenan.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Arquivos {

	private static File config;
	private static File logErros;
	private static FileWriter escritor;
	private static Scanner leitor;
	private static String linha;

	public Arquivos() {
		try {
			config = new File("mp_config.cfg");
			if (config.createNewFile()) {
				escritor = new FileWriter(config);
				escreverArquivoConfig();
				escritor.close();
				System.out.println("Arquivo de config criado.");
			} else {
				System.out.println("Arquivo de config já existe");
				leitor = new Scanner(config);
				buscarConfiguracoes();
				leitor.close();
			}

		} catch (IOException e) {
			System.out.println("Erro ao criar arquivo de configuração.");
			e.printStackTrace();
		}
	}

	private void escreverArquivoConfig() throws IOException {
		escritor.write("[MechPeste - Configurações]\n");
		escritor.write("[Decolagem Orbital]\n");
		escritor.write("apoastro=80000\n");
		escritor.write("inclinacao=90\n");
		escritor.write("[Suicide Burn]\n");
		escritor.write("altp=0.001\n");
		escritor.write("alti=0.01\n");
		escritor.write("altd=0.01\n");
		escritor.write("velp=0.025\n");
		escritor.write("veli=0.05\n");
		escritor.write("veld=0.05\n");
		escritor.write("[Auto Rover]\n");
		escritor.write("marcador=ALVO\n");
		escritor.write("velocidade=10\n");
	}

	private void buscarConfiguracoes() {
		linha = leitor.nextLine();
		while (leitor.hasNextLine()) {
			switch (linha) {
			case "[Decolagem Orbital]":
				GUI.apoastroFinalTextField.setText(retornarValor());
				GUI.direcaoOrbitaTextField.setText(retornarValor());
				break;
			case "[Suicide Burn]":
				GUI.altP.setText(retornarValor());
				GUI.altI.setText(retornarValor());
				GUI.altD.setText(retornarValor());
				GUI.velP.setText(retornarValor());
				GUI.velI.setText(retornarValor());
				GUI.velD.setText(retornarValor());
				break;
			case "[Auto Rover]":
				GUI.nomeMarcadorTextField.setText(retornarValor());
				GUI.velMaxTextField.setText(retornarValor());
				break;
			default:
				linha = leitor.nextLine();
			}
		}

	}

	public static void criarLogDeErros(StackTraceElement[] elementos) throws IOException {
		SimpleDateFormat formatar = new SimpleDateFormat("dd-MM-yy 'às' HH:mm:ss");
		Date data = new Date(System.currentTimeMillis());
		logErros = new File("mp_erros.log");
		logErros.createNewFile();
		escritor = new FileWriter(logErros, true);
		escritor.write("Log de erros criado na data: " + formatar.format(data) + "\n\n");
		for (int i = 0; i < elementos.length; i++) {
			escritor.write(elementos[i].toString() + "\n");
		}
		escritor.write("\n");
		escritor.close();
	}

	private String retornarValor() {
		linha = leitor.nextLine();
		String[] valor = linha.split("=");
		return valor[1];

	}
}
