package AEV1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Clase on dispondrem d'un mètod que retornará les coincidencies de la paraula
 * a trobar.
 */
public class PDFInfoExtractor extends PDFTextStripper {

	List<String> textoExtraido;

	/**
	 * @throws IOException Constructor vuit de la clase.
	 */
	public PDFInfoExtractor() throws IOException {
	}

	/**
	 * Mètode que rep un pdf per parámetre y agrega les línies del contingut a una
	 * Llista de Strings.
	 * 
	 * @param arxiu Fitxer pdf que manipulem.
	 * @return Retornem una llista cargarda amb Strings.
	 */

	public List<String> parsePDFDocument(File arxiu) {
		textoExtraido = new ArrayList<>();

		PDDocument pdDocument = null;
		try {
			pdDocument = Loader.loadPDF(arxiu);

			this.setStartPage(1);
			this.setEndPage(pdDocument.getNumberOfPages());
			Writer dummyWriter = new OutputStreamWriter(new ByteArrayOutputStream());
			this.writeText(pdDocument, dummyWriter);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pdDocument != null) {
				try {
					pdDocument.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return textoExtraido;
	}

	@Override
	protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
		textoExtraido.add(text);
	}
}