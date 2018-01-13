package bg.panama.btc.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import bg.util.PointDouble;

public class PanelCanvasPrix {

	int w = 300;
	int h = 50;
	String currency;
	Graphics2D g2;

	BufferedImage bf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	ImageIcon imageIcon = new ImageIcon(bf);

	public PanelCanvasPrix(String currency) {
		super();
		this.currency = currency;

		g2 = bf.createGraphics();
		g2.setColor(Color.CYAN);
		g2.fillRect(0, 0, w, h);
	}

	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	private Double xMax;
	private Double xMin;
	private Double yMax;
	private Double yMin;
	private double xM;
	private double yM;
	private double dx;
	private double dy;

	private void initFromHistory(History h) {
		xMax = null;
		for (PointDouble p : h.getListPointsPrixBrut()) {
			initMinMax(p);
		}
	}

	void initMinMax(PointDouble p) {

		if (xMax == null) {
			xMax = p.x;
			xMin = p.x;
			yMax = p.y;
			yMin = p.y;
		}
		if (p.x > xMax) {
			xMax = p.x;
		}
		if (p.y > yMax) {
			yMax = p.y;
		}
		if (p.x < xMin) {
			xMin = p.x;
		}
		if (p.y < yMin) {
			yMin = p.y;
		}

		// dx = xMax - xMin;
		dx = 2 * 60 * 60 * 1000;
		// dy = yMax - yMin;
		yMax = 0.75d;
		yMin = -0.75d;
		dy = 1.5d;

		xM = (xMax + xMin) / 2;
		yM = (yMax + yMin) / 2;

	}

	public static final Color GREEN = new Color(0x99FF66);
	public static final Color RED = new Color(0xFF0066);

	public void update(String str, Color color, History history) {
		initFromHistory(history);
		g2.setColor(Color.white);
		g2.fillRect(0, 0, w, h);
		g2.setColor(Color.black);
		g2.drawString(str, 10, 20);
		g2.setColor(Color.gray);
		g2.drawLine(0, h / 2, w, h / 2);

		for (PointDouble p : history.getListPointsPrixBrut()) {
			if ((dx > 0.000000001) && (dy > 0.000000001)) {
				int x = (int) ((((p.x - xMin) / dx)) * w);
				int y = (int) ((((-p.y) / dy * h) + h / 2) * 0.9);
				g2.setColor(Color.red);
				g2.fillOval(x, y, 4, 4);
			}
		}
		for (PointDouble p : history.getListPointsPrixFiltre()) {
			if ((dx > 0.000000001) && (dy > 0.000000001)) {
				int x = (int) ((((p.x - xMin) / dx)) * w);
				int y = (int) ((((-p.y) / dy * h) + h / 2) * 0.9);
				g2.setColor(Color.blue);
				g2.fillOval(x, y, 4, 4);
				//System.out.println("Prix Filtre:\t"+str+"\t x: " + x + "  y :" + y);
			}
		}
	}

}