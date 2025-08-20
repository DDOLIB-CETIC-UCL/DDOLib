package org.ddolib.astar.examples.JobShop;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class JobShopVisu {
    private final int hScale; // height scale
    private final int wScale;// width scale
    private final JSInstance data;

    // Arrays for highlight
    private final ArrayList<Set<Integer>> highlightSets    = new ArrayList<>();
    private final ArrayList<Format>    highlightFormats = new ArrayList<>();
    private final ArrayList<Color>     highlightColors  = new ArrayList<>();

    private Graphics2D g2d;

    public JobShopVisu(JSInstance data){
        this(50,10, data); // width scale
    }

    public JobShopVisu(int hScale, int wScale, JSInstance data){
        this.hScale = hScale;
        this.wScale = wScale;
        this.data = data;
    }

    /**
     * Output an image of the solution from an instance of jobShopSolution
     * @param jobShopSolution The solution that should be represented
     * @param path The path at which the image is stored
     */
    public void makeGraph(int[][] jobShopSolution, String path, int makespan){
        makeGraph(jobShopSolution,makespan,path);
    }



    /**
     * Output an image of an arrangement of tasks
     * @param solution A matrix of task that should be represented
     * @param maximalMakespanWidth The maximal makespan width that should be represented
     * @param path The path at which the image is stored
     */
    public void makeGraph(int[][] solution, int maximalMakespanWidth, String path) {


        int width = (maximalMakespanWidth*wScale) + 1;
        int height = solution.length * hScale + 1;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);
        for (int job = 0; job < solution.length; job++) {
            for (int op=0; op<solution[job].length; op++) {
                int x = solution[job][op] * wScale;
                int y = data.getMachine()[job][op]* hScale;
                int w = data.getDuration()[job][op] * wScale;
                int h = hScale;
                g2d.setColor(getColor(job));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(Color.BLACK);
                g2d.draw(new Rectangle(x, y, w, h));
                String text = "j" + job + "t" + op;
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = metrics.stringWidth(text);
                int textHeight = metrics.getHeight();

                // Centrer le texte dans le rectangle
                int textX = x + (w - textWidth) / 2;
                int textY = y + (h + textHeight) / 2 - metrics.getDescent();

                g2d.drawString(text, textX, textY);

            }
        }
        g2d.dispose();
        File file = new File(path);
        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            System.out.println("Could not output graph solution");
        }
    }
    /**
     * Drawing formats for the highlight of tasks
     */
    public enum Format{
        /** draw the two diagonals of the window**/
        CROSS,
        /** draw a dot in the middle of the window **/
        DOT,
        /** draw an inner border inside the window **/
        INNER_BORDER,
        /** draw a rectangle inside the window **/
        INNER_RECTANGLE,
        /** draw a large border around the window **/
        LARGE_BORDER,
    }


    // endregion
    // ======================================================================
    // region Color
    // ======================================================================

    private boolean errorPrinted = false;
    private Color getColor(int job){
        if(job < colors.length){
            return colors[job];
        }
        if (!errorPrinted){
            String stringBuilder =
                    "JobShopVisu's color palette is currently set to only support " +
                            colors.length + " colors.\n" +
                            "Jobs whose id is larger than " + (colors.length - 1) +
                            " will be colored in gray.";
            System.err.println(stringBuilder);
            errorPrinted = true;
        }
        return Color.GRAY;
    }

    private static Color[] colors = {
            new Color(255, 255, 0, 100),      // YELLOW
            new Color(192, 192, 192, 100),    // LIGHT_GRAY
            new Color(0, 255, 255, 100),      // CYAN
            new Color(0, 255, 0, 100),        // GREEN
            new Color(255, 200, 0, 100),      // ORANGE
            new Color(255, 0, 255, 100),      // MAGENTA
            new Color(130, 51, 255, 100),     // custom purple
            new Color(255, 0, 0, 100),        // RED
            new Color(255, 175, 175, 100),    // PINK
            new Color(53, 80, 255, 100)       // custom blue
    };

}
