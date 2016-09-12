import corobot.Point;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * The display and view for the Tour Guide Robot.
 *
 * Designed to run on the robot itself.
 *
 * @author: Zack Butler
 * @author: Paul Galatic
 */
public class TGR_GUI extends Application implements Observer {

    private final double scene_pref_width = 1366.0;
    private final double scene_pref_height = 768.0;
    private final double top_pref_height = 150;
    private final double bottom_pref_height = scene_pref_height - top_pref_height;
    private final double largebtn_pref_width = 500.0;
    private final double largebtn_pref_height = 350.0;
    private final double medbtn_pref_width = 350;
    private final double medbtn_pref_height = 220;
    private final double smallbtn_pref_width = 160.0;
    private final double smallbtn_pref_height = 80.0;
    private final double displaycol_pref_width = 420.0;
    private final double displaycol_pref_height = 500.0;
    private final double column_width = displaycol_pref_width / 5;
    private final double default_hgap = 20.0;
    private final double default_vgap = 20.0;
    private final double smallest_font = 24.0;
    private final double small_font = 32.0;
    private final double medium_font = 60.0;
    private final double large_font = 72.0;

    private final String color_bg = "-fx-background-color: #F8F7ED;";
    private final String color_orange = "-fx-background-color: #F36E21;";
    private final String BORDER = "-fx-border-color: black;";

    private final String NULL_IMG_LOC = "/images/null.png";
    private final String TIGER_IMG_LOC = "/images/tiger.png.gif";
    private final String PDG_IMG_LOC = "/images/pdg_img.png";
    private final String ZJB_IMG_LOC = "/images/zjb_img.png";
    private final String COROBOT_IMG_LOC = "/images/corobot_img.png";

    private final FlowPane NULL_IMG;
    private final FlowPane TIGER_IMG;
    private final FlowPane PDG_IMG;
    private final FlowPane ZJB_IMG;
    private final FlowPane COROBOT_IMG;

    private int sceneNumber = 0;
    private static boolean onIDE;
    double menuHeight = 0;

    private TGR_Model model;
    private StackPane sp;
    private String viewType;

    public TGR_GUI(){

        sp = new StackPane();

        NULL_IMG = new FlowPane(getImage(NULL_IMG_LOC, displaycol_pref_width, displaycol_pref_height));
        TIGER_IMG = new FlowPane(getImage(TIGER_IMG_LOC, top_pref_height * 1.5, top_pref_height));
        PDG_IMG = new FlowPane(getImage(PDG_IMG_LOC, displaycol_pref_width, displaycol_pref_height));
        ZJB_IMG = new FlowPane(getImage(ZJB_IMG_LOC, displaycol_pref_width, displaycol_pref_height));
        COROBOT_IMG = new FlowPane(getImage(COROBOT_IMG_LOC, displaycol_pref_width, displaycol_pref_height));

        viewType = "view_1";

    }

    /**Constructs an orange banner that goes
     * across the screen.*/
    private FlowPane getBanner(){

        FlowPane fp = new FlowPane();
        fp.setStyle(color_orange);
        fp.setMinHeight(default_hgap);
        fp.setMaxHeight(smallbtn_pref_height);
        fp.setAlignment(Pos.CENTER);

        return fp;

    }

    /**Constructs a decorative column.
     *
     * @param width : the desired width
     * @param height : the desired height*/
    private FlowPane getColumn(double width, double height){

        FlowPane fp = new FlowPane();
        fp.setStyle(color_orange);
        fp.setPrefSize(width, height);
        return fp;

    }

    /**Creates and returns an image based on an
     * the arguments.
     *
     * @param imageName : the filename of the image
     * @param width : the desired width
     * @param height : the desired height*/
    private ImageView getImage(String imageName, double width, double height){

        Image img = new Image(imageName, width, height, false, false);

        return new ImageView(img);

    }


