import corobot.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The model for the Tour Guide Robot.
 *
 * Stores data concerning:
 * --different tour routes
 * --different voice actors
 * --current location on route
 * And also executes the audio functionality.
 *
 * A lot of the code is messy because the file
 * paths are different on Intellij (the IDE I
 * used to create it) and outside it. I will make
 * a second version //TODO that fixes this.
 *
 * @author Dr. Zack Butler
 * @author Paul Galatic
 *
 */
public class TGR_Model extends Observable implements Runnable{

    //debuginfo
    private final String WAIT_MSG = "Waiting... ";
    private final String CAPTION_IGNORE = "_";
    //number of audio clips to be given in between stops
    private final int MISCAUDIO = 8;
    //sleep time constants
    private final int SLEEP_MED = 1;

    //DEBUGGING
    //whether or not the program is being run on IntelliJ
    private boolean onIDE;
    //checks to see if audio can load at all
    private Media DEFAULT_MEDIA;
    //global writer (for debugprint)
    private PrintWriter wr;


    private boolean gettingOptions;
    private boolean gettingCredits;
    private boolean isTouring;
    private boolean isTravelling;
    private boolean isResetting;
    private boolean isFinished;
    private boolean autoTouring;
    private boolean hold;
    private boolean canPlayClip;
    private int stops = 0;
    private int audioStop = 0;
    //maximum number of stops (two less than this value)
    private int totalStops = 5;
    private int tourRoute;
    private int va;
    private String information;
    private String caption;

    //holds the sequence of stops on the current
    //tour route
    private HashMap<Integer, StopNode> stopMap;
    //holds the captions (data separated due to
    //bulk)
    private HashMap<String, String> captionMap;
    //holds audio to be given at any time
    private HashMap<Integer, AudioNode> audioMap;
    //plays audio
    private MediaPlayer player;
    //the collision detection
    private List<Point> collisionData;
    //the Tour Guide Robot
    private Robot TGR;
    private Future TGR_STATUS;

    /**Sets up the model with some initial
     * conditions, most notably determining
     * which paths to use to get to the essential
     * files.
     *
     * I've designed the resources folder
     * carefully, if not correctly; be aware that
     * any mistakes I made will be a pain to
     * undo. Apologies for that.*/
    public TGR_Model(boolean onIDE){

        this.onIDE = onIDE;

        hold = true;
        stops = 0;
        information = "";
        caption = "";
        audioMap = new HashMap<>();
        //sentinels
        va = -1;
        tourRoute = -1;
        if (onIDE) {
            buildCaptionMap("resources/routes/captions.txt");
        }else {
            buildCaptionMap("routes/captions.txt");
        }

        //for debugging, we write the logs to a text file
        try {
            wr = new PrintWriter("debug.txt", "UTF-8");
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException o){
            o.printStackTrace();
        }

        //making sure we have audio, and also initializing the player
        try {
            if (onIDE) {
                DEFAULT_MEDIA = new Media(Paths.get("resources/audio/null.mp3").toUri().toString());
            }else{
                DEFAULT_MEDIA = new Media(Paths.get("audio/null.mp3").toUri().toString());
            }
        }catch (MediaException m){
            File file = new File(".");
            System.err.println("System could not find audio files! Printing all visible files...");
            for (String filename : file.list()){
                System.out.println("FILE: " + filename);
            }
            System.exit(-1);
        }

        //checking to see if there's a robot
        if (onIDE) {
            try {
                TGR = new Robot(true);
            }catch (RuntimeException e){
                debugPrint("Start the virtual robot!\n");
                System.exit(-1);
            }
        }else{
            //TODO
            TGR = new Robot(true);
        }

    }

    /**Returns the robot!*/
    public Robot getTGR() { return TGR; }

    /**Returns the 'current activity' of the robot.*/
    public String getInformation(){
        return information;
    }

    /**Returns the caption of the current stop.*/
    public String getCaption(){return caption;}

    /**Returns a list of (x, y) pairs for use
     * in the GUI drawing a visual representation
     * of the collision detection.*/
    public List<Point> getCollisionData(){return collisionData;}

    /**Returns whether the robot can play a clip.*/
    public boolean canPlayClip(){ return canPlayClip; }

    /**Returns whether or not the user is in the
     * options menu.*/
    public boolean isGettingOptions(){return gettingOptions;}

    /**Returns whether or not the user is viewing
     * the credits.*/
    public boolean isGettingCredits(){return gettingCredits;}

