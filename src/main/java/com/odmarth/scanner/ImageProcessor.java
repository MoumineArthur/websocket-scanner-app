package com.odmarth.scanner;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageProcessor {

   public static BufferedImage processImageForVisibility(BufferedImage original) {
    // Convertir en niveaux de gris
    BufferedImage grayscale = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    for (int y = 0; y < original.getHeight(); y++) {
        for (int x = 0; x < original.getWidth(); x++) {
            Color color = new Color(original.getRGB(x, y));
            int gray = (int) (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11);

            // Réduction de la luminosité (assombrir l'image si elle est trop claire)
            gray = Math.max(0, gray - 30); // Réduit chaque pixel de 30 unités de luminosité
            Color grayColor = new Color(gray, gray, gray);
            grayscale.setRGB(x, y, grayColor.getRGB());
        }
    }

    // Appliquer une correction gamma pour renforcer les détails sombres
    BufferedImage gammaCorrected = applyGammaCorrection(grayscale, 0.7); // Gamma < 1 pour assombrir

    // Binarisation (Noir et Blanc)
    BufferedImage binary = new BufferedImage(gammaCorrected.getWidth(), gammaCorrected.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
    for (int y = 0; y < gammaCorrected.getHeight(); y++) {
        for (int x = 0; x < gammaCorrected.getWidth(); x++) {
            Color color = new Color(gammaCorrected.getRGB(x, y));
            int threshold = 100; // Réduit le seuil pour capturer plus de détails sombres
            int bw = color.getRed() < threshold ? 0 : 255;
            Color bwColor = new Color(bw, bw, bw);
            binary.setRGB(x, y, bwColor.getRGB());
        }
    }

    return binary;
}
   private static BufferedImage applyGammaCorrection(BufferedImage image, double gamma) {
	    BufferedImage corrected = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
	    double gammaCorrection = 1.0 / gamma;

	    for (int y = 0; y < image.getHeight(); y++) {
	        for (int x = 0; x < image.getWidth(); x++) {
	            Color color = new Color(image.getRGB(x, y));
	            int gray = color.getRed();
	            int correctedGray = (int) (255 * Math.pow((double) gray / 255, gammaCorrection));
	            correctedGray = Math.min(255, Math.max(0, correctedGray)); // Assurer que la valeur reste entre 0 et 255
	            Color correctedColor = new Color(correctedGray, correctedGray, correctedGray);
	            corrected.setRGB(x, y, correctedColor.getRGB());
	        }
	    }

	    return corrected;
	}
}
