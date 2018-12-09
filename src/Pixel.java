public class Pixel {
    private int row;
    private int column;

    Pixel(int x, int y) {
        row = x;
        column = y;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public static boolean equals(Pixel p1, Pixel p2) {
        return p1.getRow() == p2.getRow() && p1.getColumn() == p2.getColumn();
    }

    public static double distance(Pixel p1, Pixel p2) {
        return Math.pow((Math.pow(p1.getRow() - p2.getRow(), 2) + Math.pow(p1.getColumn() - p2.getColumn(), 2)),
                0.5);
    }
}
