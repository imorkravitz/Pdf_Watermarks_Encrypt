import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.*;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.security.PdfEncryptionKeySize;
import com.spire.pdf.security.PdfPermissionsFlags;

public class EncryptPDF {
    public static String Source_DIR = "/Users/orkravitz/Downloads/ProtectMyPDF/pdf_toSplit_toEncrypt/";
    public static String Destination_DIR = "/Users/orkravitz/Downloads/ProtectMyPDF/protected/";
    public static String LOG_PATH = "/Users/orkravitz/dev/orchestrate_pdf_processing.log";  // Use orchestrator log path

    private static final Logger logger = Logger.getLogger(EncryptPDF.class.getName());

    public static void main(String[] args) {
        setupLogger();

        File directoryPath = new File(Source_DIR);
        String[] names = directoryPath.list();
        if (names == null) {
            logger.severe("No files found in the source directory.");
            return;
        }

        String[] fPath = new String[names.length];
        int fliesCount  = names.length - 1;
        logger.info("Get (" + fliesCount + ") files from path");

        for(int i = 0; i < names.length; i++) {
            if (!names[i].startsWith(".DS_Store")) {
                fPath[i] = Source_DIR + names[i];
            }
        }

        for (int i = 0; i < names.length; i++) {
            if (!names[i].startsWith(".DS_Store")) {
                PdfDocument doc = new PdfDocument();
                File pdfFile = new File(fPath[i]);
                String[] regex = null;

                try {
                    doc.loadFromFile(fPath[i]);
                } catch (Exception e) {
                    logger.severe("Failed to load file: " + fPath[i]);
                    continue;
                }

                int pages = doc.getPages().getCount();

                if (names[i].startsWith("subset-")) {
                    regex = names[i].split("-");
                    String[] temp = regex[2].split("_");
                    logger.info(regex[0] + "-" + temp[0]);
                } else {
                    regex = names[i].split("_");
                    logger.info(regex[0]);
                }

                PdfPageBase[] page = new PdfPageBase[pages];
                for (int k = 0; k < pages; k++) {
                    page[k] = doc.getPages().get(k);
                    try {
                        if (names[i].startsWith("subset-")) {
                            regex = names[i].split("-");
                            String[] temp = regex[2].split("_");
                            insertWatermark(page[k], temp[0]);
                        } else {
                            regex = names[i].split("_");
                            insertWatermark(page[k], regex[0]);

                            PdfEncryptionKeySize keySize = PdfEncryptionKeySize.Key_128_Bit;
                            String openPassword = "";
                            String permissionPassword = "GftDy56xcRKl09Q";
                            EnumSet<PdfPermissionsFlags> flags = EnumSet.of(PdfPermissionsFlags.Print, PdfPermissionsFlags.Fill_Fields);
                            doc.getSecurity().encrypt(openPassword, permissionPassword, flags, keySize);
                        }
                        logger.info("Encryption applied to file: " + names[i]);
                    } catch (Exception e) {
                        logger.severe("Failed to add watermark or encrypt page " + (k + 1) + " of file: " + names[i]);
                    }
                }

                try {
                    doc.saveToFile(Destination_DIR + names[i]);
                    doc.close();
                    pdfFile.delete();
                    logger.info("File saved and deleted: " + names[i]);
                } catch (Exception e) {
                    logger.severe("Failed to save or delete file: " + names[i]);
                }
            }
        }
        logger.info("Done");
    }

    private static void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler(LOG_PATH, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.severe("Failed to set up logger: " + e.getMessage());
        }
    }

    public static void insertWatermark(PdfPageBase page, String watermark) {
        logger.info("Starting insertWatermark");

        Dimension2D dimension2D = new Dimension();
        dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 4, page.getCanvas().getClientSize().getHeight() / 4);
        PdfTilingBrush brush = new PdfTilingBrush(dimension2D);
        brush.getGraphics().setTransparency(0.3F);
        brush.getGraphics().save();
        brush.getGraphics().translateTransform((float) brush.getSize().getWidth() / 3, (float) brush.getSize().getHeight() / 4);
        brush.getGraphics().rotateTransform(-45);

        try {
            Font awtFont = new Font("Arial Unicode MS", Font.PLAIN, 14); // Use Arial Unicode MS or another Hebrew-supporting font
            PdfTrueTypeFont font = new PdfTrueTypeFont(awtFont); // Create PdfTrueTypeFont using the AWT font
            PdfStringFormat format = new PdfStringFormat(PdfTextAlignment.Center);
            format.setRightToLeft(true); // Handle right-to-left text for Hebrew

            brush.getGraphics().drawString(watermark, font, PdfBrushes.getBlack(), 0, 0, format);
            logger.info("Watermark inserted successfully");
        } catch (Exception e) {
            logger.severe("Failed to use system font: " + e.getMessage());
            e.printStackTrace();
        }

        brush.getGraphics().restore();
        brush.getGraphics().setTransparency(1.0);
        Rectangle2D loRect = new Rectangle2D.Float();
        loRect.setFrame(new Point2D.Float(0, 0), page.getCanvas().getClientSize());
        page.getCanvas().drawRectangle(brush, loRect);
    }
}
