package splash;

// Extra class indirection so JavaFX boots up correctly with uberjar.
// https://github.com/javafxports/openjdk-jfx/issues/236#issuecomment-426606561
public class Main {
  public static void main(String[] args) {
    PreloadingApplication.main(args);
  }
}
