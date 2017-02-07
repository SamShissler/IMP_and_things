/*
 *Hunter Lloyd
 * Copyrite.......I wrote, ask permission if you want to use it outside of class.
 */

/*
  |x|1. Fix the reset function in the pulldown menu
  |x| 2. Rotate image 90 degrees, odd shaped images should work
  |X| 3. Show a histogram of the colors in a separate window
  |X|       3.5. Use the CDF to normalize the distribution evenly
  |X|       (note) https://en.wikipedia.org/wiki/Histogram_equalization
  |x| 4. Turn a color image into a grayscale and display it. 0.21 R + 0.72 G + 0.07 B
  |X| 5. Turn a color image into a grayscale image and then do a 3x3 mask to do an edge detection
  |_| 6. Track a colored object.....orange is easiest. Results is a binary image that is black except where the colored object is located.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.PixelGrabber;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;

class IMP implements MouseListener{
    JFrame frame;
    JPanel mp;
    JButton start;
    JScrollPane scroll;
    JMenuItem openItem, exitItem, resetItem;
    Toolkit toolkit;
    File pic;
    ImageIcon img;
    int colorX, colorY;
    int [] pixels;
    int [] results;
    //Instance Fields you will be using below

    //This will be your height and width of your 2d array
    int height=0, width=0;

    //your 2D array of pixels
    int picture[][];
    
    Histogram redH, blueH, greenH;
    int maxValue;
    int EDM = 2;
    int thresh = 127;

    /*
     * In the Constructor I set up the GUI, the frame the menus. The open pulldown
     * menu is how you will open an image to manipulate.
     */
    IMP(){
	toolkit = Toolkit.getDefaultToolkit();
	frame = new JFrame("Image Processing Software by Hunter");
	JMenuBar bar = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenu functions = getFunctions();
	frame.addWindowListener(new WindowAdapter(){
		@Override
		public void windowClosing(WindowEvent ev){quit();}
            });
	openItem = new JMenuItem("Open");
	openItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){ handleOpen(); }
	    });
	resetItem = new JMenuItem("Reset");
	resetItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){ reset(); }
	    });
	exitItem = new JMenuItem("Exit");
	exitItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){ quit(); }
	    });
	file.add(openItem);
	file.add(resetItem);
	file.add(exitItem);
	bar.add(file);
	bar.add(functions);
	frame.setSize(600, 600);
	mp = new JPanel();
	mp.setBackground(new Color(0, 0, 0));
	scroll = new JScrollPane(mp);
	frame.getContentPane().add(scroll, BorderLayout.CENTER);
	JPanel butPanel = new JPanel();
	butPanel.setBackground(Color.black);
	start = new JButton("start");
	start.setEnabled(false);
	start.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		}
	    });
	butPanel.add(start);
	frame.getContentPane().add(butPanel, BorderLayout.SOUTH);
	frame.setJMenuBar(bar);
	frame.setVisible(true);
    }

    /*
     * This method creates the pulldown menu and sets up listeners to selection of the menu choices. If the listeners are activated they call the methods
     * for handling the choice, fun1, fun2, fun3, fun4, etc. etc.
     */

    private JMenu getFunctions(){
	JMenu fun = new JMenu("Functions");
	JMenuItem firstItem = new JMenuItem("Rotate 90 right");
	firstItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		    turn90right();
		}
	    });
	JMenuItem secondItem = new JMenuItem("Rotate 90 left");
	secondItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		    turn90left();
		}
	    });
	JMenuItem thirdItem = new JMenuItem("Grey Scale");
	thirdItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		    greyScale();
		}
	    });
	JMenuItem histograms = new JMenuItem("Histograms");
	histograms.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		    makeHistograms();
		}
	    });
	    
	JMenuItem fourthItem = new JMenuItem("Edge Detection");
	fourthItem.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		    edgeDetect();
		}
	    });
	JMenuItem equalize = new JMenuItem("Equalize");
	equalize.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt){
		    equalize();
		}
	    });
	fun.add(firstItem);
	fun.add(secondItem);
	fun.add(thirdItem);
	fun.add(histograms);
	fun.add(fourthItem);
	fun.add(equalize);
	return fun;
    }

    /*
     * This method handles opening an image file, breaking down the picture to a one-dimensional array and then drawing the image on the frame.
     * You don't need to worry about this method.
     */
    private void handleOpen(){
	img = new ImageIcon();
	JFileChooser chooser = new JFileChooser();
	int option = chooser.showOpenDialog(frame);
	if(option == JFileChooser.APPROVE_OPTION) {
	    pic = chooser.getSelectedFile();
	    img = new ImageIcon(pic.getPath());
	}
	width = img.getIconWidth();
	height = img.getIconHeight();

	JLabel label = new JLabel(img);
	label.addMouseListener(this);
	pixels = new int[width*height];

	results = new int[width*height];


	Image image = img.getImage();

	PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width );
	try{
	    pg.grabPixels();
	}catch(InterruptedException e){
	    System.err.println("Interrupted waiting for pixels");
	    return;
	}
	for(int i = 0; i<width*height; i++)
	    results[i] = pixels[i];
	turnTwoDimensional();
	mp.removeAll();
	mp.add(label);
	mp.revalidate();
    }

    /*
     * The libraries in Java give a one dimensional array of RGB values for an image, I thought a 2-Dimensional array would be more usefull to you
     * So this method changes the one dimensional array to a two-dimensional.
     */
    private void turnTwoDimensional(){
	picture = new int[height][width];
	for(int i=0; i<height; i++){
	    for(int j=0; j<width; j++)
		picture[i][j] = pixels[i*width+j];
	}
    }
    /*
     *  This method takes the picture back to the original picture
     */
    private void reset(){
	Image image = img.getImage();
	JLabel label = new JLabel(img);
	label.addMouseListener(this);

	PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width );
	try{
	    pg.grabPixels();
	}catch(InterruptedException e){
	    System.err.println("Interrupted waiting for pixels");
	    return;
	}
	for(int i = 0; i<width*height; i++)
	    results[i] = pixels[i];
	turnTwoDimensional();
	mp.removeAll();
	mp.add(label);
	mp.revalidate();
    }
    /*
     * This method is called to redraw the screen with the new image.
     */
    private void resetPicture(){
	for(int i=0; i<height; i++)
	    for(int j=0; j<width; j++)
		pixels[i*width+j] = picture[i][j];
	Image img2 = toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));

	JLabel label2 = new JLabel(new ImageIcon(img2));
	mp.removeAll();
	mp.add(label2);

	mp.revalidate();
	mp.repaint();

    }
    /*
     * This method takes a single integer value and breaks it down doing bit manipulation to 4 individual int values for A, R, G, and B values
     */
    private int [] getPixelArray(int pixel){
	int temp[] = new int[4];
	temp[0] = (pixel >> 24) & 0xff;
	temp[1]   = (pixel >> 16) & 0xff;
	temp[2] = (pixel >>  8) & 0xff;
	temp[3]  = (pixel      ) & 0xff;
	return temp;

    }
    /*
     * This method takes an array of size 4 and combines the first 8 bits of each to create one integer.
     */
    private int getPixels(int rgb[]){
	int alpha = 0;
	int rgba = (rgb[0] << 24) | (rgb[1] <<16) | (rgb[2] << 8) | rgb[3];
	return rgba;
    }

    public void getValue(int x, int y){
	int pix = picture[x][y];
	int temp[] = getPixelArray(pix);
	System.out.println("Color value " + temp[0] + " " + temp[1] + " "+ temp[2] + " " + temp[3]);
    }

    /**************************************************************************************************
     * This is where you will put your methods. Every method below is called when the corresponding pulldown menu is
     * used. As long as you have a picture open first the when your fun1, fun2, fun....etc method is called you will
     * have a 2D array called picture that is holding each pixel from your picture.
     *************************************************************************************************/

    private void turn90left(){
	System.out.println("The height is: "+height);
	System.out.println("The width is: "+width);
	
	int newArray[][] = new int[width][height];
	for( int i=0; i<height; i++){
	    // System.out.println("This is i: " + i);
	    for( int j=1; j<width; j++){
		newArray[width-j][i] = picture[i][j];
	    }
	}
	int temp = width;
	width = height;
	height = temp;
	picture = newArray;
	resetPicture();
    }

    private void edgeDetect(){
	greyScale();
	int avg = 0, temp = 0;
	int rgbArray[] = new int[4];
	int tempicture[][] = new int[height][width];
	for(int i=0; i<height; i++){
	    tempicture[i] = Arrays.copyOf(picture[i], width);
	}
	for(int i=0; i<height; i++){
	    for(int j=0; j<width; j++){
		rgbArray = getPixelArray(tempicture[i][j]);
		avg = getSurroundingAvg(tempicture,i,j);
		temp = Math.min(255, 30*Math.abs(rgbArray[1]-avg));
		if(temp > thresh)
		    temp = 255;
		else
		    temp = 0;
		rgbArray[1] = temp;
		rgbArray[2] = temp;
		rgbArray[3] = temp;
		picture[i][j] = getPixels(rgbArray);
	    }
	}
	resetPicture();
    }

    private int getSurroundingAvg(int[][] picture, int si, int sj){
	int totSurr = 0;
	int counter = 0;
	int rgbArray[] = new int[4];
	for(int i = -EDM; i<=EDM; i++){
	    for(int j=-EDM; j<=EDM; j++){
		if(!(i==0 && j==0) && (si+i > 0 && si+i < height && sj+j > 0 && sj+j < width)){
		    rgbArray = getPixelArray(picture[si+i][sj+j]);
		    totSurr += rgbArray[1];
		    counter++;
		}
	    }
	}
	return totSurr/counter;

    }
    
    private void turn90right(){
	turn90left();
	turn90left();
	turn90left();
    }

    private void quit(){
	System.exit(0);
    }

    private void greyScale(){
	for(int i=0; i<height; i++){
	    for(int j=0; j<width; j++){
		int rgbArray[] = new int[4];
		//get three ints for R, G and B
		rgbArray = getPixelArray(picture[i][j]);
		int rgbOriginal[] = rgbArray;
		int temp = Math.round((float)((.21 * rgbArray[1]) + (.72 * rgbArray[2]) + (.07 * rgbArray[3])));
		rgbArray[1] = temp;
		rgbArray[2] = temp;
		rgbArray[3] = temp;
		//take three ints for R, G, B and put them back into a single int
		picture[i][j] = getPixels(rgbArray);	
	    }
	}
	resetPicture();
    }

    public void equalize(){
	int[] redValues = new int[256];
	int[] greenValues = new int[256];
	int[] blueValues = new int[256];
	int[] rgbA = new int[4];

	//This works correctly
	for(int i=0; i<width; i++){
	    for(int j=0; j<height; j++){
		rgbA = getPixelArray(picture[j][i]);
		redValues[rgbA[1]]++;
		greenValues[rgbA[2]]++;
		blueValues[rgbA[3]]++;
	    }
	}

	//This works correctly
	for(int i=0; i<255; i++){
	    if(i>0){
		redValues[i] = redValues[i]+redValues[i-1];
		greenValues[i] = greenValues[i]+greenValues[i-1];
		blueValues[i] = blueValues[i]+blueValues[i-1];
	    }
	}

	for(int i=0; i<height; i++){
	    for(int j=0; j<width; j++){
		rgbA = getPixelArray(picture[i][j]);
		if(rgbA[1]>0)
		    rgbA[1] = (int)(255.0*(double)redValues[rgbA[1]]/(double)redValues[254]);
		else
		    rgbA[1] = 0;
		if(rgbA[2]>0)
		    rgbA[2] = (int)(255.0*(double)greenValues[rgbA[2]]/(double)greenValues[254]);
		else
		    rgbA[2] = 0;
		if(rgbA[3]>0)
		    rgbA[3] = (int)(255.0*(double)blueValues[rgbA[3]]/(double)blueValues[254]);
		else
		    rgbA[3]=0;
		picture[i][j] = getPixels(rgbA);
	    }
	}
	resetPicture();
    }

    public void makeHistograms(){
	if(redH != null){
	    redH.dispatchEvent(new WindowEvent(redH, WindowEvent.WINDOW_CLOSING));
	    blueH.dispatchEvent(new WindowEvent(blueH, WindowEvent.WINDOW_CLOSING));
	    greenH.dispatchEvent(new WindowEvent(greenH, WindowEvent.WINDOW_CLOSING));
	}
	int[] redValues = new int[256];
	int[] greenValues = new int[256];
	int[] blueValues = new int[256];

	int[] rgbA = {0, 0, 0, 0};
	maxValue = Integer.MIN_VALUE;

	for(int i=0; i<width; i++){
	    for(int j=0; j<height; j++){
		rgbA = getPixelArray(picture[j][i]);
		redValues[rgbA[1]]++;
		greenValues[rgbA[2]]++;
		blueValues[rgbA[3]]++;
	    }
	}

	
	for(int i=0; i<255; i++){
	    if(redValues[i]>maxValue){
		maxValue = redValues[i];
	    }
	    if(greenValues[i]>maxValue){
		maxValue = greenValues[i];
	    }
	    if(blueValues[i]>maxValue){
		maxValue = blueValues[i];
	    }
	}


	
	redH = new Histogram("RED", redValues);
	greenH = new Histogram("GREEN", greenValues);
	blueH = new Histogram("BLUE", blueValues);
    }
    
    @Override
    public void mouseEntered(MouseEvent m){}
    @Override
    public void mouseExited(MouseEvent m){}
    @Override
    public void mouseClicked(MouseEvent m){
	colorX = m.getX();
	colorY = m.getY();
	System.out.println(colorX + "  " + colorY);
	getValue(colorX, colorY);
	start.setEnabled(true);
    }
    @Override
    public void mousePressed(MouseEvent m){}
    @Override
    public void mouseReleased(MouseEvent m){
	//Histogram h = new Histogram("CLICK", pixels);
    }

    public static void main(String [] args){
	IMP imp = new IMP();
    }

    public class Histogram extends JFrame{
	int[] values;
	public Histogram(String name, int[] values){
	    this.values = values.clone();
	    this.setTitle(name);
	    this.setVisible(true);
	    this.setSize(276, 500);
	    if(name.equals("RED"))
		this.setLocation(600, 0);
	    else if(name.equals("GREEN"))
		this.setLocation(876, 0);
	    else
		this.setLocation(876+276, 0);
	    this.repaint();
	    HPanel drawP = new HPanel();
	    this.add(drawP);
	    System.out.println(maxValue);

	}
	public class HPanel extends JPanel{

	    public HPanel(){
		repaint();
	    }

	    @Override
	    public void paintComponent(Graphics g){
		super.paintComponent(g);

		for(int i=0; i<values.length; i++){
		    //System.out.println(values[i]/maxValue);
		    g.drawLine(10+i, 450, 10+i, 450-(400*values[i]/maxValue));
		}
	    }
	}
    }
}
