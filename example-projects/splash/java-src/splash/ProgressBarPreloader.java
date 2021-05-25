package splash;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressBarPreloader extends Preloader {

  private static final double WIDTH = 400;
  private static final double HEIGHT = 400;

  private Stage preloaderStage;
  private Scene scene;
  private Label label;
  private ProgressBar bar;

  @Override
  public void init() throws Exception {
    bar = new ProgressBar(0);

    final Label title = new Label("Loading, please wait...");
    title.setTextAlignment(TextAlignment.CENTER);
    label = new Label("0%");

    final VBox root = new VBox(title, bar, label);
    root.setAlignment(Pos.CENTER);

    scene = new Scene(root, WIDTH, HEIGHT);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.preloaderStage = primaryStage;

    // Set preloader scene and show stage.
    scene.setFill(Color.TRANSPARENT);
    preloaderStage.initStyle(StageStyle.TRANSPARENT);
    preloaderStage.setScene(scene);
    preloaderStage.show();
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification pn) {
    if (pn instanceof ProgressNotification) {
      ProgressNotification info = (ProgressNotification)pn;
      // update progress bar based on what PreloadingApplication#init tells us
      bar.setProgress(info.getProgress());
      label.setText((info.getProgress()*100)+"%");
    }
  }

  @Override
  public void handleStateChangeNotification(StateChangeNotification info) {
    // Handle state change notifications.
    StateChangeNotification.Type type = info.getType();
    switch (type) {
      // Called after PreloadingApplication#init and before PreloadingApplication#start is called
      case BEFORE_START:
        // cljfx is loaded (happens at the end of PreloadingApplication#init), so remove the preloader!
        preloaderStage.hide();
        break;
    }
  }
}
