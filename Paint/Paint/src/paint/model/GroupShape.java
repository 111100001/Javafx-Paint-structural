package paint.model;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/* Composite Pattern: Treat a group of shapes as a single shape */
public class GroupShape extends Shape {
    private List<Shape> children = new ArrayList<>();

    public GroupShape() {
        // empty group
    }

    public GroupShape(List<Shape> shapes) {
        if (shapes != null) {
            for (Shape s : shapes) {
                if (s != null) {
                    children.add(s);
                }
            }
        }
    }

    public void addChild(Shape s) { if (s != null) children.add(s); }
    public void removeChild(Shape s) { children.remove(s); }
    public List<Shape> getChildren() { return children; }

    private Point2D computeTopLeft() {
        if (children.isEmpty()) return new Point2D(0,0);
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        for (Shape s : children) {
            Point2D tl = s.getTopLeft();
            if (tl.getX() < minX) minX = tl.getX();
            if (tl.getY() < minY) minY = tl.getY();
        }
        return new Point2D(minX, minY);
    }

    @Override
    public Point2D getTopLeft() {
        return computeTopLeft();
    }

    @Override
    public void setTopLeft(Point2D pos) {
        Point2D current = computeTopLeft();
        Point2D delta = pos.subtract(current);
        for (Shape s : children) {
            Point2D childTL = s.getTopLeft();
            s.setTopLeft(childTL.add(delta));
        }
        // don't call super.setTopLeft because we compute dynamically
    }

    @Override
    public void setColor(Color color) {
        for (Shape s : children) { s.setColor(color); }
    }

    @Override
    public void setFillColor(Color color) {
        for (Shape s : children) { s.setFillColor(color); }
    }

    @Override
    public void draw(Canvas canvas) {
        for (Shape s : children) {
            s.draw(canvas);
        }
    }

    @Override
    public Shape cloneShape() throws CloneNotSupportedException {
        GroupShape copy = (GroupShape) super.cloneShape();
        // deep clone children
        List<Shape> clonedChildren = new ArrayList<>();
        for (Shape s : this.children) {
            clonedChildren.add(s.cloneShape());
        }
        copy.children = clonedChildren;
        return copy;
    }
}

