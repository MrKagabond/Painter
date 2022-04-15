package MrKagabond.painter;

import javafx.scene.input.MouseEvent;

class PolygonManipulatorStrategy extends ShapeManipulatorStrategy {
	PolygonManipulatorStrategy(PaintModel paintModel) {
		super(paintModel);
	}

	private PolygonCommand polygonCommand;
	private boolean hasStarted = false;

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPrimaryButtonDown()) {
			if (hasStarted) {
				this.polygonCommand.add(new Point((int) e.getX(), (int) e.getY()));
			} else {
				hasStarted = true;
				this.polygonCommand = new PolygonCommand();
				this.addCommand(polygonCommand);
				this.polygonCommand.add(new Point((int) e.getX(), (int) e.getY()));
			}
		} else if (e.isSecondaryButtonDown()) {
			hasStarted = false;
			polygonCommand = null;
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (hasStarted) {
			this.polygonCommand.add(new Point((int) e.getX(), (int) e.getY()));
			this.polygonCommand.pop();
		}
	}
}