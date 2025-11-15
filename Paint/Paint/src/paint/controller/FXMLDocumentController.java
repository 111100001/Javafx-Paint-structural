package paint.controller;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import paint.model.*;


public class FXMLDocumentController implements Initializable, DrawingEngine {
  
    /***FXML VARIABLES***/
    @FXML
    private Button DeleteBtn;

    @FXML
    private ComboBox<String> ShapeBox;

    @FXML
    private Button UndoBtn;

    @FXML
    private Button RedoBtn;

    @FXML
    private ColorPicker ColorBox;

    @FXML
    private Button SaveBtn;
    
    @FXML
    private Button MoveBtn;
    
    @FXML
    private Button RecolorBtn;
    
    @FXML
    private Button LoadBtn;
    
    @FXML
    private GridPane After;
    
    @FXML
    private Pane Before;
    
    @FXML
    private Pane PathPane;
    
    @FXML
    private TextField PathText;

    @FXML
    private Button StartBtn;
    
    @FXML
    private Button ResizeBtn;
    
    @FXML
    private Button ImportBtn;
    
    @FXML
    private Button PathBtn;
    
    @FXML
    private Canvas CanvasBox;
    
    @FXML
    private Button CopyBtn;
    
    @FXML
    private Button GroupBtn;

    @FXML
    private Button UngroupBtn;

    @FXML
    private Label Message;
    
    @FXML
    private ListView ShapeList;
    
    
    
    /***CLASS VARIABLES***/
    private Point2D start;
    private Point2D end;
    
    //SINGLETON DP
    private static ArrayList<Shape> shapeList = new ArrayList<>();
    
    private boolean move=false;
    private boolean copy=false;
    private boolean resize=false;
    private boolean save=false;
    private boolean load=false;
    private boolean importt =false;
    
    //MEMENTO DP
    private Stack<ArrayList<Shape>> primary = new Stack<>();
    private Stack<ArrayList<Shape>> secondary = new Stack<>();


    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        if(event.getSource() == StartBtn){
            Before.setVisible(false);
            After.setVisible(true);
        }
        