    /**Returns whether or not the robot is
     * travelling between stops.*/
    public boolean isTravelling(){ return isTravelling; }

    /**Returns whether or not the robot has a
     * tour currently in progress.*/
    public boolean isTouring(){ return isTouring; }

    /**Returns whether or not all stops have been
     * visited.*/
    public boolean isFinished(){return isFinished;}

    /**Returns whether the robot is autotouring
     * or not (advancing to each stop without
     * waiting for user input).*/
    public boolean isAutoTouring(){return autoTouring;}

    /**Reset flag; signals the model to reset.*/
    public void setResetting(boolean b){ isResetting = b; }

    /**Flag that the user has chosen their tour
     * guide / route.*/
    public void setGettingOptions(boolean b){gettingOptions = b;}

    /**Flag that the user wants to view the
     * credits.*/
    public void setGettingCredits(boolean b){gettingCredits = b; setChanged(); notifyObservers();}

    /**Flag for robot to begin tour.*/
    public void setTouring(boolean b){ isTouring = b;}

    public void setAutoTouring(boolean b){autoTouring = b;}

    /**Flag for robot to /not/ move onto next
     * stop.*/
    public void setHold(boolean b){hold = b;}

    /**Lets the robot know it can play a voice clip.*/
    public void setCanPlayClip(boolean b){ canPlayClip = b; }

    /**Sets the voice actor.*/
    public void setVA(int i){ va = i; setChanged(); notifyObservers(); }

    /**Sets the tour route. TODO: OPTIONS*/
    public void setTourRoute(int i){ tourRoute = i; setChanged(); notifyObservers(); }

    /**Sets the 'current activity' of the robot.*/
    public void setInfo(String s){ information = s; }

    /**To be called at the end of the Application
     * thread in TGR_GUI.*/
    public void closeWriter(){ wr.close(); }

    /**Sets the caption. Since every waypoint has
     * a caption, but not all captions are
     * meaningful, edits out some. Additionally,
     * I was having some odd NullPointerException
     * issues with ICL3's caption in particular.
     * TODO?
     *
     * @param currStop : parses this to know
     * which caption to grab, and is why this is
     * a private setter*/
    private void setCaption(int currStop){

        if (stopMap != null){
            caption = stopMap.get(currStop).caption;
            if (caption == null || caption.equals(CAPTION_IGNORE)){
                caption = "";
            }
            if (caption == null){debugPrint("Stop " + Integer.toString(currStop) + " has a faulty caption.\n");}
        }

    }

    /**Returns the chosen voice actor.
     *
     * @param filename: whether or not to return
     *        the value as the end of a filename
     *        for usage in buildAudioMap() and
     *        buildAudioList()
     *
     * @return String, determined by the current
     *        va value*/
    public String getVA(boolean filename) throws FileNotFoundException{

        //voice actor IDs
        final String VA_1 = "Alice";
        final String VA_1_EXTENSION = "_alice.mp3";
        final String VA_2 = "Daisy";
        final String VA_2_EXTENSION = "_daisy.mp3";
        final String VA_3 = "George";
        final String VA_3_EXTENSION = "_george.mp3";
        final String VA_4 = "John";
        final String VA_4_EXTENSION = "_john.mp3";

        switch (va){
            case -1:
                throw new IndexOutOfBoundsException("Invalid Void Actor");
            case 1:
                if (filename){
                    return VA_1_EXTENSION;
                }
                return VA_1;
            case 2:
                if (filename){
                    return VA_2_EXTENSION;
                }
                return VA_2;
            case 3:
                if (filename){
                    return VA_3_EXTENSION;
                }
                return VA_3;
            case 4:
                if (filename){
                    return VA_4_EXTENSION;
                }
                return VA_4;
            default:
                throw new IndexOutOfBoundsException("Invalid Void Actor (value: " + va + ")");
        }

    }

