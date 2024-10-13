package AEV1;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

/**
 * Classe que carrega y fa posible la visualizació del projecte com a ventana.
 */
public class aev1 extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldDirectori;
	private JTextField textFieldParaula;
	private JTextField textFieldReemplazar;
	private JButton btnTrobarDirectori;
	private JButton btnBuscarPalabra;
	private JButton btnReemplazar;
	private JTextArea textAreaPresentacio;
	private JScrollPane scrollPane;
	private JCheckBox JchkMayusculas;
	private JCheckBox JchkAcentos;

	/**
	 * Mètode que crea la clase principal de l'aplicació.
	 * 
	 * @param args Paràmetres passats per argument al programa.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					aev1 frame = new aev1();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Mètode que carrega una estructura en arbre d'una ruta absoluta i els fitxers
	 * i subdirectoris de dins.
	 * 
	 * @param directorio Arxiu que es pasa per parámetre y fara una acció diferent
	 *                   si es directori o no.
	 * @param nivel      Indica l'altura del arxiu en l'estructura d'arbre.
	 * @return Retorna un String amb l'estructura en arbre completa.
	 */
	public static String directorioArbol(File directorio, int nivel) {

		StringBuilder arbol = new StringBuilder();

		if (!directorio.exists()) {
			JOptionPane.showMessageDialog(null, "La ruta proporcionada NO existe!", "Error", JOptionPane.ERROR_MESSAGE);
			return arbol.toString();
		}

		for (int i = 0; i < nivel; i++) {
			arbol.append("   ");
		}

		arbol.append("|--").append(directorio.getName());

		if (directorio.isFile()) {
			long ms = directorio.lastModified();
			Date d = new Date(ms);
			Calendar c = new GregorianCalendar();
			c.setTime(d);

			String dia = Integer.toString(c.get(Calendar.DATE));
			String mes = Integer.toString(c.get(Calendar.MONTH) + 1);
			String annio = Integer.toString(c.get(Calendar.YEAR));
			String hora = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
			String minuto = Integer.toString(c.get(Calendar.MINUTE));
			String segundo = Integer.toString(c.get(Calendar.SECOND));

			long tamanyEnKB = directorio.length() / 1024;

			arbol.append("  (").append(tamanyEnKB).append(" KB").append(" - ").append(dia).append("/").append(mes)
					.append("/").append(annio).append(" ").append(hora).append(":").append(minuto).append(":")
					.append(segundo).append(")");
		}

		arbol.append("\n");

		if (directorio.isDirectory()) {
			File[] archivos = directorio.listFiles();
			if (archivos != null) {
				for (File f : archivos) {
					arbol.append(directorioArbol(f, nivel + 1));
				}
			}
		}
		return arbol.toString();
	}

	/**
	 * Mètode que depenent de quin dels checkboxs esten marcats, cridará un métode
	 * diferent gracies a un switch.
	 * 
	 * @param num        Opcio entre 1 i 4 que rebrá depenent de quina opció de
	 *                   checkbox n'hi haja marcat.
	 * @param palabra    Paraula a trobar dins de l'arxiu.
	 * @param directorio Arxiu que es pasa per parámetre i fara una acció diferent
	 *                   si es directori o no.
	 * @param nivel      Indica l'altura del arxiu en l'estructura d'arbre.
	 * @return Retorna un String en l'estructura en arbre carregada i les
	 *         coincidencies de la paraula a trobar en cada un dels fitxers.
	 */
	public String tipoCoincidenciaBusqueda(int num, String palabra, File directorio, int nivel) {

		StringBuilder arbol = new StringBuilder();

		if (!directorio.exists()) {
			JOptionPane.showMessageDialog(null, "La ruta Absoluta proporcionada NO existe!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return arbol.toString();
		}

		for (int i = 0; i < nivel; i++) {
			arbol.append("   ");
		}

		if (directorio.isFile()) {
			int cantidadBusquedas = 0;
			switch (num) {
			case 1:
				cantidadBusquedas = parseaMayusculasAcentos(directorio, palabra);
				break;
			case 2:
				cantidadBusquedas = parseaMayusculas(directorio, palabra);
				break;
			case 3:
				cantidadBusquedas = parseaAcentos(directorio, limpiarAcentosString(palabra));
				break;
			case 4:
				cantidadBusquedas = noParseaAcentosMayusculas(directorio, limpiarAcentosString(palabra));
				break;
			}
			arbol.append("|--").append(directorio.getName()).append("  (").append(cantidadBusquedas)
					.append(" coincidencias)").append("\n");
		}

		if (directorio.isDirectory()) {

			arbol.append("|--").append(directorio.getName()).append("\n");

			nivel++;
			File[] archivos = directorio.listFiles();
			if (archivos != null) {
				for (File f : archivos) {
					arbol.append(tipoCoincidenciaBusqueda(num, palabra, f, nivel + 1));
				}
			}
		}

		return arbol.toString();
	}

	/**
	 * Mètode que carrega l'estructura en arbre amb la quantitat de paraules que
	 * coincideixen i es reemplacen.
	 * 
	 * @param palabraBusc Paraula a trobar dins dels fitxers.
	 * @param palabraNuev Paraula a reemplaçar de les coincidencies dels fitxers.
	 * @param directorio  Arxiu que es pasa per parámetre i fara una acció diferent
	 *                    si es directori o no.
	 * @param nivel       Indica l'altura del arxiu en l'estructura d'arbre.
	 * @param mayus       Indica si l'usuari vol respectar majuscules o no (true o
	 *                    false).
	 * @param acentos     Indica si l'usuari vol respectar accents o no (true o
	 *                    false).
	 * @return Retorna un String carregat amb l'estructura en arbre y la quantitat
	 *         de paraules reemplazades en cada un dels fitxers.
	 */
	public String reemplazarPalabra_A_Buscar(String palabraBusc, String palabraNuev, File directorio, int nivel,
			boolean mayus, boolean acentos) {

		StringBuilder arbol = new StringBuilder();

		if (!directorio.exists()) {
			JOptionPane.showMessageDialog(null, "La ruta proporcionada NO existe!", "Error", JOptionPane.ERROR_MESSAGE);
			return arbol.toString();
		}

		for (int i = 0; i < nivel; i++) {
			arbol.append("   ");
		}

		if (directorio.isFile()) {

			if (directorio.getName().endsWith(".pdf")) {
				arbol.append("|--").append(directorio.getName()).append("  (").append(" Archivo no accesible!)")
						.append("\n");
			} else {

				int cantidadBusquedas = 0;

				cantidadBusquedas = archivoNuevoReemplazado(directorio, palabraBusc, palabraNuev, mayus, acentos);

				arbol.append("|--").append(directorio.getName()).append("  (").append(cantidadBusquedas)
						.append(" Palabras Reemplazadas)").append("\n");
			}

		}

		if (directorio.isDirectory()) {
			arbol.append("|--").append(directorio.getName()).append("\n");
			nivel++;
			File[] archivos = directorio.listFiles();
			if (archivos != null) {
				for (File f : archivos) {
					arbol.append(reemplazarPalabra_A_Buscar(palabraBusc, palabraNuev, f, nivel, mayus, acentos));
				}
			}
		}
		return arbol.toString();
	}

	/**
	 * Mètode que llig una llista de Strings i troba la paraula a trobar.
	 * 
	 * @param list      Llista de Strings pasada per paràmetre amb l'informació d'un
	 *                  arxius PDF (amb extract).
	 * @param paraula   Paraula a trobar dins la llista de Strings.
	 * @param majuscula Indica si l'usuari vol respectar majuscules o no (true o
	 *                  false).
	 * @param acentos   Indica si l'usuari vol respectar accents o no (true o
	 *                  false).
	 * @return Retorna la quantitat de vegades que s'ha trobat la paraula a trobar.
	 */
	private static int cantidadBusquedasPDF(List<String> list, String paraula, boolean majuscula, boolean acentos) {
		int cant = 0;

		if (!paraula.equals("")) {
			for (String s : list) {
				if (majuscula) {
					paraula = paraula.toLowerCase();
					s = s.toLowerCase();
				}

				if (acentos) {
					paraula = limpiarAcentosString(paraula);
					s = limpiarAcentosString(s);
				}

				int indice = 0;

				while ((indice = s.indexOf(paraula, indice)) != -1) {
					cant++;
					indice += paraula.length();
				}
			}
		}
		return cant;
	}

	/**
	 * Mètode que reemplaza les paraules a trobar dins d'un fitxer i crea un nou
	 * fitxer amb l'informació modificada (MOD_).
	 * 
	 * @param archivoOrigen Arxiu que es pasa per parámetre i fara una acció
	 *                      diferent si es directori o no.
	 * @param palabraBuscar Paraula a trobar dins de l'arxiu.
	 * @param palabraNueva  Paraula a reemplaçar per la paraula que es troba.
	 * @param mayus         Indica si l'usuari vol respectar majuscules o no (true o
	 *                      false).
	 * @param acentos       Indica si l'usuari vol respectar accents o no (true o
	 *                      false).
	 * @return Retorna la quantitat de paraules trobades dins de l'arxiu.
	 */
	private int archivoNuevoReemplazado(File archivoOrigen, String palabraBuscar, String palabraNueva, boolean mayus,
			boolean acentos) {

		int palabrasReemplazadas = 0;
		try {

			FileReader ficheroLectura = new FileReader(archivoOrigen);
			BufferedReader br = new BufferedReader(ficheroLectura);
			boolean reemplazo = false;
			String nuevoTexto = "";

			String linea = br.readLine();

			while (linea != null) {
				if (mayus && acentos) {

					String linea2 = limpiarAcentosString(linea).toLowerCase();
					int indice = linea2.indexOf(palabraBuscar.toLowerCase());

					while (indice != -1) {
						reemplazo = true;
						palabrasReemplazadas++;
						indice = linea2.indexOf(palabraBuscar.toLowerCase(), indice + 1);
					}

					String lineaCambiada = linea2.replaceAll(palabraBuscar.toLowerCase(), palabraNueva.toLowerCase());
					nuevoTexto += lineaCambiada + "\n";

					linea = br.readLine();

				} else if (mayus && !acentos) {

					String linea2 = linea.toLowerCase();
					int indice = linea2.indexOf(palabraBuscar.toLowerCase());

					while (indice != -1) {
						reemplazo = true;
						palabrasReemplazadas++;
						indice = linea2.indexOf(palabraBuscar.toLowerCase(), indice + 1);
					}

					String lineaCambiada = linea2.replaceAll(palabraBuscar.toLowerCase(), palabraNueva.toLowerCase());
					nuevoTexto += lineaCambiada + "\n";

					linea = br.readLine();

				} else if (!mayus && acentos) {

					String linea2 = limpiarAcentosString(linea);
					int indice = linea2.indexOf(palabraBuscar);

					while (indice != -1) {
						reemplazo = true;
						palabrasReemplazadas++;
						indice = linea2.indexOf(palabraBuscar, indice + 1);
					}

					String lineaCambiada = linea2.replaceAll(palabraBuscar, palabraNueva);
					nuevoTexto += lineaCambiada + "\n";

					linea = br.readLine();

				} else {

					String linea2 = linea;
					int indice = linea2.indexOf(palabraBuscar);

					while (indice != -1) {
						reemplazo = true;
						palabrasReemplazadas++;
						indice = linea2.indexOf(palabraBuscar, indice + 1);
					}

					String lineaCambiada = linea2.replaceAll(palabraBuscar, palabraNueva);
					nuevoTexto += lineaCambiada + "\n";

					linea = br.readLine();

				}
			}

			if (reemplazo) {
				File ficheroNuevo = new File("." + File.separator + "MOD_" + archivoOrigen.getName());
				System.out.println();
				FileWriter fw = new FileWriter(ficheroNuevo);

				BufferedWriter bw = new BufferedWriter(fw);

				bw.append(nuevoTexto);

				bw.close();
				fw.close();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return palabrasReemplazadas;
	}

	/**
	 * Mètode que ocurreix quan l'usuari respecta l'us de les mayuscules en la
	 * paraula a trobar.
	 * 
	 * @param fi      Arxiu que depenent si es un directori o no fará una cosa
	 *                diferent.
	 * @param palabra Paraula a trobar dins de l'arxiu.
	 * @return Retorna la quantitat de vegades que es troba la paraula dins de
	 *         l'arxiu.
	 */
	private int noParseaAcentosMayusculas(File fi, String palabra) {
		int cantidadBusquedas = 0;

		FileReader ficheroLectura;
		try {
			if (fi.getName().endsWith(".pdf")) {
				try {
					PDFInfoExtractor extrac = new PDFInfoExtractor();
					List<String> textoDelPDF = extrac.parsePDFDocument(fi);
					cantidadBusquedas = cantidadBusquedasPDF(textoDelPDF, palabra, false, false);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				ficheroLectura = new FileReader(fi);
				BufferedReader br = new BufferedReader(ficheroLectura);
				String linea;
				int indice = 0;
				try {
					linea = br.readLine();
					while (linea != null) {
						indice = linea.indexOf(palabra);
						while (indice != -1) {
							cantidadBusquedas++;
							indice = linea.indexOf(palabra, indice + 1);
						}
						linea = br.readLine();
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return cantidadBusquedas;
	}

	/**
	 * Mètode que ocurreix quan l'usuari no vol respectar els accents a l'hora de
	 * trobar la paraula.
	 * 
	 * @param fi      Arxiu que depenent si es un directori o no fará una cosa
	 *                diferent.
	 * @param palabra Paraula a trobar dins de l'arxiu.
	 * @return Retorna la quantitat de vegades que es troba la paraula sense accents
	 *         dins de l'arxiu.
	 */
	private int parseaAcentos(File fi, String palabra) {
		int cantidadBusquedas = 0;

		FileReader ficheroLectura;
		try {
			if (fi.getName().endsWith(".pdf")) {
				try {
					PDFInfoExtractor extrac = new PDFInfoExtractor();
					List<String> textoDelPDF = extrac.parsePDFDocument(fi);
					cantidadBusquedas = cantidadBusquedasPDF(textoDelPDF, palabra, false, true);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				ficheroLectura = new FileReader(fi);
				BufferedReader br = new BufferedReader(ficheroLectura);
				String linea;
				try {
					linea = br.readLine();
					while (linea != null) {
						String lineaNueva = limpiarAcentosString(linea);
						int indice = lineaNueva.indexOf(limpiarAcentosString(palabra));
						while (indice != -1) {
							cantidadBusquedas++;
							indice = lineaNueva.indexOf(limpiarAcentosString(palabra), indice + 1);
						}
						linea = br.readLine();
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return cantidadBusquedas;
	}

	/**
	 * Mètode que ocurreix quan l'usuari no vol respectar les majuscules a l'hora de
	 * trobar la paraula.
	 * 
	 * @param fi      Arxiu que depenent si es un directori o no fará una cosa
	 *                diferent.
	 * @param palabra Paraula a trobar dins de l'arxiu.
	 * @return Retorna la quantitat de vegades que es troba la paraula sense
	 *         majuscules dins de l'arxiu.
	 */
	private int parseaMayusculas(File fi, String palabra) {
		int cantidadBusquedas = 0;

		FileReader ficheroLectura;
		try {
			if (fi.getName().endsWith(".pdf")) {
				try {
					PDFInfoExtractor extrac = new PDFInfoExtractor();
					List<String> textoDelPDF = extrac.parsePDFDocument(fi);
					cantidadBusquedas = cantidadBusquedasPDF(textoDelPDF, palabra, true, false);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				ficheroLectura = new FileReader(fi);
				BufferedReader br = new BufferedReader(ficheroLectura);
				String linea;
				try {
					linea = br.readLine();
					while (linea != null) {
						String linea2 = linea.toLowerCase();
						int indice = linea2.indexOf(palabra.toLowerCase());
						while (indice != -1) {
							cantidadBusquedas++;
							indice = linea2.indexOf(palabra.toLowerCase(), indice + 1);
						}
						linea = br.readLine();
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return cantidadBusquedas;
	}

	/**
	 * Mètode que ocurreix quan l'usuari no vol respectar ni les majuscules ni els
	 * accents a l'hora de trobar la paraula.
	 * 
	 * @param fi      Arxiu que depenent si es un directori o no fará una cosa
	 *                diferent.
	 * @param palabra Paraula a trobar dins de l'arxiu.
	 * @return Retorna la quantitat de vegades que es troba la paraula sense
	 *         majuscules i accents dins de l'arxiu.
	 */
	private int parseaMayusculasAcentos(File fi, String palabra) {
		int cantidadBusquedas = 0;

		FileReader ficheroLectura;
		try {
			if (fi.getName().endsWith(".pdf")) {
				try {
					PDFInfoExtractor extrac = new PDFInfoExtractor();
					List<String> textoDelPDF = extrac.parsePDFDocument(fi);
					cantidadBusquedas = cantidadBusquedasPDF(textoDelPDF, palabra, true, true);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {

				ficheroLectura = new FileReader(fi);
				BufferedReader br = new BufferedReader(ficheroLectura);
				String linea;
				try {
					linea = br.readLine();
					while (linea != null) {
						String linea2 = limpiarAcentosString(linea).toLowerCase();
						int indice = linea2.indexOf(palabra.toLowerCase());
						while (indice != -1) {
							cantidadBusquedas++;
							indice = linea2.indexOf(palabra.toLowerCase(), indice + 1);
						}
						linea = br.readLine();
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return cantidadBusquedas;
	}

	/**
	 * Mètode que amb l'ajuda d'un normalize limpia tots el signes d'acentuació
	 * d'una paraula.
	 * 
	 * @param st String el cual limpiarem d'accentuacions.
	 * @return Retorna un String net sense signes d'accentuació.
	 */
	private static String limpiarAcentosString(String st) {
		st = Normalizer.normalize(st, Normalizer.Form.NFD);
		st = st.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return st;
	}

	/**
	 * Constructor de la classe AEV1 on es crea la ventana, els components
	 * corresponents a la ventana i també la lògica de programació dels botons.
	 */
	public aev1() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1019, 615);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		btnTrobarDirectori = new JButton("Trobar");
		btnTrobarDirectori.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (textFieldDirectori.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "La Ruta Absoluta a buscar NO puede estar vacía", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					File rutaDirectorio = new File(textFieldDirectori.getText());
					String estructuraArbol = directorioArbol(rutaDirectorio, 0);
					textAreaPresentacio.setText(estructuraArbol);
				}

			}
		});
		btnTrobarDirectori.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnTrobarDirectori.setBounds(327, 231, 113, 32);
		contentPane.add(btnTrobarDirectori);

		JLabel lblDirectori = new JLabel("Ruta Directori a Treballar: ");
		lblDirectori.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblDirectori.setBounds(10, 200, 193, 25);
		contentPane.add(lblDirectori);

		JLabel lblTitol = new JLabel("GESTIÓ DE FITXERS DE TEXT PLA");
		lblTitol.setFont(new Font("Tahoma", Font.BOLD, 28));
		lblTitol.setBounds(229, 37, 490, 60);
		contentPane.add(lblTitol);

		JTextPane textPane1 = new JTextPane();
		textPane1.setBounds(0, 0, 1011, 134);
		textPane1.setBackground(new Color(144, 238, 144));
		contentPane.add(textPane1);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(476, 133, 535, 450);
		contentPane.add(scrollPane);

		textAreaPresentacio = new JTextArea();
		scrollPane.setViewportView(textAreaPresentacio);
		textAreaPresentacio.setFont(new Font("Monospaced", Font.PLAIN, 16));

		textFieldDirectori = new JTextField();
		textFieldDirectori.setBounds(10, 231, 272, 32);
		contentPane.add(textFieldDirectori);
		textFieldDirectori.setColumns(10);

		textFieldParaula = new JTextField();
		textFieldParaula.setColumns(10);
		textFieldParaula.setBounds(10, 383, 272, 32);
		contentPane.add(textFieldParaula);

		JLabel lblPalabras = new JLabel("Paraula a trobar:");
		lblPalabras.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPalabras.setBounds(10, 307, 285, 25);
		contentPane.add(lblPalabras);

		btnBuscarPalabra = new JButton("Trobar");
		btnBuscarPalabra.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (textFieldDirectori.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "La Ruta Absoluta a buscar NO puede estar vacía", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				String texto;

				File rutaDirectorio = new File(textFieldDirectori.getText());

				String palabraABuscar = textFieldParaula.getText();

				if (palabraABuscar.isEmpty()) {
					JOptionPane.showMessageDialog(null, "La palabra a buscar no puede estar vacía", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (JchkMayusculas.isSelected() && JchkAcentos.isSelected()) {
					texto = tipoCoincidenciaBusqueda(1, palabraABuscar, rutaDirectorio, 0);
				} else if (JchkAcentos.isSelected() && !JchkMayusculas.isSelected()) {
					texto = tipoCoincidenciaBusqueda(3, palabraABuscar, rutaDirectorio, 0);
				} else if (JchkMayusculas.isSelected() && !JchkAcentos.isSelected()) {
					texto = tipoCoincidenciaBusqueda(2, (palabraABuscar), rutaDirectorio, 0);
				} else {
					texto = tipoCoincidenciaBusqueda(4, palabraABuscar, rutaDirectorio, 0);
				}

				textAreaPresentacio.setText(texto);
				System.out.println(texto);
			}
		});

		btnBuscarPalabra.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBuscarPalabra.setBounds(327, 353, 113, 32);
		contentPane.add(btnBuscarPalabra);

		JLabel lblPalabraReemplazar = new JLabel("Paraula nova a posar:");
		lblPalabraReemplazar.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPalabraReemplazar.setBounds(10, 449, 285, 25);
		contentPane.add(lblPalabraReemplazar);

		textFieldReemplazar = new JTextField();
		textFieldReemplazar.setColumns(10);
		textFieldReemplazar.setBounds(10, 480, 272, 32);
		contentPane.add(textFieldReemplazar);

		btnReemplazar = new JButton("Reemplaçar");
		btnReemplazar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				File rutaDirectorio = new File(textFieldDirectori.getText());
				String palabraReemplazar = textFieldReemplazar.getText();
				String palabraBuscar = textFieldParaula.getText();
				String texto = "";

				if (textFieldDirectori.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "La Ruta Absoluta a buscar NO puede estar vacía", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (palabraBuscar.isEmpty()) {
					JOptionPane.showMessageDialog(null, "La palabra a buscar no puede estar vacía", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (palabraReemplazar.isEmpty()) {
					JOptionPane.showMessageDialog(null, "La palabra a reemplazar no puede estar vacía", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (JchkMayusculas.isSelected() && JchkAcentos.isSelected()) {
					texto = reemplazarPalabra_A_Buscar(palabraBuscar, palabraReemplazar, rutaDirectorio, 0, true, true);
				} else if (JchkAcentos.isSelected() && !JchkMayusculas.isSelected()) {
					texto = reemplazarPalabra_A_Buscar(palabraBuscar, palabraReemplazar, rutaDirectorio, 0, false,
							true);
				} else if (JchkMayusculas.isSelected() && !JchkAcentos.isSelected()) {
					texto = reemplazarPalabra_A_Buscar(palabraBuscar, palabraReemplazar, rutaDirectorio, 0, true,
							false);
				} else {
					texto = reemplazarPalabra_A_Buscar(palabraBuscar, palabraReemplazar, rutaDirectorio, 0, false,
							false);
				}

				textAreaPresentacio.setText(texto);

			}
		});

		btnReemplazar.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnReemplazar.setBounds(327, 480, 113, 32);
		contentPane.add(btnReemplazar);

		JchkMayusculas = new JCheckBox("MAYUSCULAS");
		JchkMayusculas.setBounds(10, 338, 106, 39);
		contentPane.add(JchkMayusculas);

		JchkAcentos = new JCheckBox("ACENTOS");
		JchkAcentos.setBackground(new Color(255, 255, 255));
		JchkAcentos.setBounds(136, 338, 106, 39);
		contentPane.add(JchkAcentos);

		JTextPane textPane2 = new JTextPane();
		textPane2.setBackground(new Color(192, 192, 192));
		textPane2.setBounds(0, 133, 477, 450);
		contentPane.add(textPane2);
		this.setVisible(rootPaneCheckingEnabled);
		;
	}
}