    /**Creates a large start button in the center
     * of the screen.*/
    private FlowPane getStartButton(){

        Button b1 = new Button("Start Tour");
        b1.setPrefSize(largebtn_pref_width, largebtn_pref_height);
        b1.setOnAction( e -> {
            b1.setDisable(true);
            model.optionsInit();
        } );
        b1.setFont(Font.font("Bold", large_font));

        FlowPane fp = new FlowPane(b1);
        fp.setAlignment(Pos.CENTER);

        return fp;

    }

    /**Constructs the top of the screen, with:
     * --A Restart button
     * --A Next Stop button
     * --The title label*/
    private FlowPane getTop(){

        FlowPane fp = new FlowPane();

        Label l = new Label("Tour Guide Robot");

        FlowPane f = TIGER_IMG;
        f.setAlignment(Pos.CENTER_LEFT);

        //TODO tooltips

        if (model.isTouring()) {
            l.setFont(Font.font("Bold", medium_font));
            l.setAlignment(Pos.CENTER);

            Button btn1 = new Button("Restart");
            Button btn2 = new Button("Next Stop");
            Button btn3 = new Button("Autotour");
            Button btn4 = new Button("Credits");


            btn1.setPrefSize(smallbtn_pref_width, smallbtn_pref_height);
            btn1.setFont(Font.font("Bold", smallest_font));
            btn1.setOnAction( e -> {
                btn1.setDisable(true);
                btn2.setDisable(true);
                btn3.setDisable(true);
                btn4.setDisable(true);
                sceneNumber = 0;
                model.debugPrint("Switching flag: Reset\n");
                model.setResetting(true);
            } );
            if (model.isGettingCredits()){
                btn1.setVisible(false);
            }

            btn2.setPrefSize(smallbtn_pref_width, smallbtn_pref_height);
            if (model.isFinished()){btn2.setText("Finished!");}
            btn2.setFont(Font.font("Bold", smallest_font));
            btn2.setOnAction( e -> {
                model.debugPrint("Switching flag: NextStop\n");
                model.moveOn();
            } );
            btn2.setDisable(false);
            if ((model.isTravelling() || !model.canPlayClip()) && !model.isFinished()){
                btn2.setDisable(true);
            }
            if (model.isGettingCredits()){
                btn2.setVisible(false);
            }


            btn3.setPrefSize(smallbtn_pref_width, smallbtn_pref_height);
            btn3.setFont(Font.font("Bold", smallest_font));
            btn3.setDisable(false);
            if (model.isAutoTouring()){
                btn3.setText("Exit Auto");
            }else{
                btn3.setText("Autotour");
            }
            btn3.setOnAction(e -> {
                model.setAutoTouring(!model.isAutoTouring());
                btn3.setDisable(true);
                model.debugPrint("Switching flag: Autotour\n");
            });
            if (model.isGettingCredits()){
                btn3.setVisible(false);
            }

            btn4.setPrefSize(smallbtn_pref_width, smallbtn_pref_height);
            btn4.setFont(Font.font("Bold", smallest_font));
            if (model.isGettingCredits()){
                btn4.setText("Back");
            }else{
                btn4.setText("Credits");
            }
            btn4.setOnAction(e -> {
                model.setGettingCredits(!model.isGettingCredits());
                btn4.setDisable(true);
                model.debugPrint("Switching flag: Credits\n");
            });

            GridPane gp = new GridPane();
            gp.addRow(0, btn1, btn2);
            gp.addRow(1, btn3, btn4);
            gp.setHgap(default_hgap / 2);
            gp.setVgap(default_vgap / 2);

            HBox hb2 = new HBox(f, l, gp);
            hb2.setSpacing(default_hgap);
            hb2.setAlignment(Pos.CENTER);

            fp.getChildren().add(hb2);
        }else{
            l.setFont(Font.font("Bold", large_font));
            l.setAlignment(Pos.CENTER);

            HBox hb1 = new HBox(f, l);
            fp.getChildren().add(hb1);
        }

        fp.setHgap(default_hgap / 2);
        fp.setMinHeight(top_pref_height);
        fp.setStyle(color_bg);

        return fp;

    }

