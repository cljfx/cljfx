package splash;

import com.sun.javafx.application.LauncherImpl;
import java.lang.reflect.Method;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.stage.Stage;

public class PreloadingApplication extends Application {

  // fully qualifed entry point to your cljfx entry point.
  // here, this points to src/splash.clj's -main function.
  private final String CLJFX_MAIN = "splash/-main";

  public static void main(String[] args) {
    LauncherImpl.launchApplication(PreloadingApplication.class, ProgressBarPreloader.class, args);
  }

  // incrementally load various parts of Clojure and cljfx while providing
  // UI feedback as early as possible.
  @Override
  public void init() throws Exception {
    notifyPreloader(new Preloader.ProgressNotification(0.1));

    final Class<?> theClojureClass = Class.forName("clojure.java.api.Clojure");

    notifyPreloader(new Preloader.ProgressNotification(0.3));

    final Method theVarMethod = theClojureClass.getMethod("var", Object.class);
    final Method theReadMethod = theClojureClass.getMethod("read", String.class);

    final Class<?> theIFnClass = Class.forName("clojure.lang.IFn");
    final Method theInvokeMethod = theIFnClass.getMethod("invoke", Object.class);
    final Method theInvoke0Method = theIFnClass.getMethod("invoke");

    final Object theRequiringResolveVar = theVarMethod.invoke(theClojureClass, "clojure.core/requiring-resolve");

    notifyPreloader(new Preloader.ProgressNotification(0.4));

    notifyPreloader(new Preloader.ProgressNotification(0.6));

    final Object theAppMainVar = theInvokeMethod.invoke(
        theRequiringResolveVar,
        theReadMethod.invoke(theClojureClass, CLJFX_MAIN));

    // start application behind the preloader
    theInvoke0Method.invoke(theAppMainVar);

    notifyPreloader(new Preloader.ProgressNotification(0.99));
  }

  // cljfx UI is already up, don't need to do anything more
  @Override
  public void start(Stage stage) throws Exception {
  }
}
