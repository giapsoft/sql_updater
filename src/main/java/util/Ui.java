package util;

import java.awt.*;

public class Ui extends Cache {
    public static Ui get = new Ui();
    public Dimension screenSize(){
        return cache("screenSize", () -> {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            return toolkit.getScreenSize();
        });
    }

    public static Dimension size(Dimension origin, double rate) {
        return size(origin, rate, rate);
    }

    public static Dimension size(Dimension origin, double rateWidth, double rateHeight) {
        return new Dimension((int) (origin.width * rateWidth), (int) (origin.height * rateHeight));
    }

    public Dimension screenSize(double scale){
        double width = screenSize().width * scale;
        double height = screenSize().height * scale;
        return new Dimension((int) width, (int) height);
    }

    public Dimension screenSize(double scaleWidth, double scaleHeight){
        double width = screenSize().width * scaleWidth;
        double height = screenSize().height * scaleHeight;
        return new Dimension((int) width, (int) height);
    }

    public void center(Component component) {
        final int x = (Ui.get.screenSize().width - component.getWidth()) / 2;
        final int y = (Ui.get.screenSize().height - component.getHeight()) / 2;
        component.setLocation(x, y);
    }
}
