package rainmaker.gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;
import javafx.util.Pair;
import rainmaker.Game;

import java.util.LinkedList;
import java.util.List;

public class BezierOval extends Group {
    public static final double CONTROL_POINT_STRENGTH = 1.25;

    private double majorAxisRadius, minorAxisRadius;
    private List<Pair<Point2D, Double>> endpoints;
    private Ellipse baseOval, controlPointsOval;
    private List<Point2D> controlPoints;
    private Color fill, stroke;

    public BezierOval(double majorAxisRadius, double minorAxisRadius,
                      Color fill, Color stroke) {
        this.majorAxisRadius = majorAxisRadius;
        this.minorAxisRadius = minorAxisRadius;
        baseOval = new Ellipse(majorAxisRadius, minorAxisRadius);
        this.fill = fill;
        this.stroke = stroke;
        baseOval.setFill(fill);
        getChildren().add(baseOval);
        augmentOvalWithBezierSegments();
    }

    private void augmentOvalWithBezierSegments() {
        makeControlPointOval();
        setEndPoints();
        setControlPoints();
        makeBezierCurves();
    }

    private void makeControlPointOval() {
        controlPointsOval = new Ellipse(
                majorAxisRadius * CONTROL_POINT_STRENGTH,
                minorAxisRadius * CONTROL_POINT_STRENGTH);
    }

    private void setEndPoints() {
        endpoints = new LinkedList<>();
        double theta = Game.randomInRange(35, 55);
        while (theta <= Math.toDegrees(2 * Math.PI)) {
            endpoints.add(new Pair<>(new Point2D(
                majorAxisRadius * Math.cos(Math.toRadians(theta)),
                minorAxisRadius * Math.sin(Math.toRadians(theta))), theta));
            theta += Game.randomInRange(35, 55);
        }
    }

    private void setControlPoints() {
        controlPoints = new LinkedList<>();
        for (int i = 0; i < endpoints.size() - 1; i++) {
            makeControlPointForEndpoints(i, i + 1);
        }
        makeControlPointForEndpoints(endpoints.size() - 1, 0);

    }

    private void makeControlPointForEndpoints(
            int startPointIndex, int endPointIndex) {
        double startTheta = endpoints.get(startPointIndex).getValue();
        double endTheta = endpoints.get(endPointIndex).getValue();
        double controlTheta = startTheta + ((endTheta - startTheta) / 2);
        if (endTheta < startTheta)
            controlTheta += Math.toDegrees(Math.PI);

        controlPoints.add(new Point2D(
                controlPointsOval.getRadiusX()
                        * Math.cos(Math.toRadians(controlTheta)),
                controlPointsOval.getRadiusY()
                        * Math.sin(Math.toRadians(controlTheta))));
    }

    private void makeBezierCurves() {
        for (int i = 0; i < controlPoints.size(); i++) {
            Point2D control = controlPoints.get(i);
            Point2D start = endpoints.get(i).getKey();
            Point2D end = endpoints.get((i + 1) % endpoints.size()).getKey();
            QuadCurve bezier = new QuadCurve(
                    start.getX(), start.getY(),
                    control.getX(), control.getY(),
                    end.getX(), end.getY()
            );
            bezier.setFill(fill);
            bezier.setStroke(stroke);
            getChildren().add(bezier);
        }
    }

    public Color getFill() {
        return fill;
    }

    public void setFill(Color fill) {
        this.fill = fill;
        getChildren().forEach(shape -> ((Shape) shape).setFill(fill));
    }

    public double getWidth() {
        return 2 * controlPointsOval.getRadiusX();
    }

    public double getHeight() {
        return 2 * controlPointsOval.getRadiusY();
    }
}
