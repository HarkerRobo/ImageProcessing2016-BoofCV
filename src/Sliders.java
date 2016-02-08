import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class Sliders extends Thread implements ChangeListener
{
    JFrame frame;
    private static JSlider GSlide = new JSlider(0, 255);
    private static JLabel GText = new JLabel("G");
    private static JSlider LSlide = new JSlider(-510, 255);
    private static JLabel LText = new JLabel("L");
    private static JSlider StaySlide = new JSlider(-510, 255);
    private static JLabel StayText = new JLabel("STAY");
    private static JSlider WHWSlide = new JSlider(0, 1000);
    private static JLabel WHWText = new JLabel("WHW");
    private static JSlider APWSlide = new JSlider(0, 1000);
    private static JLabel APWText = new JLabel("APW");
    private static JSlider SVWSlide = new JSlider(0, 1000);
    private static JLabel SVWText = new JLabel("SVW");
    private static JSlider NIWSlide = new JSlider(0, 1000);
    private static JLabel NIWText = new JLabel("NIW");
    private static JSlider SWSlide = new JSlider(0, 100, 1);
    private static JLabel SWText = new JLabel("SW");
    private static JLabel avg = new JLabel("avgg: avgl:");
    private static JPanel sliders = new JPanel();
    public Sliders(int l, int g, int stay){
        GSlide.setValue(g);
        LSlide.setValue(l);
        StaySlide.setValue(stay);
        frame = new JFrame("Sliders");
        sliders.add(GText);
        sliders.add(GSlide);
        GSlide.addChangeListener(this);
        sliders.add(LText);
        sliders.add(LSlide);
        LSlide.addChangeListener(this);
        //sliders.add(StayText);
        //sliders.add(StaySlide);
        //StaySlide.addChangeListener(this);
        sliders.add(WHWText);
        sliders.add(WHWSlide);
        WHWSlide.addChangeListener(this);
        sliders.add(APWText);
        sliders.add(APWSlide);
        APWSlide.addChangeListener(this);
        sliders.add(SVWText);
        sliders.add(SVWSlide);
        SVWSlide.addChangeListener(this);
        sliders.add(NIWText);
        sliders.add(NIWSlide);
        NIWSlide.addChangeListener(this);
        sliders.add(SWText);
        sliders.add(SWSlide);
        SWSlide.addChangeListener(this);
        sliders.add(avg);
        frame.add(sliders);
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
    }
    public int getG(){
        return GSlide.getValue();
    }
    public int getL(){
        return LSlide.getValue();
    }
    public int getStay(){
        return StaySlide.getValue();
    }
    public static int whWeight(){
        return WHWSlide.getValue();
    }
    public static int apWeight(){
        return APWSlide.getValue();
    }
    public static int sideVerticalWeight(){
        return SVWSlide.getValue();
    }
    public static int noInternalWeight(){
        return NIWSlide.getValue();
    }
    public static int stayWeight(){
        return SWSlide.getValue();
    }
    @Override
    public void stateChanged(ChangeEvent e)
    {
        //if(e.getSource().equals(GSlide)){
            GText.setText("G: " + GSlide.getValue());
        //} else if(e.getSource().equals(LSlide)){
            LText.setText("L: " + LSlide.getValue());
        //} else if(e.getSource().equals(StaySlide)){
            StayText.setText("Stay: "  + StaySlide.getValue());
        //}
            WHWText.setText("WHW: " + WHWSlide.getValue());
            APWText.setText("APW: " + APWSlide.getValue());
            SVWText.setText("SVW: " + SVWSlide.getValue());
            NIWText.setText("NIW: " + NIWSlide.getValue());
            SWText.setText("SW: " + SWSlide.getValue());
    }
    public void setAvg(long avgg, long avgl){
        avg.setText("avgg: " + avgg + " avgl: " + avgl);
    }
}
