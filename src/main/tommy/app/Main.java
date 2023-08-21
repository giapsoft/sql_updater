package app;

import app.common.AppBuilder;
import app.common.AppState;
import app.forms.app.App;
import app.forms.capture.Capture;
import app.forms.export.DbHolder;
import app.forms.export.Export;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import setup.Config;

import javax.imageio.ImageIO;
import javax.jws.WebParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppState.get.init();
        DbHolder.init();
        Parent root = AppBuilder.getAsParent(currentScreen);
        primaryStage.setTitle("Synergix Supermodel Updater");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static Class<?> currentScreen = App.class;

    public static void main(String[] args) throws IOException {
        String svnPath = "D:\\draft\\testing-sql";
        if (args != null && args.length > 0) {
            svnPath = args[0];
            if (args.length > 1) {
                if ("export".equalsIgnoreCase(args[1])) {
                    currentScreen = Export.class;
                } else if ("capture".equalsIgnoreCase(args[1])) {
                    currentScreen = Capture.class;
                }
            }
        }
        new Config(svnPath);
        launch(args);
    }
}
