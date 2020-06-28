import java.util.ArrayList;
import javafx.animation.*;
import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.collections.ObservableList;
import javafx.scene.shape.*;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class RandomWalkPattern extends Application {
  @Override
  public void start(Stage primaryStage) {
	  RandomWalkAnimation pane = new RandomWalkAnimation();
	   BorderPane container = new BorderPane();
	   container.setCenter(pane);
	   Button btStart = new Button("Start");
	   container.setBottom(btStart);
	   BorderPane.setAlignment(btStart, Pos.BOTTOM_CENTER);
	   
	   btStart.setOnAction(e -> pane.startAnimation()) ;
	   
	   
	   Scene scene = new Scene(container, 16*30, 16*30 + 50);
	   primaryStage.setTitle("Exercise15_35");
	   primaryStage.setScene(scene);
	   primaryStage.show();
   }
  
  public static void main(String[] args) {
	  Application.launch(args);
  }
}

/**

 ** Make the animation 

*/

class RandomWalkAnimation extends MappingPane {
	private Animation animation;
	ArrayList<Double> list = new ArrayList<Double>();
	private int count;
	private ArrayList<Line> lines = new ArrayList<>();
	
	public void startAnimation() {
        count = 0;
	    getChildren().removeAll(lines);
	    lines.clear();
	    generateWalk();
		
		animation = new Timeline(new KeyFrame(Duration.millis(500), e-> walkOneStep()));
		animation.setCycleCount(list.size() - 1);
		animation.play();
		}
	
	private void walkOneStep() {
		if(count < list.size() - 3) {
			Line l = new Line(list.get(count), list.get(count + 1), list.get(count + 2), list.get(count + 3));
			count += 2;
			getChildren().add(l);
			lines.add(l);
		}
	}
	
	public void generateWalk() {
		GenerateWalkPattern walk = new GenerateWalkPattern();
		walk.generate();
		ArrayList<Integer> list1 =  walk.getPointList();
		list.clear();
		for(int i = 0; i < list1.size() ; i++) {
			double startX = pointPlot(list1.get(i))[0];
			double startY = pointPlot(list1.get(i))[1];
			list.add(startX);
			list.add(startY);
		}
	}
 }

/*
   ** Generate a Mapping Pane
   ** 16 x 16 Mapping Pane
*/
class MappingPane extends Pane {
	   private ArrayList<Rectangle> gridList = new ArrayList<>();
	   private Polyline p = new Polyline();
	   private double x = 0;
	   private double y = 0;
	   private double width = 30;
	   private double height = 30;
	   private double[][] map = new double[17][17];
	   
	public MappingPane() {
		createMap();
		paintMappingPane();
	}
	
	public void startWalk() {
		GenerateWalkPattern walk = new GenerateWalkPattern();
		walk.generate();
		ArrayList<Integer> list = walk.getPointList();
		ObservableList<Double> lineList = p.getPoints();
		lineList.clear();
		for(int i = 0; i < list.size() ; i++) {
			double startX = pointPlot(list.get(i))[0];
			double startY = pointPlot(list.get(i))[1];
			lineList.add(startX);
			lineList.add(startY);
		}
		
		getChildren().remove(p);
		getChildren().add(p);
	}
	
	public double getGridWidth() {
		return width;
	}
	
	public double getGridHeight() {
		return height;
	}
	
	public void setGridWidth(double w) {
		width = w;
	}
	
	public void setGridHeight(double h) {
		height = h;
	}
	
	private void createMap() {
		double k = 0;
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[i].length; j++)
				map[i][j] = k++;
		}
	}
	
	protected double[] pointPlot(double point) {
		double[] res = new double[2];
		// search for point
		for(int i = 0; i < map.length; i++) 
			for(int j = 0; j < map[i].length; j++)
				if(map[i][j] == point) {
					res[0] = i*width;
					res[1] = j*height;
				}
		
		return res;
	 }
	
	public Polyline getPolyline() {
		return p;
	}
	
	
	protected void paintMappingPane() {
		  for(int i = 0; i < 16; i++) {
			  x = 0;
			  for(int j = 0; j < 16; j++) {
			      gridList.add(new Rectangle(x, y, width, height));
			      x = (j + 1)*width;
			  }
			  
			  y = (i + 1) * height;
		  }
		  
		// change properties of rectangle
		  for(int i = 0; i < gridList.size(); i++) {
			  gridList.get(i).setFill(Color.WHITE);
			  gridList.get(i).setStroke(Color.LIGHTGRAY);
		  }
		  
		  getChildren().addAll(gridList);
	}
}

/**
 
   ** Generating a random walk pattern
   ** Including a Walk-Matrix

*/

class GenerateWalkPattern {
	private int[][] walkMatrix = new int[289][4];
	private int[] visited = new int[289];
	private ArrayList<Integer> pointList = new ArrayList<>();
	/**
	  Map left -> 0, right-> 1, up-> 2, down -> 3
	 */
	
	public void generate() {
		initialize();
		generateRandomWalk(walkMatrix.length / 2);
		
	}
	

	private void initialize() {

		// Up walk is not possible in top row
		for(int i = 0; i < 16; i++)
			walkMatrix[i][2] = -1;
		
		// Left walk is not possible in leftmost column
		 for(int i = 0; i < 289; i+= 17)
			 walkMatrix[i][0] = -1;
		 
		 // Right Walk is not possible in rightmost column
		  for(int i = 16; i < 289; i+=17) 
			  walkMatrix[i][1] = -1;
			  
		// down walk is not possible in the down most row
		  for(int i = 272; i < 289; i++) 
			  walkMatrix[i][3] = -1;
			   
	}
	
	private int generateRandomWalk(int startVertex) {
		visited[startVertex] = 1;
		pointList.add(startVertex);
		if(isBorderPoint(startVertex))
			return -1;
		else {
		   int direction = generateValidDirection(startVertex);
		
		
		   if(direction == -1) {
			  // Dead end is reached
			  return -1;
	     	}
		    else { 
		      visited[pointDecoder(startVertex, direction)] = 1;
		      walkMatrix[startVertex][direction] = 1;
		
		      return generateRandomWalk(pointDecoder(startVertex, direction));
		    }
	    }
	}
	
	private boolean isBorderPoint(int startVertex) {
		for(int i = 0; i < 4; i++) 
			if(walkMatrix[startVertex][i] == -1)
				return true;
		
		return false;
	}
	
	private int generateValidDirection(int startVertex) {
		ArrayList<Integer> canBeVisitedList = new ArrayList<>();
		for(int i = 0; i < 4; i++) 
			if(walkMatrix[startVertex][i] == 0 && !isVisited(pointDecoder(startVertex, i))) {
					canBeVisitedList.add(i);
			}
		
		
		if(canBeVisitedList.size() == 0)
			return -1;
		else {
			int i = (int)(Math.random() * canBeVisitedList.size());
			return canBeVisitedList.get(i);
		}
	}
	
	private boolean isVisited(int point) {
		return visited[point] == 1;
	}
	
	private int pointDecoder(int startVertex, int direction) {
		if(direction == 0)
			return startVertex - 1;
		else if(direction == 1)
			return startVertex + 1;
		else if(direction == 2)
			return startVertex -17;
		else if(direction == 3)
			return startVertex + 17;
		
		return -9999;
	}
	
	public ArrayList<Integer> getPointList() {
		return pointList;
	}
}