    /**'Truly' initializes the model, after it's
     * made sure the user has made up their mind
     * one way or the other. This way the maps
     * aren't assembled before the user has
     * locked in.*/
    private void trueInit(){

        //tour route IDs
        final String ROUTE_0 = "routes/test_route.txt";
        final String ROUTE_0_DEBUG = "resources/routes/test_route.txt";
        final String ROUTE_1 = "routes/basic_route.txt";
        final String ROUTE_1_DEBUG = "resources/routes/basic_route.txt";
        final String ROUTE_2 = "routes/complete_route.txt";
        final String ROUTE_2_DEBUG = "resources/routes/complete_route.txt";
        final String ROUTE_3 = "routes/office_route.txt";
        final String ROUTE_3_DEBUG = "resources/routes/office_route.txt";

        switch (tourRoute){
            case 0:
                if (onIDE) {
                    buildStopMap(ROUTE_0_DEBUG);
                }else{
                    buildStopMap(ROUTE_0);
                }
                break;
            case 1:
                if (onIDE){
                    buildStopMap(ROUTE_1_DEBUG);
                }else{
                    buildStopMap(ROUTE_1);
                }
                break;
            case 2:
                if (onIDE){
                    buildStopMap(ROUTE_2_DEBUG);
                }else{
                    buildStopMap(ROUTE_2);
                }
                break;
            case 3:
                if (onIDE){
                    buildStopMap(ROUTE_3_DEBUG);
                }else{
                    buildStopMap(ROUTE_3);
                }
                break;
            default:
                throw new IllegalArgumentException("Bad Tour Route");
        }

        if (onIDE){
            buildAudioMap("resources/routes/audio.txt");
        }else{
            buildAudioMap("routes/audio.txt");
        }

        totalStops = stopMap.size();

        isTravelling = true;
        setAutoTouring(false);
        setTouring(true);
        setHold(false);

        playSound(stopMap.get(0).audioName);
        setInfo("Getting my bearings...");
        caption = "Are all the subjects present and accounted for? " +
                "Excellent, then let us begin. My name is Tour Guide " +
                "Robot. You may call me T-G-R if you prefer. I was " +
                "designed to give you lovely humanoids a tour of the " +
                "B. Thomas Golisano College of Communication and " +
                "Information Sciences. As soon as I get my bearings, " +
                "I will begin.";
        stops++;
    }

    /**Sets / resets the model to an initial
     * state (before options are chosen).*/
    public void optionsInit(){

        setGettingOptions(true);
        stops = 0;

        setResetting(false);

        setChanged();
        notifyObservers();

    }

    /**The main loop for the tour. First, it
     * waits while the user is inputting their
     * chosen tour route, voice actor, and other
     * options. After that is finished, it waits
     * until it has been flagged to proceed to
     * the next stop, or to begin a tour. Unless
     * it has been told to reset, it progresses
     * through each stop until there are none
     * left. At that point, it waits until it is
     * told to reset.*/
    @Override
    public void run() {

        int iterator = 0;

        while(true){

            /*First while loop—for while the
            * users are grabbing their options.*/
            while (isGettingOptions() || !isTouring()){
                if (iterator == 10){
                    debugPrint(WAIT_MSG + "(Getting Options)\n");
                    iterator = 0;
                }
                checkReset();
                iterator++;
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_MED);
                } catch (InterruptedException e) {}
            }

            trueInit();

            /*Second while loop—for when the
            * users are on the tour.*/
            while (stops < totalStops) {

                while ((hold && !isAutoTouring()) || !canPlayClip() || isGettingCredits()) {
                    if (iterator == 10){
                        debugPrint(WAIT_MSG + "(Waiting for Next Stop)\n");
                        iterator = 0;
                    }
                    iterator++;
                    updateCollisionData();
                    setChanged(); notifyObservers();
                    if (checkReset()){break;}
                    try {
                        TimeUnit.SECONDS.sleep(SLEEP_MED);
                    } catch (InterruptedException e) {}
                }

                if (checkReset()){break;}
                nextStop();

                stops++;

            }

            isFinished = true;
            setChanged(); notifyObservers();

            /*Finishes up the tour—only loops
            * while the robot is waiting for the
            * user to press Finish.*/
            if (!isResetting) {
                setHold(true);
                while ((hold && !isAutoTouring()) || !canPlayClip() || isGettingCredits()){
                    try{
                        TimeUnit.SECONDS.sleep(SLEEP_MED);
                    }catch (InterruptedException e){}
                }

                information = "All stops have been visited!";
                caption = "That is all for now, but I hope you enjoyed this " +
                        "tour of Golisano, and that you will find your " +
                        "increased understanding useful through the passing " +
                        "of time. After all, knowledge is power.";
                setChanged(); notifyObservers();
                try {
                    if (onIDE) {
                        playSound("resources/audio/va_" + va + "/ending" + getVA(true));
                    }else{
                        playSound("audio/va_" + va + "/ending" + getVA(true));
                    }
                    TGR.navigateToLocation(stopMap.get(0).waypointName).pause();
                }catch (FileNotFoundException e){
                    System.err.println("Could not find the finishing touches!");
                    e.printStackTrace();
                }catch (MapException e){
                    System.err.println("Corobot could not navigate back home!");
                    e.printStackTrace();
                }catch (CorobotException e){
                    System.err.println("Corobot could not find its way home!");
                    e.printStackTrace();
                }
            }

