import java.awt.Color;

import ecs100.UI;

public class Rectangle {
	private String name;
	private double x;
	private double y;
	private double w;
	private double h;
	
	public void drawShape() {
		UI.setColor(Color.cyan);
		UI.drawRect(x, y, w, h);
	}
	
	public Rectangle(String name, double x, double y, double w, double h) {
		super();
		this.name = name;
		this.x = x;
		this.y = y;
		this.h = h;
		this.w = w;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public double getH() {
		return h;
	}

	public void setH(double h) {
		this.h = h;
	}

	public boolean isChosen(double mosX, double mosY) {
		if ((mosX > x) && (mosX < (x + w)) && (mosY > y) && (mosY < y + w)) {
			return true;
		} else {
				return false;
			}
		}

}
