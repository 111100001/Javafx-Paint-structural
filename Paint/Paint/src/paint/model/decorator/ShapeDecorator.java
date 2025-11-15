package paint.model.decorator;

import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import paint.model.Shape;
import paint.model.iShape;
import paint.model.GroupShape;

/* Base Decorator: delegates everything to the wrapped shape */
public class ShapeDecorator implements iShape {
    protected final Shape delegate;

    public ShapeDecorator(Shape delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setPosition(Point2D position) { delegate.setPosition(position); }

    @Override
    public Point2D getPosition() { return delegate.getPosition(); }

    @Override
    public void setProperties(Map<String, Double> properties) { delegate.setProperties(properties); }

    @Override
    public Map<String, Double> getProperties() { return delegate.getProperties(); }

    @Override
    public void setColor(Color color) { delegate.setColor(color); }

    @Override
    public Color getColor() { return delegate.getColor(); }

    @Override
    public void setFillColor(Color color) { delegate.setFillColor(color); }

    @Override
    public Color getFillColor() { return delegate.getFillColor(); }

    @Override
    public void draw(Canvas canvas) { delegate.draw(canvas); }

    @Override
    public Object clone() throws CloneNotSupportedException { return delegate.clone(); }

    public static iShape withSelection(Shape shape) {
        return new SelectionDecorator(shape);
    }
}

/* SelectionDecorator: draws a dashed highlight rectangle around the wrapped shape */
class SelectionDecorator extends ShapeDecorator {
    private static final Color HIGHLIGHT = Color.web("#3fa7ff", 0.9);

    public SelectionDecorator(Shape delegate) {
        super(delegate);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineDashes(6, 6);
        gc.setLineWidth(1.5);
        gc.setStroke(HIGHLIGHT);

        // Base top-left from the shape itself (works for all, including GroupShape)
        Point2D tl = delegate.getTopLeft();
        double w = 0, h = 0;

        if (delegate instanceof GroupShape) {
            // Compute bounding box across children
            GroupShape g = (GroupShape) delegate;
            double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
            for (Shape child : g.getChildren()) {
                Map<String, Double> cp = child.getProperties();
                Point2D ctl = child.getTopLeft();
                double cw = 0, ch = 0;
                String ctype = child.getClass().getSimpleName();
                switch (ctype) {
                    case "Rectangle":
                    case "Square":
                        cw = cp.getOrDefault("width", 0.0);
                        ch = cp.getOrDefault("height", 0.0);
                        break;
                    case "Ellipse":
                    case "Circle":
                        cw = cp.getOrDefault("hRadius", 0.0) * 2;
                        ch = cp.getOrDefault("vRadius", 0.0) * 2;
                        break;
                    case "Line": {
                        double x1 = cp.getOrDefault("startPositionX", 0.0);
                        double y1 = cp.getOrDefault("startPositionY", 0.0);
                        double x2 = cp.getOrDefault("endPositionX", 0.0);
                        double y2 = cp.getOrDefault("endPositionY", 0.0);
                        double cminX = Math.min(x1, x2), cminY = Math.min(y1, y2);
                        double cmaxX = Math.max(x1, x2), cmaxY = Math.max(y1, y2);
                        ctl = new Point2D(cminX, cminY);
                        cw = (cmaxX - cminX); ch = (cmaxY - cminY);
                        break;
                    }
                    case "Triangle": {
                        double tx1 = cp.getOrDefault("startPositionX", 0.0);
                        double ty1 = cp.getOrDefault("startPositionY", 0.0);
                        double tx2 = cp.getOrDefault("endPositionX", 0.0);
                        double ty2 = cp.getOrDefault("endPositionY", 0.0);
                        double tx3 = cp.getOrDefault("thirdPointX", 0.0);
                        double ty3 = cp.getOrDefault("thirdPointY", 0.0);
                        double cminX = Math.min(tx1, Math.min(tx2, tx3));
                        double cminY = Math.min(ty1, Math.min(ty2, ty3));
                        double cmaxX = Math.max(tx1, Math.max(tx2, tx3));
                        double cmaxY = Math.max(ty1, Math.max(ty2, ty3));
                        ctl = new Point2D(cminX, cminY);
                        cw = (cmaxX - cminX); ch = (cmaxY - cminY);
                        break;
                    }
                }
                minX = Math.min(minX, ctl.getX());
                minY = Math.min(minY, ctl.getY());
                maxX = Math.max(maxX, ctl.getX() + cw);
                maxY = Math.max(maxY, ctl.getY() + ch);
            }
            if (minX == Double.POSITIVE_INFINITY) {
                // empty group fallback: nothing to draw
                return;
            }
            tl = new Point2D(minX, minY);
            w = Math.max(0, maxX - minX);
            h = Math.max(0, maxY - minY);
        } else {
            // Non-group shapes: use their properties to find size
            Map<String, Double> p = delegate.getProperties();
            String type = delegate.getClass().getSimpleName();
            switch (type) {
                case "Rectangle":
                case "Square":
                    w = p.getOrDefault("width", 0.0);
                    h = p.getOrDefault("height", 0.0);
                    break;
                case "Ellipse":
                case "Circle":
                    w = p.getOrDefault("hRadius", 0.0) * 2;
                    h = p.getOrDefault("vRadius", 0.0) * 2;
                    break;
                case "Line": {
                    double x1 = p.getOrDefault("startPositionX", 0.0);
                    double y1 = p.getOrDefault("startPositionY", 0.0);
                    double x2 = p.getOrDefault("endPositionX", 0.0);
                    double y2 = p.getOrDefault("endPositionY", 0.0);
                    double minX = Math.min(x1, x2), minY = Math.min(y1, y2);
                    double maxX = Math.max(x1, x2), maxY = Math.max(y1, y2);
                    tl = new Point2D(minX, minY);
                    w = (maxX - minX); h = (maxY - minY);
                    break;
                }
                case "Triangle": {
                    double tx1 = p.getOrDefault("startPositionX", 0.0);
                    double ty1 = p.getOrDefault("startPositionY", 0.0);
                    double tx2 = p.getOrDefault("endPositionX", 0.0);
                    double ty2 = p.getOrDefault("endPositionY", 0.0);
                    double tx3 = p.getOrDefault("thirdPointX", 0.0);
                    double ty3 = p.getOrDefault("thirdPointY", 0.0);
                    double minX = Math.min(tx1, Math.min(tx2, tx3));
                    double minY = Math.min(ty1, Math.min(ty2, ty3));
                    double maxX = Math.max(tx1, Math.max(tx2, tx3));
                    double maxY = Math.max(ty1, Math.max(ty2, ty3));
                    tl = new Point2D(minX, minY);
                    w = (maxX - minX); h = (maxY - minY);
                    break;
                }
            }
        }

        gc.strokeRect(tl.getX() - 3, tl.getY() - 3, w + 6, h + 6);
        gc.setLineDashes(null);
    }
}
