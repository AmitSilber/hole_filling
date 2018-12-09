import org.opencv.core.Core;

public class Main {


    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
       HoleFiller obj = new HoleFiller(args, 4, 2, 0.0001);
        obj.fillHole();

    }
}