    /**Draws the obstacle detection based on the
     * data (a list of Corobot.Point objects)
     * recieved from the model.
     *
     * @param gc : where the detected collisions
     * are to be drawn (a gc is owned by a Canvas
     * object, contained below in a Pane)*/
    private void drawObstacleDectection(GraphicsContext gc){
        List<Point> collisionData = model.getCollisionData();
        if (collisionData == null){return;}
        final int SCALE_MULTIPLE = 120;
        final int DOT_SIZE = 2;
        final double X_OFFSET = displaycol_pref_width * 0.5;
        final double Y_OFFSET = displaycol_pref_height * 1.2;

        collisionData.forEach(p -> {
            double x = -p.getY() * SCALE_MULTIPLE;
            double y = -p.getX() * SCALE_MULTIPLE;

            gc.setFill(Color.BLACK);
            gc.fillOval(x + X_OFFSET, y + Y_OFFSET, DOT_SIZE, DOT_SIZE);
        });
    }

    /**Creates a display grid alligned toward the
     * bottom of the screen, with:
     * --A display slot for the live obstacle
     *   detection
     * --A display slot for audio captions
     * --A display slot for any relevant images
     *   (images were dropped late in production
     *   because the labs were under renovation).*/
    private FlowPane getDisplayGrid(){

        FlowPane fp = new FlowPane();

        GridPane gp = new GridPane();

        Pane p1 = new Pane();
        p1.setPrefSize(displaycol_pref_width, displaycol_pref_height);

        Canvas c = new Canvas();
        c.setWidth(displaycol_pref_width);
        c.setHeight(displaycol_pref_height);
        GraphicsContext gc = c.getGraphicsContext2D();
        drawObstacleDectection(gc);
        p1.getChildren().add(c);
        p1.setStyle(BORDER);

        Pane p2 = new Pane();
        p2.setPrefSize(displaycol_pref_width, displaycol_pref_height);
        Label caption = new Label(model.getCaption());
        caption.setFont(Font.font(small_font * 0.7));
        caption.setWrapText(true);
        caption.setTextAlignment(TextAlignment.JUSTIFY);
        caption.setPrefWidth(displaycol_pref_width);
        p2.getChildren().add(caption);

        Pane p3 = new Pane();
        p3.setPrefSize(displaycol_pref_width, displaycol_pref_height);
        FlowPane f2 = COROBOT_IMG;
        f2.setStyle(BORDER);
        p3.getChildren().add(f2);

        gp.addRow(0, p1, p2, p3);

        Label l1 = new Label("Live Obstacle Detection");
        l1.setFont(Font.font("Bold", small_font));

        Label l2 = new Label(model.getInformation());
        l2.setFont(Font.font("Bold", small_font));
        l2.setWrapText(true);
        l2.setAlignment(Pos.CENTER);
        l2.setTextAlignment(TextAlignment.CENTER);
        l2.setPrefWidth(displaycol_pref_width);

        Label l3 = new Label("This is me!");
        l3.setFont(Font.font("Bold", small_font));
        l3.setAlignment(Pos.CENTER_RIGHT);
        l3.setPrefWidth(displaycol_pref_width);

        gp.addRow(1, l1, l2, l3);

        gp.setStyle(color_bg);
        gp.setAlignment(Pos.BOTTOM_CENTER);
        gp.setHgap(default_hgap);
        gp.setVgap(default_vgap);

        fp.getChildren().add(gp);
        fp.setStyle(color_bg);

        return fp;

    }

