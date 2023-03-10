import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.EnumSet;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.*;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.security.PdfEncryptionKeySize;
import com.spire.pdf.security.PdfPermissionsFlags;

public class EncryptPDF {

    public static String PATH = "/*****/";
    public static String FINAL = "/*****/";

    public static void main(String[] args) {

        System.out.println("\n*******************************\n");

        File directoryPath = new File(PATH);
        String[] names = directoryPath.list();
        String[] fPath = new String[names.length];
        System.out.println("Get Files Path : \n");

        for(int i=0; i<names.length; i++) {
            if (!names[i].startsWith(".DS_Store")){
                fPath[i] = PATH + names[i];
                System.out.println(fPath[i]);
            }
        }

        System.out.println("\n*******************************\n");
        System.out.println("Get Files Names : \n");
        for (int i = 0; i < names.length; i++) {
            if (!names[i].startsWith(".DS_Store")) {
                // Create a PdfDocument instance for each PDF file

                PdfDocument doc = new PdfDocument();
                File pdfFile = new File(fPath[i]);
                // Load the PDF file
                // Create a list to store the split PDF documents
                String[] regex = null;

                doc.loadFromFile(fPath[i]);

                // Get the number of pages in the PDF document
                int pages = doc.getPages().getCount();

                if (names[i].startsWith("subset-")){
                    regex = names[i].split("-");
                    String[] temp = regex[2].split("_");
                    System.out.println(regex[0] + "-" + temp[0]);
                }else{
                    regex = names[i].split("_");
                    System.out.println(regex[0]);
                }

                // Put a watermark on each page
                PdfPageBase[] page = new PdfPageBase[pages];
                for (int k = 0; k < pages; k++) {
                    page[k] = doc.getPages().get(k);
                    if (names[i].startsWith("subset-")){
                        // case greater then 10 pages
                        // using subsets files
                        // files are NOT Encrypted! >> will Encrypt in the next program
                        regex = names[i].split("-");
                        String[] temp = regex[2].split("_");
                        insertWatermark(page[k], temp[0]);; // Add watermarks
                    }else{
                        // if case is equal, or smaller then 10 pages
                        // include Encryption
                        regex = names[i].split("_");
                        insertWatermark(page[k], regex[0]); // Add watermarks

                        // Encrypt the PDF document
                        PdfEncryptionKeySize keySize = PdfEncryptionKeySize.Key_128_Bit;
                        String openPassword = "";
                        String permissionPassword = "/*****/";
                        EnumSet flags = EnumSet.of(PdfPermissionsFlags.Print, PdfPermissionsFlags.Fill_Fields);
                        doc.getSecurity().encrypt(openPassword, permissionPassword, flags, keySize);
                    }
                }


                // Save and close the PDF document
                doc.saveToFile(FINAL + names[i]);
                doc.close();
            }
        }
        System.out.println("\nDone\n");
    }
    public static void insertWatermark(PdfPageBase page, String watermark) {
        Dimension2D dimension2D = new Dimension();
        dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 4, page.getCanvas().getClientSize().getHeight() / 4);
        PdfTilingBrush brush = new PdfTilingBrush(dimension2D);
        brush.getGraphics().setTransparency(0.3F);
        brush.getGraphics().save();
        brush.getGraphics().translateTransform((float) brush.getSize().getWidth() / 3, (float) brush.getSize().getHeight() / 4);
        brush.getGraphics().rotateTransform(-45);
        brush.getGraphics().drawString(watermark, new PdfFont(PdfFontFamily.Helvetica, 16), PdfBrushes.getBlack(), 0 , 0 , new PdfStringFormat(PdfTextAlignment.Center));
        brush.getGraphics().restore();
        brush.getGraphics().setTransparency(1.5);
        Rectangle2D loRect = new Rectangle2D.Float();
        loRect.setFrame(new Point2D.Float(0, 0), page.getCanvas().getClientSize());
        page.getCanvas().drawRectangle(brush, loRect);
    }

}