        Message.setText("");
        if(event.getSource()==DeleteBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
            int index = ShapeList.getSelectionModel().getSelectedIndex();
            removeShape(shapeList.get(index));
            }else{
                Message.setText("You need to pick a shape first to delete it.");
            }
        }
        
        if(event.getSource()==RecolorBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                int index = ShapeList.getSelectionModel().getSelectedIndex();
                shapeList.get(index).setFillColor(ColorBox.getValue());
                refresh(CanvasBox);
            }else{
                Message.setText("You need to pick a shape first to recolor it.");
            }
        }
        
        if(event.getSource()==MoveBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                move=true;
                Message.setText("Click on the new top-left position below to move the selected shape.");
            }else{
                Message.setText("You need to pick a shape first to move it.");
            }
        }
        
        if(event.getSource()==CopyBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                copy=true;
                Message.setText("Click on the new top-left position below to copy the selected shape.");
            }else{
                Message.setText("You need to pick a shape first to copy it.");
            }
        }
        
        if(event.getSource()==ResizeBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                resize=true;
                Message.setText("Click on the new right-button position below to resize the selected shape.");
            }else{
                Message.setText("You need to pick a shape first to copy it.");
            }
        }
        
        if(event.getSource()==UndoBtn){
            if(primary.empty()){Message.setText("We are back to zero point! .. Can Undo nothing more!");return;}
            undo();
        }
        
        if(event.getSource()==RedoBtn){
            if(secondary.empty()){Message.setText("There is no more history for me to get .. Go search history books.");return;}
            redo();
        }
        
        if(event.getSource()==SaveBtn){
            showPathPane();
            save=true;
        }
        
        if(event.getSource()==LoadBtn){
            showPathPane();
            load=true;
        }
        
        if(event.getSource()==ImportBtn){
            showPathPane();
            importt=true;
        }
        
        if(event.getSource()==PathBtn){
            if(PathText.getText().isEmpty()){PathText.setText("You need to set the path of the file.");return;}
            if(save){save=false;save(PathText.getText());}
            else if(load){load=false;load(PathText.getText());}
            else if(importt){importt=false;installPluginShape(PathText.getText());}
            hidePathPane();
        }

        if(event.getSource()==GroupBtn){
            handleGroup();
        }
        if(event.getSource()==UngroupBtn){
            handleUngroup();
        }
    }
    
    public void showPathPane(){
        Message.setVisible(false);
        PathPane.setVisible(true);
    }
    
    public void hidePathPane(){
        PathPane.setVisible(false);
        Message.setVisible(true);
    }
    
    public void startDrag(MouseEvent event){
        start = new Point2D(event.getX(),event.getY());
        Message.setText("");
    }
    public void endDrag(MouseEvent event) throws CloneNotSupportedException{
        end = new Point2D(event.getX(), event.getY());
        if(end.equals(start)){clickFunction();}else{dragFunction();}
    }
    
    public void clickFunction() throws CloneNotSupportedException{
        if(move){move=false;moveFunction();}
        else if(copy){copy=false;copyFunction();}
        else if(resize){resize=false;resizeFunction();}
    }
    
    public void moveFunction(){
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        shapeList.get(index).setTopLeft(start);
        refresh(CanvasBox);
    }
    
    public void copyFunction() throws CloneNotSupportedException{
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Shape temp = shapeList.get(index).cloneShape();
        if(temp.equals(null)){System.out.println("Error cloning failed!");}
        else{
            shapeList.add(temp);
            shapeList.get(shapeList.size()-1).setTopLeft(start);
            refresh(CanvasBox);
        }
    }
    
    public void resizeFunction(){
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Color c = shapeList.get(index).getFillColor();
        start = shapeList.get(index).getTopLeft();
        //Factory DP
        Shape temp = new ShapeFactory().createShape(shapeList.get(index).getClass().getSimpleName(),start,end,ColorBox.getValue());
        if(temp.getClass().getSimpleName().equals("Line")){Message.setText("Line doesn't support this command. Sorry :(");return;}
        shapeList.remove(index);
        temp.setFillColor(c);
        shapeList.add(index, temp);
        refresh(CanvasBox);
        
    }
    
    public void dragFunction(){
        String type = ShapeBox.getValue();
        Shape sh;
        //Factory DP
        try{sh = new ShapeFactory().createShape(type,start,end,ColorBox.getValue());}catch(Exception e)
        {Message.setText("Don't be in a hurry! Choose a shape first :'D");return;}
        addShape(sh);
        sh.draw(CanvasBox);
        
    }
    
    
    //Observer DP
    public ObservableList<String> getStringList(){
        ObservableList<String> l = FXCollections.observableArrayList();
        try{
            for(Shape s : shapeList){
                String temp = s.getClass().getSimpleName() + "  (" + (int) s.getTopLeft().getX() + "," + (int) s.getTopLeft().getY() + ")";
                l.add(temp);
            }
        }catch(Exception e){/* ignore for now */}
        return l;
    }
    
    public ArrayList<Shape> cloneList(ArrayList<Shape> l) throws CloneNotSupportedException{
        ArrayList<Shape> temp = new ArrayList<Shape>();
        for(int i=0;i<l.size();i++){
            temp.add(l.get(i).cloneShape());
        }
        return temp;
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList shapeList = FXCollections.observableArrayList();
        shapeList.add("Circle");shapeList.add("Ellipse");shapeList.add("Rectangle");shapeList.add("Square");shapeList.add("Triangle");shapeList.add("Line");
        ShapeBox.setItems(shapeList);
        
        ColorBox.setValue(Color.BLACK);
        // Refresh highlight on selection change
        if (ShapeList != null && CanvasBox != null) {
            ShapeList.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) -> {
                redraw(CanvasBox);
            });
        }
    }

    @Override
    public void refresh(Object canvas) {
        try {
            primary.push(cloneList(shapeList));
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        redraw((Canvas) canvas);
        ShapeList.setItems(getStringList());
    }
    
    public void redraw(Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 850, 370);
        try{
            int selectedIndex = ShapeList.getSelectionModel() != null ? ShapeList.getSelectionModel().getSelectedIndex() : -1;
            for(int i=0;i<shapeList.size();i++){
                if(i == selectedIndex){
                    paint.model.iShape deco = paint.model.decorator.ShapeDecorator.withSelection(shapeList.get(i));
                    deco.draw(canvas);
                } else {
                    shapeList.get(i).draw(canvas);
                }
            }
        }catch(Exception e){}
    }

    @Override
    public void addShape(Shape shape) {
        shapeList.add(shape);
        refresh(CanvasBox);
    }

    @Override
    public void removeShape(Shape shape) {
        shapeList.remove(shape);
        refresh(CanvasBox);
    }

    @Override
    public void updateShape(Shape oldShape, Shape newShape) {
        shapeList.remove(oldShape);
        shapeList.add(newShape);
        refresh(CanvasBox);
    }

    @Override
    public Shape[] getShapes() {
        return shapeList.toArray(new Shape[0]);
    }

    @Override
    public void undo() {
        if(secondary.size()<21){
            ArrayList<Shape> temp = primary.pop();
            secondary.push(temp);
            if(primary.empty()){shapeList = new ArrayList<>();}
            else{shapeList = primary.peek();}
            redraw(CanvasBox);
            ShapeList.setItems(getStringList());
        }else{Message.setText("Sorry, Cannot do more than 20 Undo's :'(");}
    }

    @Override
    public void redo() {
        ArrayList<Shape> temp = secondary.pop();
        primary.push(temp);
        shapeList = primary.peek();
        redraw(CanvasBox);
        ShapeList.setItems(getStringList());
    }

    @Override
    public void save(String path) {
        if(path.substring(path.length()-4).equals(".xml")){
            SaveToXML x = new SaveToXML(path,shapeList);
            if(x.checkSuccess()){Message.setText("File Saved Successfully");}
            else{Message.setText("Error happened while saving, please check the path and try again!");}
        }
        else if(path.substring(path.length()-5).equals(".json")){
            Message.setText("Sorry, Json is not supported :(");
        }
        else{Message.setText("Wrong file format .. save to either .xml or .json");}
  
    }

    @Override
    public void load(String path) {
        if(path.substring(path.length()-4).equals(".xml")){
            try {
                LoadFromXML l = new LoadFromXML(path);
                if(l.checkSuccess()){
                shapeList = l.getList();
                refresh(CanvasBox);
                Message.setText("File loaded successfully");
                }
                else{Message.setText("Error loading the file .. check the file path and try again!");}
            } catch (SAXException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else if(path.substring(path.length()-5).equals(".json")){
            Message.setText("Sorry, Json is not supported :(");
        }
        else{Message.setText("Wrong file format .. load from either .xml or .json");}
    }

    @Override
    public List<Class<? extends Shape>> getSupportedShapes() {
        return null;
    }

    @Override
    public void installPluginShape(String jarPath) {
        Message.setText("Not supported yet.");
    }

    private void handleGroup(){
        if(shapeList.size()<2){Message.setText("Need at least 2 shapes to group.");return;}
        int idx = ShapeList.getSelectionModel().getSelectedIndex();
        Shape a; Shape b;
        if(idx==-1){
            a = shapeList.get(shapeList.size()-1);
            b = shapeList.get(shapeList.size()-2);
        } else if(idx==shapeList.size()-1){
            a = shapeList.get(idx);
            b = shapeList.get(idx-1);
        } else {
            a = shapeList.get(idx);
            b = shapeList.get(idx+1);
        }
        if(a instanceof GroupShape || b instanceof GroupShape){Message.setText("Cannot nest groups in this demo.");return;}
        GroupShape g = new GroupShape(java.util.Arrays.asList(a,b));
        shapeList.remove(a); shapeList.remove(b);
        shapeList.add(g);
        Message.setText("Composite group created.");
        refresh(CanvasBox);
    }

    private void handleUngroup(){
        int idx = ShapeList.getSelectionModel().getSelectedIndex();
        if(idx==-1){Message.setText("Select a group to ungroup.");return;}
        Shape target = shapeList.get(idx);
        if(!(target instanceof GroupShape)){Message.setText("Selected item not a group.");return;}
        GroupShape g = (GroupShape) target;
        shapeList.remove(g);
        java.util.List<Shape> kids = g.getChildren();
        for(int i=0;i<kids.size();i++){ shapeList.add(idx+i, kids.get(i)); }
        Message.setText("Group split into children.");
        refresh(CanvasBox);
    }
}