    /**Constructs the options menu, for the user
     * to choose from a list of voice actors and
     * tour routes. Since there are three menus
     * (two "choose" and one "confirm") there are
     * three different styles of FlowPane this
     * function can return.*/
    private FlowPane getOptionsMenu(){

        FlowPane fp = new FlowPane();
        GridPane gp = new GridPane();

        if (sceneNumber == 0) {

            Label l = new Label("Choose Voice Actor");
            l.setFont(Font.font("Verdana", medium_font));
            gp.addRow(0, l);

            Button btn1 = new Button("Alice");
            btn1.setOnAction(e -> {
                model.setVA(1);
                sceneNumber = 1;
            });
            btn1.setTooltip(new Tooltip("Default voice."));
            btn1.getTooltip().install(btn1, btn1.getTooltip());
            btn1.setFont(Font.font(medium_font));
            btn1.setPrefSize(medbtn_pref_width, medbtn_pref_height);

            Button btn2 = new Button("Daisy");
            btn2.setOnAction(e -> model.setVA(2));
            btn2.setTooltip(new Tooltip("Alternative female voice."));
            btn2.getTooltip().install(btn2, btn2.getTooltip());
            btn2.setFont(Font.font(medium_font));
            btn2.setPrefSize(medbtn_pref_width, medbtn_pref_height);

            Button btn3 = new Button("George");
            btn3.setOnAction(e -> {
                model.setVA(3);
                sceneNumber = 1;
            });
            btn3.setTooltip(new Tooltip("Default male voice."));
            btn3.getTooltip().install(btn3, btn3.getTooltip());
            btn3.setFont(Font.font(medium_font));
            btn3.setPrefSize(medbtn_pref_width, medbtn_pref_height);

            Button btn4 = new Button("John");
            btn4.setOnAction(e -> {
                model.setVA(4);
                sceneNumber = 1;
            });
            btn4.setTooltip(new Tooltip("Alternate male voice—with an accent!"));
            btn4.getTooltip().install(btn4, btn4.getTooltip());
            btn4.setFont(Font.font(medium_font));
            btn4.setPrefSize(medbtn_pref_width, medbtn_pref_height);

            gp.addRow(1, btn1, btn2);
            gp.addRow(2, btn3, btn4);
            sceneNumber = 1;

            menuHeight = gp.getHeight();
        }else if (sceneNumber == 1){
            Label l = new Label("Choose Tour Route");
            l.setFont(Font.font(medium_font));
            gp.addRow(0, l);

            HBox hb = new HBox();

            Button btn1 = new Button("Basic Route");
            btn1.setOnAction(e -> {
                model.setTourRoute(1);
                sceneNumber = 2;
            });
            btn1.setFont(Font.font(medium_font));
            btn1.setWrapText(true);
            btn1.setPrefSize(medbtn_pref_width, medbtn_pref_height);
            btn1.setTooltip(new Tooltip("A basic tour route that hits the most major stops. This is the shortest route."));
            btn1.getTooltip().setWrapText(true);
            btn1.getTooltip().setMaxWidth(scene_pref_width);
            btn1.getTooltip().setFont(Font.font(small_font));

            Button btn2 = new Button("Complete Route");
            btn2.setOnAction(e -> {
                model.setTourRoute(2);
                sceneNumber = 2;
            });
            btn2.setFont(Font.font(medium_font));
            btn2.setWrapText(true);
            btn2.setPrefSize(medbtn_pref_width, medbtn_pref_height);
            btn2.setTooltip(new Tooltip("A complete tour route that visits every stop, so that you can get to know the facility and its professors."));
            btn2.getTooltip().setWrapText(true);
            btn2.getTooltip().setMaxWidth(scene_pref_width);
            btn2.getTooltip().setFont(Font.font(small_font));

            Button btn3 = new Button("Office Route");
            btn3.setOnAction(e -> {
                model.setTourRoute(3);
                sceneNumber = 2;
            });
            btn3.setFont(Font.font(medium_font));
            btn3.setWrapText(true);
            btn3.setPrefSize(medbtn_pref_width, medbtn_pref_height);
            btn3.setTooltip(new Tooltip("A tour route that visits every office so that you can get to know the professors of Golisano."));
            btn3.getTooltip().setWrapText(true);
            btn3.getTooltip().setMaxWidth(scene_pref_width);
            btn3.getTooltip().setFont(Font.font(small_font));

            hb.getChildren().addAll(btn1, btn2, btn3);
            hb.setSpacing(default_hgap);

            gp.addRow(1, hb);
        }else if (sceneNumber == 2){
            Label l = new Label("Confirm Options");
            l.setFont(Font.font(medium_font));
            gp.addRow(0, l);

            HBox hb = new HBox();

            Button btn1 = new Button("Confirm");

            Button btn2 = new Button("Back");

            btn1.setFont(Font.font(medium_font));
            btn1.setPrefSize(medbtn_pref_width, medbtn_pref_height);
            btn1.setOnAction(e -> {
                btn1.setDisable(true);
                btn2.setDisable(true);
                model.setGettingOptions(false);
                model.setTouring(true);
            });

            btn2.setOnAction(e -> {
                sceneNumber = 0;
                btn1.setDisable(true);
                btn2.setDisable(true);
                model.setResetting(true);
            });
            btn2.setFont(Font.font(medium_font));
            btn2.setPrefSize(medbtn_pref_width, medbtn_pref_height);

            hb.getChildren().addAll(btn1, btn2);
            hb.setSpacing(default_hgap);

            gp.addRow(1, hb);
        }else{
            throw new IllegalStateException("getOptionsMenu() being called when it shouldn't");
        }

        gp.setHgap(default_hgap);
        gp.setVgap(default_vgap);
        gp.setPrefWidth(scene_pref_width - 200);
        fp.getChildren().add(gp);
        fp.setAlignment(Pos.CENTER);
        fp.setStyle(color_bg);
        fp.setMinHeight(menuHeight);
        return fp;

    }

