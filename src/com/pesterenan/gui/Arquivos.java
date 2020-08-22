package com.pesterenan.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Arquivos {

	private static File config;
	private static File logErros;
	private static FileWriter escritor;
	private static Scanner leitor;
	private static String linha;

	public static final String DO = "[Decolagem Orbital]";
	public static final String SB = "[Suicide Burn]";
	public static final String AR = "[Auto Rover]";

	public Arquivos() {
		try {
			config = new File("mp_config.cfg");
			if (config.createNewFile()) {
				escritor = new FileWriter(config);
				criarArquivoConfig();
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

	private void criarArquivoConfig() throws IOException {
		escritor.write("[MechPeste - Configurações]\n");
		escritor.write(DO + "\n");
		escritor.write("apoastro=80000\n");
		escritor.write("inclinacao=90\n");
		escritor.write(SB + "\n");
		escritor.write("altp=0.001\n");
		escritor.write("alti=0.01\n");
		escritor.write("altd=0.01\n");
		escritor.write("velp=0.025\n");
		escritor.write("veli=0.05\n");
		escritor.write("veld=0.05\n");
		escritor.write(AR + "\n");
		escritor.write("marcador=ALVO\n");
		escritor.write("velocidade=10\n");
	}

	private void buscarConfiguracoes() {
		linha = leitor.nextLine();
		while (leitor.hasNextLine()) {
			switch (linha) {
			case DO:
				GUI.apoastroFinalTextField.setText(retornarValor());
				GUI.direcaoOrbitaTextField.setText(retornarValor());
				break;
			case SB:
				GUI.altP.setText(retornarValor());
				GUI.altI.setText(retornarValor());
				GUI.altD.setText(retornarValor());
				GUI.velP.setText(retornarValor());
				GUI.velI.setText(retornarValor());
				GUI.velD.setText(retornarValor());
				break;
			case AR:
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
		escritor = new FileWriter(logErros, false);
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

	public static void gravarDadosConfig(String[] dados) throws IOException {
		List<String> linhas = Files.readAllLines(config.toPath());
		for (String linha : linhas) {
			if (linha.trim().contentEquals(dados[0])) {
				switch (dados[0]) {
				case DO:
					linhas.set(linhas.indexOf(linha) + 1, "apoastro=" + dados[1]);
					linhas.set(linhas.indexOf(linha) + 2, "inclinacao=" + dados[2]);
					break;

				case SB:
					linhas.set(linhas.indexOf(linha) + 1, "altp=" + dados[1]);
					linhas.set(linhas.indexOf(linha) + 2, "alti=" + dados[2]);
					linhas.set(linhas.indexOf(linha) + 3, "altd=" + dados[3]);
					linhas.set(linhas.indexOf(linha) + 4, "velp=" + dados[4]);
					linhas.set(linhas.indexOf(linha) + 5, "veli=" + dados[5]);
					linhas.set(linhas.indexOf(linha) + 6, "veld=" + dados[6]);
					break;

				case AR:
					linhas.set(linhas.indexOf(linha) + 1, "marcador=" + dados[1]);
					linhas.set(linhas.indexOf(linha) + 2, "velocidade=" + dados[2]);
					break;
				}
			}
		}
		Files.write(config.toPath(), linhas);
	}
}
