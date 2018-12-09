import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import java.io.File;
import java.util.ArrayList;



public class HoleFiller {
    private final int HOLE = -1;
    private Mat image;
    private Imgcodecs imageCodecs;
    private File file;
    private ArrayList<Pixel> boundary;
    private int rows;
    private int cols;
    private int connectivity;
    private double power;
    private double epsilon;

    HoleFiller(String[] paths, int connectivity, double z, double epsilon) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.imageCodecs = new Imgcodecs();
        if (paths.length == 2) { // image and mask case
            Mat original = imageCodecs.imread(paths[0], 0);
            Mat mask = imageCodecs.imread(paths[1], 0);
            this.image = original.mul(mask);
        } else if (paths.length == 1) { // just image case
            this.image = imageCodecs.imread(paths[0], 0);
        } else {
            System.out.println("error, no file attached");
            return;
        }
        this.file = new File(paths[0]);
        this.rows = image.rows();
        this.cols = image.cols();
        this.connectivity = connectivity;
        this.boundary = new ArrayList<>();
        this.power = z;
        this.epsilon = epsilon;

    }

    public void fillHole() {

        findBoundary();

        while (!boundary.isEmpty()) {
            fillInnerBoundary();
        }

        saveImage();
    }

    private void fillInnerBoundary() {
        ArrayList<Pixel> newBoundary = new ArrayList<>();
        ArrayList<Pixel> holeNeighborhood = new ArrayList<>();
        for (Pixel p1 : boundary) { // go boundary pixels and fill their neighbors hole pixels
            findConnectedHolePixel(p1, holeNeighborhood); // find the neighboring hole pixels
            for (Pixel p2 : holeNeighborhood) {
                image.put(p2.getRow(), p2.getColumn(), calcIntensity(p2));
                newBoundary.add(p2);
            }
            holeNeighborhood.clear();
        }
        this.boundary = newBoundary;
    }

    private void findConnectedHolePixel(Pixel p1, ArrayList<Pixel> holeNeighborhood) {
        Pixel[] neighborhood = getNeighborhood(p1.getRow(), p1.getColumn());
        for (Pixel p2 : neighborhood) {
            if (image.get(p2.getRow(), p2.getColumn())[0] == HOLE) {
                holeNeighborhood.add(p2);
            }
        }
    }

    private double calcIntensity(Pixel p1) {
        double weightedAVG = 0;
        double AVG = 0;
        for (Pixel p2 : boundary) {
            AVG += weightFunction(p1, p2);
            weightedAVG += weightFunction(p1, p2) * image.get(p2.getRow(), p2.getColumn())[0];
        }
        return weightedAVG / AVG;
    }

    private double weightFunction(Pixel p1, Pixel p2) {
        return 1 / ((Math.pow(Pixel.distance(p1, p2), power) + epsilon));
    }

    private void findBoundary() {
        Pixel[] rectBoundary = boundingPixels();
        if (rectBoundary == null) {
            return;
        }
        int length = rectBoundary[3].getColumn() - rectBoundary[1].getColumn();
        int height = rectBoundary[2].getRow() - rectBoundary[0].getRow();
        int row = rectBoundary[0].getRow();
        int col = rectBoundary[1].getColumn();
        for (int i = row; i <= row + height; i++) {
            for (int j = col; j <= col + length; j++) {
                if (checkIfBoundary(i, j)) {
                    boundary.add(new Pixel(i, j));
                }
            }
        }

    }

    private boolean checkIfBoundary(int row, int col) {
        if (image.get(row, col)[0] == HOLE) {
            return false;
        }
        Pixel[] neighborhood = getNeighborhood(row, col);
        for (Pixel pixel : neighborhood) {
            if (image.get(pixel.getRow(), pixel.getColumn())[0] == HOLE) {
                return true;
            }
        }
        return false;
    }

    private Pixel[] getNeighborhood(int row, int col) {
        if (connectivity == 4) {
            Pixel[] neighborhood = new Pixel[4];
            neighborhood[0] = new Pixel(row - 1, col);
            neighborhood[1] = new Pixel(row, col + 1);
            neighborhood[2] = new Pixel(row + 1, col);
            neighborhood[3] = new Pixel(row, col - 1);
            return neighborhood;
        }
        Pixel[] neighborhood = new Pixel[8];
        neighborhood[0] = new Pixel(row - 1, col - 1);
        neighborhood[1] = new Pixel(row - 1, col);
        neighborhood[2] = new Pixel(row - 1, col + 1);
        neighborhood[3] = new Pixel(row, col + 1);
        neighborhood[4] = new Pixel(row + 1, col + 1);
        neighborhood[5] = new Pixel(row + 1, col);
        neighborhood[6] = new Pixel(row + 1, col - 1);
        neighborhood[7] = new Pixel(row, col - 1);
        return neighborhood;
    }

    private Pixel[] boundingPixels() {
        Pixel[] bounding = {findUpMost(), findLeftMost(), findDownMost(), findRightMost()};
        // 0- up most, 1- left most, 2- down most, 3 -right most
        for (Pixel p : bounding) {
            if (p == null) {
                return null;
            }
        }
        return bounding;
    }

    private Pixel findUpMost() {
        for (int i = 0; i < rows; i++) { // find up most
            for (int j = 0; j < cols; j++) {
                double[] data = image.get(i, j);
                if (data[0] == HOLE) {
                    return new Pixel(i - 1, j);
                }
            }
        }
        return null;
    }

    private Pixel findLeftMost() {
        for (int j = 0; j < cols; j++) { // find left most
            for (int i = 0; i < rows; i++) {
                double[] data = image.get(i, j);
                if (data[0] == HOLE) {
                    return new Pixel(i, j - 1);
                }
            }
        }
        return null;
    }

    private Pixel findDownMost() {
        for (int i = rows - 1; i > -1; i--) { // find down most
            for (int j = 0; j < rows; j++) {
                double[] data = image.get(i, j);
                if (data[0] == HOLE) {
                    return new Pixel(i + 1, j);
                }
            }
        }
        return null;
    }

    private Pixel findRightMost() {
        for (int j = cols - 1; j > -1; j--) { // find right most
            for (int i = 0; i < rows; i++) {
                double[] data = image.get(i, j);
                if (data[0] == HOLE) {
                    return new Pixel(i, j + 1);
                }
            }
        }
        return null;
    }

    private void saveImage() {
        imageCodecs.imwrite(this.file.getParent() + "\\fixed.jpg", image);

    }
}