    /**Returns a brief credits to those that
     * contributed to the project in some way,
     * shape, or form. If you believe you have
     * contributed significantly, please add
     * your name, and perhaps an image of
     * yourself!*/
    private FlowPane getCredits(){

        FlowPane fp = new FlowPane();
        GridPane gp = new GridPane();

        FlowPane pdg_profile = PDG_IMG;
        pdg_profile.setPrefSize(displaycol_pref_width - 200, displaycol_pref_height - 200);
        pdg_profile.setStyle(BORDER);

        FlowPane zjb_profile = ZJB_IMG;
        zjb_profile.setPrefSize(displaycol_pref_width - 200, displaycol_pref_height - 200);
        zjb_profile.setStyle(BORDER);

        Label l1 = new Label("Funded by Rochester Institute of Technology");
        l1.setWrapText(true);
        l1.setTextAlignment(TextAlignment.CENTER);
        l1.setFont(Font.font(small_font));

        Label l2 = new Label("Images and information sourced from: cs.rit.edu");
        l2.setWrapText(true);
        l2.setTextAlignment(TextAlignment.CENTER);
        l2.setFont(Font.font(small_font));

        Label l3 = new Label("Voice Actors by TextAloud:\nfromtexttospeech.com");
        l3.setWrapText(true);
        l3.setTextAlignment(TextAlignment.CENTER);
        l3.setFont(Font.font(small_font));

        VBox credits = new VBox(l1, l2, l3);
        credits.setSpacing(default_vgap * 2);
        credits.setPrefSize(displaycol_pref_width, displaycol_pref_height);

        gp.addRow(0, pdg_profile, credits, zjb_profile);

        Label l4 = new Label("pdg6505@g.rit.edu");
        l4.setWrapText(true);
        l4.setTextAlignment(TextAlignment.CENTER);
        l4.setFont(Font.font(small_font));

        Label l5 = new Label("zjb@cs.rit.edu");
        l5.setWrapText(true);
        l5.setTextAlignment(TextAlignment.RIGHT);
        l5.setFont(Font.font(small_font));

        FlowPane f1 = new FlowPane(l4);
        f1.setPrefWidth(displaycol_pref_width);

        FlowPane emptypane = new FlowPane();

        FlowPane f2 = new FlowPane(l5);
        f2.setPrefWidth(displaycol_pref_width);

        gp.addRow(1, f1, emptypane, f2);
        gp.setHgap(default_hgap);
        gp.setVgap(default_vgap);

        fp.getChildren().add(gp);


        return fp;

    }


