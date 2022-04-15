package MrKagabond.painter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javafx.scene.canvas.GraphicsContext;

public class PaintModel extends Observable implements Observer {

	CircleCommand CIRCLE = new CircleCommand(null, 0);
	PolygonCommand POLYGON = new PolygonCommand();
	RectangleCommand RECTANGLE = new RectangleCommand(null, null);
	SquiggleCommand SQUIGGLE = new SquiggleCommand();

	public void save(PrintWriter writer) {
		writer.print("PaintSaveFileVersion1.0\n");
		for (PaintCommand c : this.commands) {
			if (c.getClass().isInstance(CIRCLE)) {
				writer.print("Circle\n");
				writer.print(c.displayGeneralDetails());
				writer.print("\tcenter:" + ((CircleCommand) c).getCentre() + "\n");
				writer.print("\tradius:" + ((CircleCommand) c).getRadius() + "\n");
				writer.print("End Circle\n");
			} else if (c.getClass().isInstance(POLYGON)) {
				writer.print("Polygon\n");
				writer.print(c.displayGeneralDetails());
				writer.print("\tpoints:\n");
				for (Point p : ((PolygonCommand) c).getPoints()) {
					writer.print("\t\tpoint: " + p.toString() + "\n");
				}
				writer.print("\tend points\n");
				writer.print("End Polygon\n");
			} else if (c.getClass().isInstance(RECTANGLE)) {
				writer.print("Rectangle\n");
				writer.print(c.displayGeneralDetails());
				writer.print("\tp1:" + ((RectangleCommand) c).getP1() + "\n");
				writer.print("\tp2:" + ((RectangleCommand) c).getP2() + "\n");
				writer.print("End Rectangle\n");
			} else if (c.getClass().isInstance(SQUIGGLE)) {
				writer.print("Squiggle\n");
				writer.print(c.displayGeneralDetails());
				writer.print("\tpoints\n");
				for (Point p : ((SquiggleCommand) c).getPoints()) {
					writer.print("\t\tpoint:" + p.toString() + "\n");
				}
				writer.print("\tend points\n");
				writer.print("End Squiggle\n");
			}
		}
		writer.print("EndPaintSaveFile\n");
		writer.close();
	}

	public void reset() {
		for (PaintCommand c : this.commands) {
			c.deleteObserver(this);
		}
		this.commands.clear();
		this.setChanged();
		this.notifyObservers();
	}

	public void addCommand(PaintCommand command) {
		this.commands.add(command);
		command.addObserver(this);
		this.setChanged();
		this.notifyObservers();
	}

	private ArrayList<PaintCommand> commands = new ArrayList<PaintCommand>();

	public void executeAll(GraphicsContext g) {
		for (PaintCommand c : this.commands) {
			c.execute(g);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers();
	}
}
