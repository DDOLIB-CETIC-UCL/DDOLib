package org.ddolib.examples.smic.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.LinkedList;
import java.util.List;

public class SmicChartView extends StackPane {

    private final TaskLineChart lineChart;
    private final XYChart.Series<Number, Number> series;
    private final Pane overlayPane; // Le calque pour dessiner "hors cadre"
    private final int initInventory;

    public SmicChartView(int initInventory, int maxInventory) {
        this.initInventory = initInventory;

        // 1. Configuration des axes
        final NumberAxis xAxis = new NumberAxis(0, 75, 1);
        xAxis.setLabel("Time");

        // Calcul dynamique de la borne Y
        final int yLimit = maxInventory + 5 - maxInventory % 5;
        final NumberAxis yAxis = new NumberAxis(0, yLimit, 5);
        yAxis.setLabel("Inventory");

        // 2. Création du Graphique Personnalisé
        lineChart = new TaskLineChart(xAxis, yAxis, maxInventory);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setLegendVisible(false);

        // IMPORTANT : On laisse 50px de vide en bas du graphique pour afficher nos rectangles
        lineChart.setPadding(new Insets(0, 0, 40, 0));

        series = new XYChart.Series<>();
        lineChart.getData().add(series);

        // 3. Création de l'Overlay (Calque transparent)
        overlayPane = new Pane();
        overlayPane.setPickOnBounds(false); // Permet aux clics de traverser vers le graphique
        overlayPane.setMouseTransparent(true);

        // 4. Le Callback : Quand le Chart a fini de se dessiner, on dessine l'overlay
        lineChart.setOnLayoutCallback(() -> drawOverlayTasks(lineChart.getTasks()));

        // 5. Assemblage : Chart au fond, Overlay au-dessus
        this.getChildren().addAll(lineChart, overlayPane);
    }

    public void refresh(List<SmicTask> tasks) {
        Platform.runLater(() -> {
            // Mise à jour des données de la courbe
            series.getData().clear();
            ObservableList<XYChart.Data<Number, Number>> list = FXCollections.observableArrayList();
            list.add(new XYChart.Data<>(0, initInventory));

            for (SmicTask task : tasks) {
                list.add(new XYChart.Data<>(task.end(), task.inventoryAtEnd()));
            }

            series.getData().setAll(list);

            // On passe les tâches au graphique (ce qui déclenchera le redessin de l'overlay)
            lineChart.setTasks(tasks);
        });
    }

    /**
     * Dessine les rectangles et IDs dans le calque supérieur (Overlay).
     * Utilise la conversion de coordonnées pour s'aligner avec les axes.
     */
    private void drawOverlayTasks(List<SmicTask> tasks) {
        overlayPane.getChildren().clear();

        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        // Position Y fixe dans l'overlay (dans la zone de padding du bas)
        double yPos = this.getHeight() - 40;

        for (SmicTask task : tasks) {
            // A. Récupérer les coordonnées X dans le monde du Graphique
            double xStartAxis = xAxis.getDisplayPosition(task.start());
            double xEndAxis = xAxis.getDisplayPosition(task.end());

            // B. Convertir ces coordonnées vers le monde de l'Overlay (Scène -> Local)
            // Cette étape est cruciale car les deux Panes n'ont pas forcément le même repère
            Point2D pStart = xAxis.localToScene(xStartAxis, 0);
            Point2D pEnd = xAxis.localToScene(xEndAxis, 0);

            // Sécurité si la fenêtre n'est pas encore visible
            if (pStart == null || pEnd == null) continue;

            Point2D pStartOverlay = overlayPane.sceneToLocal(pStart);
            Point2D pEndOverlay = overlayPane.sceneToLocal(pEnd);

            double realXStart = Math.min(pStartOverlay.getX(), pEndOverlay.getX());
            double width = Math.abs(pEndOverlay.getX() - pStartOverlay.getX());
            double height = 20;

            // C. Dessin du Rectangle
            Rectangle rect = new Rectangle(realXStart, yPos, width, height);
            rect.setFill(Color.ORANGE.deriveColor(0, 1, 1, 0.5));
            rect.setStroke(Color.ORANGE);

            // D. Dessin du Texte
            Text text = new Text("" + task.id());
            text.setFont(Font.font("Arial", FontWeight.BOLD, 10));

            // Centrage du texte
            text.setX(realXStart + (width - text.getLayoutBounds().getWidth()) / 2);
            text.setY(yPos + (height / 2) + 4);

            overlayPane.getChildren().addAll(rect, text);
        }
    }


    // --- CLASSE INTERNE : Graphique Personnalisé ---
    private static class TaskLineChart extends LineChart<Number, Number> {
        private final int maxInventory;
        private List<SmicTask> tasks = new LinkedList<>();

        // Le Callback pour prévenir l'extérieur
        private Runnable onLayoutCallback;

        public TaskLineChart(Axis<Number> xAxis, Axis<Number> yAxis, int maxInventory) {
            super(xAxis, yAxis);
            this.maxInventory = maxInventory;
        }

        // Setter pour définir l'action à effectuer après le layout
        public void setOnLayoutCallback(Runnable callback) {
            this.onLayoutCallback = callback;
        }

        public List<SmicTask> getTasks() {
            return tasks;
        }

        public void setTasks(List<SmicTask> tasks) {
            this.tasks = tasks;
            requestChartLayout(); // Demande une mise à jour visuelle
        }

        @Override
        protected void layoutPlotChildren() {
            // 1. Laisser JavaFX dessiner la courbe standard
            super.layoutPlotChildren();

            // 2. Dessiner nos éléments internes (Lignes limites)
            drawLimits();

            // 3. Déclencher le dessin externe (Overlay)
            if (onLayoutCallback != null) {
                onLayoutCallback.run();
            }
        }

        private void drawLimits() {
            ObservableList<Node> plotChildren = getPlotChildren();
            // Nettoyage des anciennes lignes limites
            plotChildren.removeIf(node -> "LIMIT_LINE".equals(node.getUserData()));

            NumberAxis yAxis = (NumberAxis) getYAxis();
            NumberAxis xAxis = (NumberAxis) getXAxis();

            drawHorizontalLine(plotChildren, xAxis, yAxis, maxInventory, Color.RED, "Max inventory");
        }

        private void drawHorizontalLine(ObservableList<Node> children, NumberAxis xAxis, NumberAxis yAxis, int value, Color color, String label) {
            double yPixel = yAxis.getDisplayPosition(value);
            if (Double.isNaN(yPixel)) return;

            double xEnd = xAxis.getWidth();

            // Ligne pointillée
            Line line = new Line(0, yPixel, xEnd, yPixel);
            line.setStroke(color);
            line.setStrokeWidth(2);
            line.getStrokeDashArray().addAll(10d, 5d);
            line.setUserData("LIMIT_LINE"); // Tag important pour le nettoyage

            // Label texte à droite
            Text text = new Text(label + " (" + value + ")");
            text.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            text.setFill(color);
            text.setX(xEnd - 100);
            text.setY(yPixel - 5);
            text.setUserData("LIMIT_LINE");

            children.add(line);
            children.add(text);
        }
    }
}