    /**Assembles the majority of the GUI, based
     * on what viewtype is chosen.
     *
     * While the 'center' of the BorderPane
     * displaying the majority of the GUI is
     * retrieved by getBanner(), the function of
     * this banner primarily is to separate the
     * top of the GUI, with the controls, from
     * the display / optionsmenu / credits below.*/
    private Pane assemble() {

        BorderPane bp = new BorderPane();
        bp.setStyle(color_bg);
        bp.setTop(getTop());
        bp.setCenter(getBanner());

        VBox vb = new VBox();

        if (!viewType.equals("view_1")) {

            if (viewType.equals("view_2")) {
                vb.getChildren().add(getDisplayGrid());
                vb.getChildren().add(getBanner());
            } else if (viewType.equals("view_3")) {
                vb.getChildren().add(getOptionsMenu());
                vb.getChildren().add(getBanner());
            } else if (viewType.equals("view_4")){
                vb.getChildren().add(getCredits());
                vb.getChildren().add(getBanner());
            }
            bp.setBottom(vb);

            HBox hb = new HBox();
            hb.getChildren().add(getColumn(column_width, scene_pref_height));
            hb.getChildren().add(bp);
            hb.getChildren().add(getColumn(column_width, scene_pref_height));
            hb.setAlignment(Pos.BOTTOM_CENTER);
            return hb;
        }

        return bp;

    }

    /**Initializes the stage.*/
    public void start(Stage stage){

        model = new TGR_Model(onIDE);
        model.addObserver(this);
        new Thread(model).start();

        sp.getChildren().add(0, assemble());
        sp.getChildren().add(1, getStartButton());

        sp.setPrefSize(scene_pref_width, scene_pref_height);

        Scene scene = new Scene(sp);

        stage.setTitle("Tour Guide Robot");
        stage.setScene(scene);
        if (!onIDE){stage.setFullScreen(true);}
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                model.closeWriter();
                model.getTGR().closeSocket();
                Platform.exit();
                System.exit(0);
            }
        });

    }

    /**Based on the flags updated by user input
     * or TGR progress, changes the ViewType that
     * the GUI will display.*/
    public void update(Observable obs, Object o){

        Platform.runLater(new Runnable() {
            public void run() {
                sp.getChildren().clear();
                if (model.isTouring() && !model.isGettingCredits()) {
                    viewType = "view_2"; //displayGrid
                    sp.getChildren().add(0, assemble());
                }else if (model.isGettingOptions()){
                    viewType = "view_3"; //optionsMenu
                    sp.getChildren().add(0, assemble());
                }else if (model.isGettingCredits()){
                    viewType = "view_4"; //credits
                    sp.getChildren().add(0, assemble());
                }else{
                    viewType = "view_1"; //startButton
                    sp.getChildren().add(0, assemble());
                    sp.getChildren().add(1, getStartButton());
                }
            }
        });
    }

    /**Java has a different file structure than
     * the program does once it's fully compiled
     * (see /out/production/TGR) so whether or
     * not the program is being run on an IDE is
     * very important.
     *
     * If you're experiencing difficulty with
     * files being found, be sure to try
     * switching the argument (True is designed
     * for being run on an IDE, False otherwise).*/
    public static void main(String[] args) {

        if (args.length == 0 || args.length > 1){
            System.out.println("USAGE: java TGR_GUI ide_or_robot[t/f]");
            System.exit(0);
        }else{
            switch (args[0].toLowerCase()){
                case "t":
                    onIDE = true;
                    break;
                case "true":
                    onIDE = true;
                    break;
                case "f":
                    onIDE = false;
                    break;
                case "false":
                    onIDE = false;
                    break;
                default:
                    System.err.println("Could not recognize argument.");
                    System.out.println("USAGE: java TGR_GUI ide_or_robot[t/f]");
                    System.exit(0);
            }

        }

        Application.launch();

    }


}