package com.example.NexFight;
import com.example.EthanApiPlugin.*;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.NPCPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.example.EthanApiPlugin.QuickPrayer.PROTECT_FROM_MAGIC;
import static com.example.EthanApiPlugin.QuickPrayer.PROTECT_FROM_MISSILES;

@PluginDescriptor(
        name = "Nex",
        description = "",
        enabledByDefault = false,
        tags = {"sal"}
)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class NexPlugin extends Plugin
{
    TileObject altar;
    WorldPoint altarLocation = new WorldPoint(2940,5203,0);
    WorldPoint bankPoint = new WorldPoint(2900,5200,0);
    WorldArea bankArea = new WorldArea(bankPoint,9,7);
    WorldPoint nexPoint;
    WorldArea nexArea;

    WorldArea nexBox1;
    WorldArea nexBox2;
    WorldArea nexBox3;
    WorldArea nexBox4;
    //WorldPoint shadowAttack = new WorldPoint()
    @Inject
    Client client;
    NPC nex;
    NPC fumus;
    NPC umbra;
    NPC cruor;
    NPC glacies;
    NPC bloodReaver;
    NPC currentTarget;
    NPC wrathNex;
    String currentTargetString;
    TileObject stalagmite;
    TileObject containIce;
    int timeout = 0;
    int eatTimeout = 0;
    int eatNumber = 5;
    boolean needsToAttack = true;
    List<WorldPoint> shadows = new ArrayList<WorldPoint>();
    List<WorldPoint> nexMeleeSpots = new ArrayList<WorldPoint>();
    List<WorldPoint> nexPoints = new ArrayList<WorldPoint>();
    List<WorldPoint> safePoints = new ArrayList<WorldPoint>();
    WorldPoint safeSpotAirplane;
    WorldArea nexContainThis;
    WorldArea nexContainThisSafe;
    WorldArea nexShadowDistance;
    WorldArea nexShadowDistanceSafe;
    WorldArea wrathBox;
    WorldArea wrathBoxSafe;
    WorldArea nexBloodSacrifice;
    WorldArea nexBloodSacraficeSafe;
    WorldPoint westSafe;
    WorldPoint northSafe;
    WorldPoint eastSafe;
    WorldPoint southSafe;
    List<WorldPoint> safeSpots;

    Optional<WorldPoint> safePoint;
    int shadowTimeout = 5;
    boolean shadowPhase = false;
    boolean attackingMinion = false;
    boolean containThis = false;
    boolean dodgeAirplane = false;
    int containThisTimer = 0;
    boolean forceTab = false;
    //8676, 1426 nex spawn


    @Override
    @SneakyThrows
    public void startUp()
    {
        timeout = 0;
        eatTimeout = 0;
        eatNumber = 5;
        shadowTimeout = 0;
        needsToAttack = true;
        shadowPhase = false;
        attackingMinion = false;
        currentTargetString = "";
        containThis = false;
        containThisTimer = 0;
        forceTab = false;
        dodgeAirplane = false;
    }

    @Override
    public void shutDown()
    {

    }

    @Subscribe
    public void onGameTick(GameTick e)  {
        /*
        TODO turn on quick prayers and prayer flick
        dodge air plane

         */
        List<Integer> nexInts = new ArrayList<Integer>();
        nexInts.add(11278);
        nexInts.add(11281);
        nexInts.add(11280);
        nex = NPCs.search().idInList(nexInts).first().orElse(null);
        wrathNex = NPCs.search().withId(11282).first().orElse(null);
        fumus = NPCs.search().withId(11283).first().orElse(null);
        umbra = NPCs.search().withId(11284).first().orElse(null);
        cruor = NPCs.search().withId(11285).first().orElse(null);
        glacies = NPCs.search().withId(11286).first().orElse(null);
        bloodReaver = NPCs.search().withId(11294).first().orElse(null);;
        //stalagmite = TileObjects.search().withId(42944).first().orElse(null);
        stalagmite = TileObjects.search().withId(42944).nearestToPlayer().orElse(null);
        containIce = TileObjects.search().withId(42943).first().orElse(null);
        altar = TileObjects.search().withId(42965).first().orElse(null);
        timeout--;
        eatTimeout--;

        if (forceTab)
        {
            client.runScript(915, 3);
            forceTab = false;
        }
        if (client.getWidget(5046276) == null)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB), "Setup");
            forceTab = true;
        }
        if (Inventory.getItemAmount(ItemID.SERPENTINE_HELM) == 0 && Inventory.getItemAmount(ItemID.ANCIENT_COIF) == 0)//refreshing inventory and equipment tabs
        {
            InventoryInteraction.useItemIndex(0, "Wear");
        }
        if (Inventory.getItemAmount(ItemID.ANCIENT_COIF) > 0)
        {
            InventoryInteraction.useItemIndex(0, "Wear");
        }
        if (nex != null)
        {
            if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1) {
                togglePrayer();
            }
            togglePrayer();
            nexMeleeSpots.removeAll(nexMeleeSpots);
            nexContainThis = new WorldArea(new WorldPoint(nex.getWorldLocation().getX()-1, nex.getWorldLocation().getY()-1,0), 5,5);
            nexContainThisSafe = new WorldArea(new WorldPoint(nex.getWorldLocation().getX()-2, nex.getWorldLocation().getY()-2,0), 7,7);
            nexShadowDistance = new WorldArea(new WorldPoint(nex.getWorldLocation().getX()-7, nex.getWorldLocation().getY()-7,0), 17,17);
            nexShadowDistanceSafe = new WorldArea(new WorldPoint(nex.getWorldLocation().getX()-8, nex.getWorldLocation().getY()-8,0), 19,19);
            nexMeleeSpots.add(new WorldPoint(nex.getWorldLocation().getX()+2, nex.getWorldLocation().getY()+3, 0));
            nexMeleeSpots.add(new WorldPoint(nex.getWorldLocation().getX()-1, nex.getWorldLocation().getY()+2, 0));
            nexMeleeSpots.add(new WorldPoint(nex.getWorldLocation().getX()-1, nex.getWorldLocation().getY(), 0));
            nexMeleeSpots.add(new WorldPoint(nex.getWorldLocation().getX()+1, nex.getWorldLocation().getY()-1, 0));
            nexMeleeSpots.add(new WorldPoint(nex.getWorldLocation().getX()+3, nex.getWorldLocation().getY(), 0));
        }
        int hitpoints = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        setTarget();
        if (altar == null)
        {
            System.out.println("altar is null");
        }
        if (altar != null && !altar.getWorldLocation().equals(altarLocation)) //checks to see if you're inside nex room.
        {
            createNexArea();
            if (nexArea.contains(client.getLocalPlayer().getWorldLocation()))
            {
                if (shadowTimeout > 0) //lower timeout every tick until shadows have dissappeared then remove from list.
                {
                    shadowTimeout--;
                }
                if (shadowTimeout == 4)
                {
                    //find first safe point within -2 +2 of character.
                    safePoint = nexPoints.stream().filter(x ->
                            (x.getX() <= client.getLocalPlayer().getWorldLocation().getX()+2 && x.getX() >= client.getLocalPlayer().getWorldLocation().getX()-2)
                                    && (x.getY() <= client.getLocalPlayer().getWorldLocation().getY()+2 && x.getY() >= client.getLocalPlayer().getWorldLocation().getY()-2)
                                    && !shadows.contains(x)).findFirst();
                    System.out.println("moving to safe location: " + safePoint.toString());
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(safePoint.get());
                    timeout = 3;
                    needsToAttack = true;
                }
                if (shadowTimeout <= 0 && shadows.stream().count() > 0) //remove shadows once they are no longer around.
                {
                    shadows.removeAll(shadows);
                }
                if (eatTimeout <= 0 && eatNumber < 5) //only eat if delay is finished and eatNumber tells what food it's time to eat
                {
                    eatActions();
                }
                if (client.getLocalPlayer().getInteracting() == null) //if not interacting with anyihtng attack nex
                {
                    needsToAttack = true;
                }
            }
        }
        if (Inventory.getItemAmount(ItemID.VIAL) > 0)
        {
            InventoryInteraction.useItem(ItemID.VIAL, "Drop");
            needsToAttack = true;
        }
        if (timeout <= 0)
        {
            if (altar != null && !altar.getWorldLocation().equals(altarLocation))
            {
                createNexArea();
                if (nexArea.contains(client.getLocalPlayer().getWorldLocation()))
                {
                    if (dodgeAirplane)
                    {
                        if (nex != null)
                        {
                            System.out.println("dodge airplane");
                            safePoints.removeAll(safePoints);
                            if (nex.getOrientation() == 0)//south
                            {
                                System.out.println("Dodge airplane south");
                                safePoints.add(eastSafe);
                                safePoints.add(westSafe);
                                safePoints.add(northSafe);
                                safeSpotAirplane = FindSafe.FindSafe(safePoints, client.getLocalPlayer().getWorldLocation());
                            }
                            if (nex.getOrientation() == 512)//west
                            {
                                System.out.println("Dodge airplane west");
                                safePoints.add(eastSafe);
                                safePoints.add(southSafe);
                                safePoints.add(northSafe);
                                safeSpotAirplane = FindSafe.FindSafe(safePoints, client.getLocalPlayer().getWorldLocation());
                            }
                            if (nex.getOrientation() == 1024)//north
                            {
                                System.out.println("Dodge airplane north");
                                safePoints.add(eastSafe);
                                safePoints.add(westSafe);
                                safePoints.add(southSafe);
                                safeSpotAirplane = FindSafe.FindSafe(safePoints, client.getLocalPlayer().getWorldLocation());
                            }
                            if (nex.getOrientation() == 1536) //east
                            {
                                System.out.println("Dodge airplane east");
                                safePoints.add(eastSafe);
                                safePoints.add(southSafe);
                                safePoints.add(northSafe);
                                safeSpotAirplane = FindSafe.FindSafe(safePoints, client.getLocalPlayer().getWorldLocation());
                            }
                            dodgeAirplane = false;
                        }
                    }
                    if (hitpoints < 55 && eatNumber >= 5)
                    {
                        System.out.println("Need to eat, set eat number.");
                        eatNumber = 0;
                    }
                    if (wrathNex != null)
                    {
                        wrathBox = new WorldArea(new WorldPoint(wrathNex.getWorldLocation().getX()-2, wrathNex.getWorldLocation().getY()-2, 0),7,7);
                        wrathBoxSafe = new WorldArea(new WorldPoint(wrathNex.getWorldLocation().getX()-3, wrathNex.getWorldLocation().getY()-3, 0),9,9);
                        System.out.println("dodge wrath");
                        if (wrathBox.contains(client.getLocalPlayer().getWorldLocation()))
                        {
                            safePoint = FindSafe.FindSafe(nexPoints, client.getLocalPlayer().getWorldLocation(), wrathBox, wrathBoxSafe, 6, 6);
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(safePoint.get());
                            System.out.println("moving to safe point: " + safePoint.toString());
                        }
                        timeout = 20;
                        return;
                    }
                    if (stalagmite != null && Inventory.getItemAmount(ItemID.DRAGON_MACE) > 0)
                    {
                        System.out.println("equip mace");
                        InventoryInteraction.useItem(ItemID.DRAGON_MACE, "Wield");
                        timeout = 1;
                        return;
                    }
                    if (stalagmite != null)
                    {
                        System.out.println("attack stalagmite");
                        TileObjectInteraction.interact(stalagmite, "Attack");
                        timeout = 3;
                        return;
                    }
                    if (Inventory.getItemAmount(ItemID.ARMADYL_CROSSBOW) > 0)
                    {
                        System.out.println("equip cbow");
                        InventoryInteraction.useItem(ItemID.ARMADYL_CROSSBOW, "Wield");
                        timeout = 1;
                        return;
                    }
                    if (bloodReaver != null && !currentTargetString.equals("cruor"))
                    {
                        currentTarget = bloodReaver;
                    }
                    if (shadowPhase && !attackingMinion && nexShadowDistance.contains(client.getLocalPlayer().getWorldLocation()))
                    {
                        safePoint = FindSafe.FindSafe(nexPoints, client.getLocalPlayer().getWorldLocation(), nexShadowDistance, nexShadowDistanceSafe, 12, 12 );
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(safePoint.get());
                        System.out.println("moving to safe point: " + safePoint.toString());
                        needsToAttack = true;
                        timeout = 3;
                        return;
                    }
                    if (!shadowPhase && !attackingMinion)
                    {
                        if (!client.getLocalPlayer().getWorldLocation().equals(nexMeleeSpots.get(0)))
                        {
                            if (!nexBox1.contains(nexMeleeSpots.get(0)) && !nexBox2.contains(nexMeleeSpots.get(0))
                                    && !nexBox3.contains(nexMeleeSpots.get(0)) && !nexBox4.contains(nexMeleeSpots.get(0)))
                            {
                                if (containIce == null)
                                {
                                    //TODO take comments out once no longer using mass worlds, nex moves too much on mass worlds for this.
                                    /*
                                    MousePackets.queueClickPacket();
                                    MovementPackets.queueMovement(nexMeleeSpots.get(0));
                                    timeout = 2;
                                    return;

                                     */

                                }
                            }
                        }
                    }
                    if (needsToAttack && currentTarget != null)
                    {
                        System.out.println("attack target");
                        MousePackets.queueClickPacket();
                        NPCPackets.queueNPCAction(currentTarget, "Attack");
                        timeout = 1;
                        needsToAttack = false;
                        return;
                    }
                    if (nex == null)
                    {
                        System.out.println("going to start position");
                        if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1)
                        {
                            togglePrayer();
                        }
                        WorldPoint middle = new WorldPoint(nexPoint.getX()+12, nexPoint.getY()+13,0);
                        if (!client.getLocalPlayer().getWorldLocation().equals(middle))
                        {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(middle);
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned e)
    {
        if (e.getGameObject().getId() == 42942)
        {
            shadows.add(WorldPoint.fromLocal(client,e.getGameObject().getLocalLocation()));
            shadowTimeout = 6;
        }
    }
    @Subscribe
    public void onChatMessage(ChatMessage e)
    {
        if (e.getType() == ChatMessageType.GAMEMESSAGE)
        {
            if (e.getMessage().contains("Fill my soul with smoke!"))
            {
                currentTargetString = "nex";
                shadowPhase = false;
                attackingMinion = false;
                if (nex != null)
                {
                    if (!EthanApiPlugin.isQuickPrayerActive(PROTECT_FROM_MAGIC))
                    {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(1, 5046276, -1, 12); //quickPrayer magic
                    }
                    System.out.println("Attack nex");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(nex, "Attack");
                }
                timeout = 3;
            }
            if (e.getMessage().contains("Fumus, don't fail me!"))
            {
                /*
                currentTargetString = "fumus";
                shadowPhase = false;
                attackingMinion = true;
                if (fumus != null)
                {
                    System.out.println("Attack fumus");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(fumus, "Attack");
                }
                timeout = 1;

                 */
            }
            if (e.getMessage().contains("Darken my shadow!"))
            {
                currentTargetString = "nex";
                shadowPhase = true;
                attackingMinion = false;
                if (nex != null)
                {
                    System.out.println("Attack nex");
                    if (!EthanApiPlugin.isQuickPrayerActive(PROTECT_FROM_MISSILES))
                    {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(1, 5046276, -1, 13); //quickPrayer range
                    }
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(nex, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("Umbra, don't fail me!"))
            {
                currentTargetString = "umbra";
                shadowPhase = true;
                attackingMinion = true;
                if (umbra != null)
                {
                    System.out.println("Attack umbra");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(umbra, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("Flood my lungs with blood!"))
            {
                currentTargetString = "nex";
                shadowPhase = false;
                attackingMinion = false;
                if (nex != null)
                {
                    if (!EthanApiPlugin.isQuickPrayerActive(PROTECT_FROM_MAGIC))
                    {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(1, 5046276, -1, 12); //quickPrayer magic
                    }
                    System.out.println("Attack nex");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(nex, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("Cruor, don't fail me!"))
            {
                currentTargetString = "cruor";
                shadowPhase = false;
                attackingMinion = true;
                if (cruor != null)
                {
                    System.out.println("Attack cruor");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(cruor, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("Infuse me with the power of ice!"))
            {
                currentTargetString = "nex";
                shadowPhase = false;
                attackingMinion = false;
                if (nex != null)
                {
                    System.out.println("Attack nex");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(nex, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("Glacies, don't fail me!"))
            {
                currentTargetString = "glacies";
                shadowPhase = false;
                attackingMinion = true;
                if (glacies != null)
                {
                    System.out.println("Attack glacies");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(glacies, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("NOW, THE POWER OF ZAROS!"))
            {
                currentTargetString = "nex";
                shadowPhase = false;
                attackingMinion = false;
                if (nex != null)
                {
                    System.out.println("Attack nex");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(nex, "Attack");
                }
                timeout = 1;
            }
            if (e.getMessage().contains("A siphon will solve this!"))
            {
                if (bloodReaver != null)
                {
                    System.out.println("Attack nex");
                    MousePackets.queueClickPacket();
                    NPCPackets.queueNPCAction(bloodReaver, "Attack");
                }
                if (!currentTargetString.equals("cruor"))
                {
                    needsToAttack = true;
                    timeout = 2;
                }
            }
            if (e.getMessage().contains("Die now, in a prison of ice!"))
            { }
            if (e.getMessage().contains("Contain this!"))
            {
                safePoint = FindSafe.FindSafe(nexPoints, client.getLocalPlayer().getWorldLocation(), nexContainThis, nexContainThisSafe, 6, 6 );
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(safePoint.get());
                System.out.println("moving to safe point: " + safePoint.toString());
                needsToAttack = true;
                timeout = 2;
                return;
            }
            if (e.getMessage().contains("Nex has marked you for a blood sacrifice! RUN!"))
            {
                System.out.println("Dodge blood sacrifice");
                nexBloodSacrifice = new WorldArea(new WorldPoint(nex.getWorldLocation().getX()-7, nex.getWorldLocation().getY()-7, 0), 17, 17);
                nexBloodSacraficeSafe = new WorldArea(new WorldPoint(nex.getWorldLocation().getX()-8, nex.getWorldLocation().getY()-8, 0), 19, 19);
                safePoint = FindSafe.FindSafe(nexPoints, client.getLocalPlayer().getWorldLocation(), nexBloodSacrifice, nexBloodSacraficeSafe, 10, 10);
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(safePoint.get());
                needsToAttack = true;
                timeout = 7;
                return;
            }
            if (e.getMessage().contains("There is..."))
            {
                dodgeAirplane = true;
                timeout = 2;
                return;
            }
        }
    }

    public void togglePrayer()
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
    }
    public void handleBrews()
    {
        System.out.println("in handle brews.");
        if (Inventory.getItemAmount(ItemID.SARADOMIN_BREW1) > 0)
        {
            System.out.println("drinking brew1");
            InventoryInteraction.useItem(ItemID.SARADOMIN_BREW1, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.SARADOMIN_BREW2) > 0)
        {
            System.out.println("drinking brew2");
            InventoryInteraction.useItem(ItemID.SARADOMIN_BREW2, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.SARADOMIN_BREW3) > 0)
        {
            System.out.println("drinking brew3");
            InventoryInteraction.useItem(ItemID.SARADOMIN_BREW3, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.SARADOMIN_BREW4) > 0)
        {
            System.out.println("drinking brew4");
            InventoryInteraction.useItem(ItemID.SARADOMIN_BREW4, "Drink");
        }
    }
    public void handleRanging()
    {
        if (Inventory.getItemAmount(ItemID.RANGING_POTION1) > 0)
        {
            InventoryInteraction.useItem(ItemID.RANGING_POTION1, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.RANGING_POTION2) > 0)
        {
            InventoryInteraction.useItem(ItemID.RANGING_POTION2, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.RANGING_POTION3) > 0)
        {
            InventoryInteraction.useItem(ItemID.RANGING_POTION3, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.RANGING_POTION4) > 0)
        {
            InventoryInteraction.useItem(ItemID.RANGING_POTION4, "Drink");
        }
    }
    public void handleRestores()
    {
        if (Inventory.getItemAmount(ItemID.SUPER_RESTORE1) > 0)
        {
            InventoryInteraction.useItem(ItemID.SUPER_RESTORE1, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.SUPER_RESTORE2) > 0)
        {
            InventoryInteraction.useItem(ItemID.SUPER_RESTORE2, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.SUPER_RESTORE3) > 0)
        {
            InventoryInteraction.useItem(ItemID.SUPER_RESTORE3, "Drink");
            return;
        }
        if (Inventory.getItemAmount(ItemID.SUPER_RESTORE4) > 0)
        {
            InventoryInteraction.useItem(ItemID.SUPER_RESTORE4, "Drink");
        }
    }
    public void setTarget()
    {
        if (currentTarget == null)
        {
            //System.out.println("setting target to nex b/c null");
            currentTarget = nex;
        }
        if (currentTargetString.equals("nex"))
        {
            //System.out.println("setting target to nex");
            currentTarget = nex;
        }
        if (currentTargetString.equals("fumus"))
        {
            //System.out.println("setting target to fumus");
            currentTarget = fumus;
        }
        if (currentTargetString.equals("umbra"))
        {
            //System.out.println("setting target to umbra");
            currentTarget = umbra;
        }
        if (currentTargetString.equals("cruor"))
        {
            //System.out.println("setting target to cruor");
            currentTarget = cruor;
        }
        if (currentTargetString.equals("glacies"))
        {
            //System.out.println("setting target to glacies");
            currentTarget = glacies;
        }
    }

    public void eatActions()
    {
        if (eatNumber <=2 )
        {
            handleBrews();
            eatTimeout = 3;
            eatNumber++;
            needsToAttack = true;
        }
        else if (eatNumber == 3)
        {
            handleRestores();
            eatTimeout = 3;
            eatNumber++;
            needsToAttack = true;
        }
        else
        {
            handleRanging();
            eatTimeout = 3;
            eatNumber++;
            needsToAttack = true;
        }
    }

    public void createNexArea()
    {
        //finds southwest corner of room basd off altar.
        nexPoint = new WorldPoint(altar.getWorldLocation().getX()-26, altar.getWorldLocation().getY()-11, 0);
        //create nex area.
        nexArea = new WorldArea(nexPoint, 22,22);
        //set black squares that you can't walk to below.
        nexBox1 = new WorldArea(new WorldPoint(nexPoint.getX()+2, nexPoint.getY()+2, 0), 8,8);
        nexBox2 = new WorldArea(new WorldPoint(nexPoint.getX()+2, nexPoint.getY()+13, 0), 8,8);
        nexBox3 = new WorldArea(new WorldPoint(nexPoint.getX()+13, nexPoint.getY()+2, 0), 8,8);
        nexBox4 = new WorldArea(new WorldPoint(nexPoint.getX()+13, nexPoint.getY()+13, 0), 8,8);

        westSafe = new WorldPoint(nexPoint.getX()+9, nexPoint.getY()+11, 0);
        northSafe = new WorldPoint(nexPoint.getX()+11, nexPoint.getY()+13, 0);
        eastSafe = new WorldPoint(nexPoint.getX()+13, nexPoint.getY()+11, 0);
        southSafe = new WorldPoint(nexPoint.getX()+11, nexPoint.getY()+9, 0);

        for (int i = 0; i <22; i++)
        {
            for (int j = 0; j < 22; j++)
            {
                WorldPoint temp = new WorldPoint(nexPoint.getX()+i, nexPoint.getY()+j,0);
                if (!nexBox1.contains(temp) && !nexBox2.contains(temp) && !nexBox3.contains(temp) && !nexBox4.contains(temp))
                {
                    //adds all walkable points in the nex room.
                    nexPoints.add(temp);
                }
            }
        }
    }
}
