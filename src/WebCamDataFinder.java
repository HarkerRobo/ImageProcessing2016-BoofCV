import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import com.github.sarxos.webcam.Webcam;

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.tracker.MeanShiftLikelihoodType;
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.shapes.ShapeFittingOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;

/**
 * Gathers data to learn the correct weights from
 *
 * @author joelmanning
 */
public class WebCamDataFinder {

    //Optimal width/height for the tape
    private static final double WH_RATIO = 1.66666667;
    //Optimal percent of the tape takes up of the smallest cube around it
    private static final double PERCENT_AREA = 0.31428571428;
    //Minimum sum of rgb values to be counted in a shape
    public static final int MIN_L = -269/*550*/;
    public static final int MIN_G = 249;
    //A display for testing the program
    static ListDisplayPanel gui = new ListDisplayPanel();
    //Settings for polygon display
    static double splitFraction = 0.05;
    static double minimumSideFraction = 0.1;
    //Minimum height and width to be considered as the tape
    private static final int MIN_HEIGHT = 10;
    private static final int MIN_WIDTH = 10;
    private static int STAY_VALUE = 100;
    //private static JTextField GSlide = new JTextField("1", 10/*0, 255*/);
    //private static JSlider LSlide = new JSlider(-510, 255);
    //private static JPanel sliders = new JPanel();
    //static JPanel panel = new JPanel();
    private static boolean useSliders = true;
    private static Sliders sl;
    public static void main(String[] args) {
        int dataNum = Integer.MAX_VALUE;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("data/data"));
            dataNum = Integer.parseInt(br.readLine());
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (NumberFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //List<String> lines = new ArrayList<String>();//Arrays.asList("The first line", "The second line");
        //Path file = Paths.get("data" + dataNum + ".txt");
        //Files.write(file, lines, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        if(useSliders){
            sl = new Sliders(MIN_L, MIN_G, STAY_VALUE);
        }
        //Chooser cho = new Chooser();
        //gui.addKeyListener(cho);
        // Open a webcam at a resolution close to 640x480
        Webcam webcam = UtilWebcamCapture.openDefault(640,480);
        // Create the panel used to display the image and feature tracks
        ImagePanel gui = new ImagePanel();
        gui.setPreferredSize(webcam.getViewSize());
        ShowImages.showWindow(gui,"KLT Tracker",true);
        Point2D_I32[] past = null;
        //int minimumTracks = 100;
        while( true ) {
            List<String> lines = new ArrayList<String>();//Arrays.asList("The first line", "The second line");
            Path file = Paths.get("data/data" + dataNum + ".txt");
            BufferedImage image = webcam.getImage();
            //turns it into a black and white image where black are the light pixels and everything else is white
            BufferedImage lImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            long totl = 0;
            long totg = 0;
            for(int x = 0; x < lImage.getWidth(); x++){
                for(int y = 0; y < lImage.getHeight(); y++){
                    int[] rgb = getRGBFromPixel(image.getRGB(x, y));
                    int newColor;
                    int l = rgb[1] - rgb[2] - rgb[0];
                    totl += l;
                    totg += rgb[1];
                    if(l > L() && rgb[1] > G()){
                        newColor = 0;
                    } else {
                        newColor = 255;
                    }
                    totl += l;
                    lImage.setRGB(x, y, toRGB(newColor));
                }
            }
            if(useSliders){
                long avgl = totl / (lImage.getWidth() * lImage.getHeight());
                long avgg = totg / (lImage.getWidth() * lImage.getHeight());
                sl.setAvg(avgg,  avgl);
            }
            ImageFloat32 lConverted = ConvertBufferedImage.convertFromSingle(lImage,
                    null, ImageFloat32.class);
            //adds the black and white image to the gui
            BufferedImage displayImage = new BufferedImage(lConverted.width,
                    lConverted.height, BufferedImage.TYPE_INT_RGB);
            ImageUInt8 binary = new ImageUInt8(lConverted.width, lConverted.height);

            // Finds edges inside the image
            CannyEdge<ImageFloat32, ImageFloat32> canny = FactoryEdgeDetectors
                    .canny(2, false, true, ImageFloat32.class, ImageFloat32.class);

            canny.process(lConverted, 0.1f, 0.3f, binary);

            List<Contour> contours = BinaryImageOps.contour(binary,
                    ConnectRule.EIGHT, null);

            Graphics2D g2 = lImage.createGraphics();
            g2.setStroke(new BasicStroke(2));

            //keeps track of the current set of corners and the error value associated with it
            Point2D_I32[] currentCorners = null;
            double currentError = Integer.MAX_VALUE;
            //int[] currentIndices = null;
            TreeMap<Double, Contour> tm = new TreeMap<Double, Contour>();
            System.out.println("There were " + contours.size() + " contours");
            for (Contour c : contours)
            {
                //System.out.println("Found shape with " + c.external.size() + " vertices");
                int size = polygonArea(c.external);
                Point2D_I32[] corners = fourCorners(c.external);
                //System.out.println("Found a contour with corners: " + c.external.toString());
                int cornerSize = polygonArea(corners);
                int height = (corners[0].y + corners[1].y - corners[2].y - corners[3].y)/2;
                int width = (corners[0].x + corners[3].x - corners[1].x - corners[2].x)/2;
                if(height > MIN_HEIGHT && width > MIN_WIDTH && cornerSize != 0/* && size > minSize*/){
                    double error = 0;
                    //adds error if the ratio of width to height is different from expected
                    double whError = Math.abs((double)1.0 - ((double)width/height)/WH_RATIO);
                    error +=  whError * whWeight();
                    //adds error if it is taking up a different percent of expected cube area
                    double apError = Math.abs((double)1.0 - ((double)size/cornerSize)/PERCENT_AREA);
                    error += apError * apWeight();
                    //adds error if the right corners have different x values and if the left corners have different x values
                    double svError = Math.abs((double)corners[0].x - corners[3].x)/width + Math.abs((double)corners[1].x - corners[2].x)/width;
                    error += svError * sideVerticalWeight();
                    //adds error if there is empty space inside the shape
                    double niError = 0;
                    if(!c.internal.isEmpty()){
                        niError = 1;
                    }
                    error += niError * noInternalWeight();
                    if(past != null){
                        for(int i = 0; i < 4; i++){
                            error += (Math.abs(past[i].x - corners[i].x) + Math.abs(past[i].y - corners[i].y)) * stayWeight();
                        }
                    }
                    //if this has less error than the current least error set current corners to this
                    if(error < currentError){
                        currentError = error;
                        currentCorners = corners;
                    }
                    System.out.println("I had an error of " + error);
                    if(tm.put(new Double(error), c) != null){
                        System.out.println("overwritten");
                    }
                    lines.add("whError=" + whError + ", apError=" + apError + ", svError=" + svError + ", niError=" + niError + ", chosen=false");
                } else {
                    lines.add("negligible");
                }
                //List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(
                //        c.external, true, splitFraction, minimumSideFraction, 100);

                //g2.setColor(new Color(rand.nextInt()));
                //draws this shape
                //VisualizeShapes.drawPolygon(vertexes, true, g2);
            }
            past = currentCorners;
            List<Point2D_I32> l = new ArrayList<Point2D_I32>();
            if(currentCorners != null){
                for(int i = 0; i < 4; i++){
                    l.add(currentCorners[i]);
                }
                System.out.println("Corners are: " + Arrays.toString(currentCorners));
            } else {
                System.out.println("Current corners was null");
            }
            Entry<Double, Contour> current = tm.firstEntry();
            int g = 255;
            int r = 0;       
            for(int i = 0; i < 3 && current != null; i++){
                System.out.println(255 * (Math.max(0, i - 1)) + ", " +  255 * (i % 2) + ", " +  255 * (1 - Math.signum(i)));
                Color c = new Color((Math.max(0, i - 1)), (i % 2), (1 - Math.signum(i)));
                g2.setColor(c);
                VisualizeShapes.drawPolygon(current.getValue().external, true, g2);
                current = tm.higherEntry(current.getKey());
            }
            List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(
                    l, true, splitFraction, minimumSideFraction, 100);
            g2.setColor(Color.CYAN);
            //draws the final conclusion of the program in cyan, then prints it out
            VisualizeShapes.drawPolygon(vertexes, true, g2);
            gui.setBufferedImageSafe(lImage);
            try
            {
                while(true){
                    String in = br.readLine();
                    if(in.equalsIgnoreCase("r")){
                        int index = contours.indexOf(tm.higherEntry(tm.higherKey(tm.firstKey())).getValue());
                        lines.set(index, lines.get(index).substring(0, lines.get(index).length() - 5) + "true");
                        break;
                    } else if(in.equalsIgnoreCase("g")){
                        int index = contours.indexOf(tm.higherEntry(tm.firstKey()).getValue());
                        lines.set(index, lines.get(index).substring(0, lines.get(index).length() - 5) + "true");
                        break;
                    } else if(in.equalsIgnoreCase("b")){
                        int index = contours.indexOf(tm.firstEntry().getValue());
                        lines.set(index, lines.get(index).substring(0, lines.get(index).length() - 5) + "true");
                        break;
                    } else if(in.equalsIgnoreCase("s")){
                        dataNum--;
                        break;
                    } else if(in.equalsIgnoreCase("stop")){
                        PrintWriter pw = new PrintWriter(new FileWriter("data/data"));
                        pw.println(dataNum);
                        pw.close();
                        br.close();
                        System.exit(0);
                    }
                    else {
                        System.out.println("Could not recognize input, type r, g, b, or s");
                    }
                }
                Files.write(file, lines, Charset.forName("UTF-8"));
                dataNum++;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    private static Point2D_I32[] fourCorners(List<Point2D_I32> points){
        Point2D_I32[] ps = new Point2D_I32[4];
        for(Point2D_I32 p: points){
            if(ps[0] == null || ps[0].x + ps[0].y < p.x + p.y){
                ps[0] = p;
            }
            if(ps[1] == null || - ps[1].x + ps[1].y < - p.x + p.y){
                ps[1] = p;
            }
            if(ps[2] == null || - ps[2].x - ps[2].y < - p.x - p.y){
                ps[2] = p;
            }
            if(ps[3] == null || ps[3].x - ps[3].y < p.x - p.y){
                ps[3] = p;
            }
        }
        return ps;
    }
    /**
     * returns the area of a polygon made up of points
     */
    private static int polygonArea(List<Point2D_I32> points) 
    { 
        int area = 0;         // Accumulates area in the loop
        int j = points.size() - 1;  // The last vertex is the 'previous' one to the first

        for (int i=0; i < points.size(); i++)
        {
            area = area +  (points.get(j).x + points.get(i).x) * (points.get(j).y - points.get(i).y); 
            j = i;  //j is previous vertex to i
        }
        return area/2;
    }
    /**
     * returns the area of a polygon made up of points
     */
    private static int polygonArea(Point2D_I32[] points) 
    { 
        int area = 0;         // Accumulates area in the loop
        int j = points.length - 1;  // The last vertex is the 'previous' one to the first

        for (int i=0; i < points.length; i++)
        {
            area = area +  (points[j].x + points[i].x) * (points[j].y - points[i].y); 
            j = i;  //j is previous vertex to i
        }
        return area/2;
    }
    /**
     * returns an array {r, g, b} from the integer value
     */
    public static int[] getRGBFromPixel(int pixel){
        int b = pixel & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int r = (pixel >> 16) & 0xFF;
        return new int[]{r, g, b};
    }
    /**
     * gives an rgb value with all of r, g, and b as l
     */
    public static int toRGB(int l){
        int n = l;
        if(n >= 256){
            n = 255;
        }
        return n + (n << 8) + (n << 16);
    }
    /**
     * takes in a list and returns a list of all points between first and last
     */
    public static List<Point2D_I32> between(List<Point2D_I32> list, Point2D_I32 first, Point2D_I32 last){
        List<Point2D_I32> b = new ArrayList<Point2D_I32>();
        int fi = list.indexOf(first);
        int li = list.indexOf(last);
        if(fi == -1 || li == -1){
            return null;
        }
        for(int i = fi + 1; i != li; i++){
            if(i == list.size()){
                i = 0;
            }
            b.add(list.get(i));
        }
        return b;
    }
    private static List<Point2D_I32> between(List<Point2D_I32> list, int start, int end, int num){
        List<Point2D_I32> l = new ArrayList<Point2D_I32>();
        for(int i = start + 1; i < end; i += list.size()/num){
            l.add(list.get(i));
        }
        return l;
    }
    private static int G(){
        if(useSliders){
            return sl.getG();
        }
        return MIN_G;
    }
    private static int L(){
        if(useSliders){
            return sl.getL();
        }
        return MIN_L;
    }
    private static int Stay(){
        if(useSliders){
            return sl.getStay();
        }
        return STAY_VALUE;
    }
    private static int whWeight(){
        return sl.whWeight();
    }
    private static int apWeight(){
        return sl.apWeight();
    }
    private static int sideVerticalWeight(){
        return sl.sideVerticalWeight();
    }
    private static int noInternalWeight(){
        return sl.noInternalWeight();
    }
    private static int stayWeight(){
        return sl.stayWeight();
    }
}