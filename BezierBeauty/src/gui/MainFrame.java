package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This is the MainFrame main() class for the BezierBeauty project,
 * creating a simple simulation of how Bezier curves are drawn.
 * The frame steps through shapes, increasing how many sided the 
 * shape is, with a maximum number of sides equal to the number
 * of different colors. At the very least, this is interesting to
 * watch as it cycles through.
 * 
 * @author Chuck Meriam, theInterrupted
 * @version 1.0
 *
 */
public class MainFrame implements Runnable, MouseListener{
	private static final int WIN_HEIGHT = 700;
	private static final int WIN_WIDTH = 700;
	private static final Dimension WIN_SIZE = new Dimension(WIN_WIDTH, WIN_WIDTH);
	private static final double TIME_STEP = 0.01;
	private static final Color[] colors = {new Color(204,0,0),new Color(255,153,0),new Color(153,204,0),
										   new Color(0,204,153),new Color(0,102,204),new Color(102,0,255),
										   new Color(204,0,204),new Color(214,0,147),new Color(255,80,80)};
	private MainPanel panel;
	private BufferedImage buffer;
	private List<Point2D[]> bezierHandles = new ArrayList<Point2D[]>();
	private List<Point2D> bezierPoints = new ArrayList<Point2D>();
	private int sides = 3;
	private double t = 0;
	
	/**
	 * Construct the class, setting up the MainPanel (private class
	 * that extends JPanel) and the JFrame.
	 */
	public MainFrame(){
		panel = new MainPanel();
		panel.setSize(WIN_SIZE);
		
		JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setResizable(false);
        frame.setSize(WIN_WIDTH, WIN_HEIGHT);
        frame.setLocation(5, 5);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
		Thread thread = new Thread(new MainFrame());
		thread.start();
	}

	public void mouseClicked(MouseEvent arg0) {	}
	public void mouseEntered(MouseEvent arg0) {	}
	public void mouseExited(MouseEvent arg0) { }
	public void mousePressed(MouseEvent arg0) {	}
	public void mouseReleased(MouseEvent arg0) { }

	/**
	 * The main loop, which after a set amount of
	 * time, will repaint the MainPanel.
	 */
	public void run() {
		while(true){
			try {
				Thread.sleep(50);
			} catch (Exception e){
				e.printStackTrace();
			}
			panel.repaint();
		}
	}
	
	/**
	 * Redraws the BufferedImage painted onto the MainPanel.
	 * With every call to repaint(), this method also
	 * recalculates the necessary changes to update the
	 * animation.
	 * 
	 * @return BufferedImage to be painted onto the MainPanel
	 */
	private BufferedImage drawBuffer(){
		buffer = new BufferedImage(WIN_WIDTH, WIN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffer.createGraphics();
		RenderingHints rh = new RenderingHints(
	             RenderingHints.KEY_TEXT_ANTIALIASING,
	             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g.setRenderingHints(rh);
	    
		g.setColor(new Color(20,20,20));
		g.fillRect(0, 0, WIN_WIDTH, WIN_HEIGHT);
		setupBezierHandles();
		
		for (int i = 0; i < bezierHandles.size(); i++){
			Point2D[] pts = bezierHandles.get(i);
			g.setColor(colors[i]);
			g.setStroke(new BasicStroke(0.3f));
			for (Point2D pt : pts){
				g.drawOval((int)(pt.getX()-5), (int)(WIN_HEIGHT-(pt.getY()+5)), 10, 10);
			}
			g.setStroke(new BasicStroke(1.0f));
			for (int j = 0; j < pts.length - 1; j++){
				g.drawLine((int)pts[j].getX(), (int)(WIN_HEIGHT-pts[j].getY()),
							(int)pts[j+1].getX(), (int)(WIN_HEIGHT-pts[j+1].getY()));
			}
		}
		
		g.setColor(new Color(255,255,255));
//		for (Point2D pt : bezierPoints){
//			g.fillOval((int)pt.getX(), (int)(WIN_HEIGHT-pt.getY()), 2, 2);
//		}
		g.setColor(colors[sides - 2]);
		
		for (int i = 0; i < bezierPoints.size() - 1; i++){
			Point2D pt1 = bezierPoints.get(i);
			Point2D pt2 = bezierPoints.get(i+1);
			g.drawLine((int)pt1.getX(), (int)(WIN_HEIGHT-pt1.getY()), 
					(int)pt2.getX(), (int)(WIN_HEIGHT-pt2.getY()));
		}
		timeStep();
		return buffer;
	}
	
	/**
	 * This method clears and builds the Bezier handle
	 * list by first calculating the points needed for
	 * the current shape, and then sending those points
	 * to the calculateBezierPoints method for all
	 * subsequent calculations.
	 */
	private void setupBezierHandles(){
		double x_cent = WIN_WIDTH/2;
		double y_cent = WIN_HEIGHT/2;
		double radius = WIN_WIDTH/2-50;
		double rad = Math.PI/2;
		Point2D[] bezier = new Point2D[sides + 1];
		for (int i = 0; i < sides; i++){
			bezier[i] = new Point2D.Double(x_cent + radius * Math.cos(rad),
								  y_cent + radius * Math.sin(rad));
			rad += (Math.PI*2)/sides;
		}
		bezier[sides] = bezier[0];
		bezierHandles.removeAll(bezierHandles);
		calculateBezierPoints(bezier);
		
	}
	
	/**
	 * This method calculates the sub-points of any array of points
	 * as a function of midpoints, i.e. point c is calculated as a
	 * point between point a and point b, and where along line ab 
	 * is determined by a percentage. This method is used recursively
	 * until only 1 point remains.
	 * 
	 * @param pts Collection of points
	 */
	private void calculateBezierPoints(Point2D[] pts){
		bezierHandles.add(pts);
		Point2D[] npts = new Point2D[pts.length - 1];
		int i = 0;
		double x, y;
		for (; i < pts.length - 1; i++){
			x = pts[i].getX() + ((pts[i+1].getX() - pts[i].getX()) * t);
			y = pts[i].getY() + ((pts[i+1].getY() - pts[i].getY()) * t);
			npts[i] = new Point2D.Double(x,y);
		}
		if (pts.length == 2){
			x = pts[0].getX() + ((pts[1].getX() - pts[0].getX()) * t);
			y = pts[0].getY() + ((pts[1].getY() - pts[0].getY()) * t);
			bezierPoints.add(new Point2D.Double(x,y));
		}
		if (npts.length > 1){
			calculateBezierPoints(npts);
		}
	}
	
	/**
	 * This method is used to track the percentage as a function
	 * of time by adding the TIME_STEP for each repaint, as well
	 * as adding to the count of sides on a shape.
	 */
	private void timeStep(){
		if (t <= 1){
			t += TIME_STEP;
		} else if (sides < colors.length) {
			t = 0;
			sides += 1;
		}
	}
	
	/**
	 * This simple private class that extends JPanel is
	 * used just to Ovverid the paint method and insert
	 * the Buffered Image.
	 *
	 */
	private class MainPanel extends JPanel{
		private static final long serialVersionUID = 1L;

		@Override  
        public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g; 
			g2.drawImage(drawBuffer(),0,0,this);
		}
	}

}
