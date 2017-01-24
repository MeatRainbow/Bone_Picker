import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

@ScriptManifest(author = "LRDBLK", info = "Picks chicken bones", name = "Bone Picket", version = 1.1, logo = "")
public class BonePicker extends Script {

    ///////////////////////
    //Variables For Paint//
   ///////////////////////

    private final Color color1 = new Color(102, 102, 102);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(255, 0, 51);

    private final BasicStroke stroke1 = new BasicStroke(2);

    private final Font font1 = new Font("Arial", 0, 9);


    ////////////////
    //VARIABLES//
    ////////////

    private final Area FARM_AREA = new Area(3226, 3300, 3235, 3287);
    private final Area BANK_AREA = Banks.LUMBRIDGE_UPPER;
    private int bonesPickedUp;
    private int priceOfBones;
    private GrandExchange ge = new GrandExchange();
    private enum State{
        PICKUP, TRAVEL_TO_BANK, TRAVEL_TO_CHICKEN, BANK, WAIT
    }

    ////////////////////
    //REQUIRED METHODS//
    ///////////////////

    @Override
    public void onStart() {
        log("Let's get started!");
        getExperienceTracker().start(Skill.ATTACK);
        try {
            priceOfBones = ge.getSellingPrice(526);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onLoop() throws InterruptedException {

        switch (getState()){
            case PICKUP:
                GroundItem item = getGroundItems().closest(g ->
                        g.getName().equalsIgnoreCase("Bones") && FARM_AREA.contains(g));
                Item[] currentInv = getInventory().getItems();
                if (item != null && item.exists() && map.canReach(item)) {
                    if (pickup(item)){
                        Timing.waitCondition(() -> !inventoryTheSame(currentInv), 5000);
                        bonesPickedUp++;
                        log("Bone picked up! Current Bone count:  " + bonesPickedUp);
                    }else{
                        log("Failed to pickup bones :(((((");
                    }
                }
                break;

            case TRAVEL_TO_BANK:
                walking.webWalk(BANK_AREA);
                break;

            case TRAVEL_TO_CHICKEN:
                walking.webWalk(FARM_AREA);
                break;

            case BANK:
                depositBank();
                log("Deposited inventory");
                break;

            case WAIT:
                Timing.waitCondition(() -> !working(), 1500);
                break;


        }


        return random(50, 400);
    }

    @Override
    public void onExit() {

    }

    @Override
    public void onPaint(Graphics2D g) {

        g.setColor(color1);
        g.fillRoundRect(2, 2, 143, 84, 16, 16);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRoundRect(2, 2, 143, 84, 16, 16);
        g.setFont(font1);
        g.setColor(color3);
        g.drawString("Time Run: " + Timing.msToString(getExperienceTracker().getElapsed(Skill.ATTACK)), 7, 24);
        g.drawString("Bones Collected: " + bonesPickedUp, 8, 49);
        g.drawString("Profit: " + bonesPickedUp * priceOfBones, 7, 70);


        Point mP = getMouse().getPosition();

        //mouse X
        g.drawLine(mP.x - 5, mP.y + 5, mP.x + 5, mP.y - 5);
        g.drawLine(mP.x + 5, mP.y + 5, mP.x - 5, mP.y - 5);

        g.setColor(Color.white);

    }


    ///////////
    //Methods//
    //////////



    /*
        Gets the state of the script to figure out next action
     */
    private State getState() {

        if (!working()) {
            if (!getInventory().isFull()){
                if(FARM_AREA.contains(myPlayer())){
                    return State.PICKUP;
                }else{
                    return State.TRAVEL_TO_CHICKEN;
                }
            }else{
                if(BANK_AREA.contains(myPlayer())){
                    return State.BANK;
                }else{
                    return State.TRAVEL_TO_BANK;
                }
            }
        }else{
            return State.WAIT;
        }


    }

    /*
        returns true if player is animating or is moving
     */
    private boolean working(){
        return myPlayer().isMoving() || myPlayer().isAnimating();
    }


    /*
        picks up a ground item
        Returns true if successful
     */
    private boolean pickup(GroundItem item){
        return item.interact("Take");
    }

    /*
        compares the given inventory to the current inventory
        and returns true if they're the same
     */
    private boolean inventoryTheSame(Item[] x){

       Item[] currentInventory = getInventory().getItems();

       return Arrays.equals(x, currentInventory);


    }

    /*
        deposits inventory to bank
     */
    private void depositBank(){
        if (getBank().isOpen()){
            getBank().depositAll();
        }else{
            Timing.waitCondition(() -> bank.open(), 2000);
        }
    }



}