            /*Third while loop—for while the
            * program waits to be reset.*/
            while(!isResetting && isTouring) {
                if (iterator == 5){
                    debugPrint(WAIT_MSG + "(Waiting for Reset)\n");
                    iterator = 0;
                }
                iterator++;
                updateCollisionData();
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_MED);
                } catch (InterruptedException e) {}

                checkReset();
            }
        }

    }

    /**Instructs the robot to travel to the next
     * stop, updating the GUI along the way.*/
    private void nextStop(){

        boolean playedClip = false;

        try {

            //START TRAVELLING
            isTravelling = true;
            setInfo("Travelling...");
            notifyObservers();
            TGR_STATUS = TGR.navigateToLocation(stopMap.get(stops).waypointName);
            setChanged();
            notifyObservers();

            //WHILE TRAVELLING
            //TODO future object with factoids
            Random r = new Random();
            Boolean coinFlip = r.nextBoolean();
            while (!TGR_STATUS.is_fufilled()){
                updateCollisionData();

                int stopsLeft = totalStops - stops;
                if ((stopsLeft <= audioMap.size() || coinFlip) && !playedClip){
                    AudioNode thisNode = audioMap.remove(audioStop);
                    caption = thisNode.caption;
                    playSound(thisNode.audioName);
                    playedClip = true;
                    audioStop++;
                }

                try {
                    setChanged(); notifyObservers();
                    debugPrint("Not there yet! (Travelling)\n");
                    TimeUnit.SECONDS.sleep(SLEEP_MED);
                }catch (InterruptedException e){}
            }

            //WAIT UNTIL ARRIVED
            try    {TGR_STATUS.pause();
            }catch (CorobotException c){}
            //EXECUTE UPON ARRIVAL
            isTravelling = false;
            setHold(true);
            setInfo("LOC: " + stopMap.get(stops).printName);
            setCaption(stops);
            if (stopMap.get(stops).audioName.equals("intro_2")){ //special case
                caption = "Now that I know where I am, I will " +
                        "explain the tour. Depending on the " +
                        "selected route, I will travel to several " +
                        "waypoints, and give you relevant information " +
                        "about each, both using my voice, as well as " +
                        "information on screen. While I am touring, I " +
                        "will also provide information about Golisano " +
                        "in general, for example, the mission of the " +
                        "various departments centered here. I will " +
                        "return to this location once I am finished " +
                        "for the next tour. Please press the button " +
                        "\"Next Stop\" or enable Auto Tour to continue. " +
                        "If you wish to change any of your options, press " +
                        "Restart.\n";
            }
            setChanged(); notifyObservers();
            debugPrint(information + "\n");
            if (canPlayClip){
                playSound(stopMap.get(stops).audioName);
            }
        }catch (MapException n) {
            n.printStackTrace();
        }

    }

    /**Flag for robot to move to the next stop.*/
    public void moveOn(){
        hold = false;
    }

    /**Checks the resetting flag and executes
     * reset protocol if it's active.*/
    private boolean checkReset(){

        if (isResetting){
            stops = 0;
            isFinished = false;
            setTouring(false);
            setGettingOptions(false);
            setAutoTouring(false);
            if (!(player == null)){
                player.stop();
            }
            if (!(TGR == null) && !(stopMap == null)) { //if !(tour_hasn't_been_run_at_all_yet)
                try {
                    TGR.navigateToLocation(stopMap.get(0).waypointName);
                } catch (MapException e) {
                    e.printStackTrace();
                }
            }

            setChanged(); notifyObservers();

            return true;
        }

        return false;

    }

    /**Plays a given voice clip.
     *
     * If a clip is already playing, returns
     * false.*/
    private synchronized void playSound(String soundFile){

        setCanPlayClip(false);

        if (player == null){
            player = new MediaPlayer(DEFAULT_MEDIA);
        }

        Media sound = new Media(Paths.get(soundFile).toUri().toString());

        player = new MediaPlayer(sound);

        player.setOnEndOfMedia(new Runnable(){
            public void run(){
                setCanPlayClip(true);
            }
        });

        player.play();

    }

    /**Grabs the collision data from the robot
     * and uses the robot's helper function to
     * turn it into a list of points that are
     * then assigned to collisionData.*/
    private void updateCollisionData(){

        try {
            Future fut = TGR.getScan().pause();
            String[] dat = fut.get();
            if (dat == null){return;}
            collisionData = TGR.scanStringToList(dat);
        }catch (CorobotException c){}

    }

    /**Builds the map of stops for the robot to
     * navigate to, in order by integer, based on
     * the route chosen by the user.
     *
     * FILE FORMAT:
     * (Lower Display Name);(Audio File Name Prefix);(Regiestered Corobot Waypoint)
     * EXAMPLE:
     * Instructional Computing Lab 1;icl1;ICL1
     *
     * To be efficient, the variance in voice
     * actors are accounted for by an earlier
     * function, getVA().
     *
     * @param filename : text file location*/
    private void buildStopMap(String filename){

        int i = 0;
        String debugString = "audio/";
        String vaString = "va_" + Integer.toString(va) + "/";

        if (onIDE){
            debugString = "resources/audio/";
        }

        try {
            Scanner in = new Scanner(new File(filename));
            String nodeLine;
            stopMap = new HashMap<>();
            while(in.hasNextLine()){
                nodeLine = in.nextLine();
                String parts[] = nodeLine.split(";");
                if (parts.length == 3) {
                    //printName audioName waypointName caption
                    StopNode newNode = new StopNode();
                    newNode.printName = parts[0];
                    newNode.audioName = debugString + vaString + parts[1] + getVA(true);
                    newNode.waypointName = parts[2];
                    newNode.caption = captionMap.get(parts[2]);
                    stopMap.put(i, newNode);
                    i++;
                }
            }
        }catch (FileNotFoundException e){
            System.err.println("StopMap could not be built!");
            e.printStackTrace();
        }

        debugPrint("stopMap successfully built!\n");

    }

    /**Same as buildStopMap(), only a tad
     * simpler.
     *
     * @param filename : see buildStopMap()*/
    private void buildAudioMap(String filename){

        int i = 0;

        String debugString = "audio/";
        String vaString = "va_" + Integer.toString(va) + "/";

        if (onIDE){
            debugString = "resources/audio/";
        }

        try{
            Scanner in = new Scanner(new File(filename));
            audioMap = new HashMap<>();
            while (in.hasNextLine()){
                String parts[] = in.nextLine().split(";");
                if (parts.length == 2){
                    AudioNode newNode = new AudioNode();
                    newNode.audioName = debugString + vaString + parts[0] + getVA(true);
                    newNode.caption = parts[1];
                    audioMap.put(i, newNode);
                    i++;
                }
            }
        }catch (FileNotFoundException e){
            System.err.println("AudioList could not be built!");
            e.printStackTrace();
        }

    }

    /**Same as buildStopMap(), but a little more
     * complex. Ideally, every reachable waypoint
     * should have a caption, so that routes can
     * be easily designed, modified, and
     * customized.
     *
     * As of August 4th, 2016, the included
     *      captions.txt
     * file is updated to include every stop with
     * at least a hollow caption.
     *
     * @param filename : see buildStopMap()*/
    private void buildCaptionMap(String filename){

        try{
            Scanner in = new Scanner(new File(filename));
            captionMap = new HashMap<>();
            while (in.hasNextLine()){
                String parts[] = in.nextLine().split("&"); //do NOT use this character in a caption!
                if (parts.length == 2) {
                    captionMap.put(parts[0].toUpperCase(), parts[1]);
                }else if (parts.length > 2){
                    for (int i = 0; i < parts.length; i++) {
                        System.out.println(parts[i]);
                    }
                }
            }
        }catch (FileNotFoundException e){
            System.err.print("CaptionMap could not be built!");
            e.printStackTrace();
        }

    }

    /**Prints the message. For debugging
     * purposes in principle.*/
    public void debugPrint(String msg){
        System.out.print("DEBUG: " + msg);
        wr.print("DEBUG: " + msg);
    }

}

/**Basic class to hold quadruple:
 * 1. 'printed' name of waypoint
 * 2. waypoint name according to waypoints.csv
 * 3. filename of audio file
 * 4. caption string*/
class StopNode {
    String printName;
    String waypointName;
    String audioName;
    String caption;
}

/**Basic class to hold a tuple of filename and
 * caption string.*/
class AudioNode {
    String audioName;
    String caption;
}

//those nice people I met--their names are
//Barat and Anu (pheonetic spellling)