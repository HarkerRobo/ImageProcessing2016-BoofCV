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
    private static JPanel sliders = new JPanel();
    public Sliders(int l, int g){
        GSlide.setValue(g);
        LSlide.setValue(l);
        frame = new JFrame("Sliders");
        sliders.add(GText);
        sliders.add(GSlide);
        GSlide.addChangeListener(this);
        sliders.add(LText);
        sliders.add(LSlide);
        LSlide.addChangeListener(this);
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
    @Override
    public void stateChanged(ChangeEvent e)
    {
        if(e.getSource().equals(GSlide)){
            GText.setText("G: " + GSlide.getValue());
        } else if(e.getSource().equals(LSlide)){
            LText.setText("L: " + LSlide.getValue());
        }
    }
}
