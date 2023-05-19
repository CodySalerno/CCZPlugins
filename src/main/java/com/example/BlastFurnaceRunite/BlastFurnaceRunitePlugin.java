package com.example.BlastFurnaceRunite;
import com.example.EthanApiPlugin.*;
import com.example.InteractionApi.*;
import com.example.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.example.WidgetInfoExtended;
import com.google.common.primitives.Shorts;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeSearched;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.swing.*;
import javax.swing.text.html.Option;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Runite BF",
        description = "",
        enabledByDefault = false,
        tags = {"Sal"}
)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class BlastFurnaceRunitePlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;
    @Inject
    Client client;
    int timeout = 0;
    WorldPoint nearBars = new WorldPoint(1941,4962,0);
    WorldPoint nearConveyor = new WorldPoint(1942, 4967,0);
    WorldPoint nearBank = new WorldPoint(1948, 4957,0);
    boolean collectBars = false;
    int coalBagAmount = 0;
    @Inject
    EthanApiPlugin api;
    @Inject
    WidgetPackets widgetPackets;
    @Inject
    MousePackets mousePackets;
    WorldPoint nearGe = new WorldPoint(3175, 3457,0);
    WorldPoint atGe = new WorldPoint(3165, 3482,0);
    WorldPoint afterDoor2 = new WorldPoint(2929, 10191,0);
    boolean needSupplies = false;
    int coinAmount;
    boolean needCoinAmount = false;
    boolean needToTele = false;
    @Inject
    private BlastFurnaceRuniteOverlay overlay;


    @Override
    @SneakyThrows
    public void startUp()
    {
        timeout = 0;
        coalBagAmount = 0;
        needSupplies = false;
        if (Inventory.getItemAmount(ItemID.COAL_BAG_12019) != 0)
        {
            System.out.println("Checking coal bag");
            InventoryInteraction.useItem(ItemID.COAL_BAG_12019, "Check");
        }
        coinAmount = 0;
        needCoinAmount = false;
        needToTele = false;
        overlay.startTime = System.currentTimeMillis();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlay.bars = 0;
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        int coalInFurnace = client.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        int runiteBars = client.getVarbitValue(Varbits.BLAST_FURNACE_RUNITE_BAR);
        Widget coalBagWidget = client.getWidget(12648450);
        Widget barWidget = client.getWidget(17694734);
        Widget toggleRun = client.getWidget(10485787);
        timeout--;
        if (coalBagWidget != null) //if coalbag text appears update number remaining in coalbag.
        {
            coalBagAmount = CoalBag.updateAmount(coalBagWidget.getText());
        }
        if (client.getVarpValue(173) == 0)
        {
            mousePackets.queueClickPacket();
            widgetPackets.queueWidgetAction(toggleRun, "Toggle Run");
        }
        Optional<TileObject> bankChest = TileObjects.search().withId(26707).nearestToPlayer();
        Optional<TileObject> geBank = TileObjects.search().withId(10060).nearestToPlayer();
        Optional<TileObject> conveyor = TileObjects.search().withId(9100).nearestToPlayer();
        Optional<TileObject> barDispenser = TileObjects.search().withId(9092).nearestToPlayer();
        Optional<TileObject> stairs = TileObjects.search().withId(9084).nearestToPlayer();
        Optional<TileObject> door1 = TileObjects.search().withId(6977).nearestToPlayer();
        Optional<TileObject> door2 = TileObjects.search().withId(6102).nearestToPlayer().filter(x -> x.getWorldLocation().getX() == 2929);
        Optional<TileObject> door3 = TileObjects.search().withId(6975).nearestToPlayer();
        Optional<NPC> romeo = NPCs.search().withId(5037).nearestToPlayer();
        Optional<NPC> ge = NPCs.search().withId(2149).nearestToPlayer();
        if (timeout <= 0)
        {
            System.out.println("Coal: " + coalBagAmount);
            if ((romeo.isPresent() || ge.isPresent()) && !needToTele)
            {
                needSupplies = true;
            }
            //System.out.println("timeout is 0");
            if (needSupplies)
            {
                //System.out.println("need supplies");
                if (client.getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER) == null)
                {
                    if (needCoinAmount)
                    {
                        System.out.println("getting coin amount");
                        Optional<Widget> coin = Inventory.search().withId(995).first();
                        List<Widget> items = Inventory.search().result();
                        System.out.println(coin.isPresent());
                        coinAmount = coin.get().getItemQuantity();
                        System.out.println(coinAmount);
                        needCoinAmount = false;
                    }
                    if (bankChest.isPresent())
                    {
                        System.out.println("Teleport to varrock");
                        InventoryInteraction.useItem(ItemID.VARROCK_TELEPORT, "Break");
                        timeout = 10;
                    }
                    if (ge.isPresent())
                    {
                        System.out.println("opening GE");
                        NPCInteraction.interact(ge.get(), "Exchange");
                        timeout = 5;
                        return;
                    }
                    if (romeo.isPresent())
                    {
                        System.out.println("Going close to ge");
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(nearGe);
                        timeout = 30;
                        return;
                    }
                    if (nearGe.getX() == client.getLocalPlayer().getWorldLocation().getX() && nearGe.getY() == client.getLocalPlayer().getWorldLocation().getY())
                    {
                        System.out.println("Going to ge");
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(atGe);
                        timeout = 15;
                        return;
                    }
                }
                else
                {
                    Widget confirm = client.getWidget(465,29);
                    Widget lower = client.getWidget(465,25);
                    if (confirm != null && !confirm.isHidden() && !Widgets.search().hiddenState(false).withTextContains("Sell offer").empty())
                    {
                        System.out.println("cofirming offer");
                        timeout = 1;
                        for (int i = 0; i < 50; i++)
                        {
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474265, -1, 8);
                        }
                        mousePackets.queueClickPacket();
                        widgetPackets.queueWidgetAction(confirm, "Confirm");
                        timeout = 10;
                        return;
                    }
                     List<Widget> runiteBar =
                            Arrays.stream(client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER).getDynamicChildren()).filter(Objects::nonNull).filter(x -> x.getItemId() == 2364).collect(Collectors.toList());
                    if (runiteBar.stream().count() > 0)
                    {
                        System.out.println("Selling runite bars");
                        mousePackets.queueClickPacket();
                        widgetPackets.queueWidgetAction(runiteBar.get(0), "Offer");
                        timeout = 10;
                        return;
                    }
                    if (!Widgets.search().hiddenState(false).withText("Runite bar").empty())
                    {
                        if (!Widgets.search().hiddenState(false).withText("Collect").empty())
                        {
                            System.out.println("Collecting runite bars");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474246, -1, 0);
                            timeout = 10;
                            return;
                        }
                        else
                        {
                            timeout = 30;
                            return;
                        }
                    }
                    List<Widget> runiteOre =
                            Arrays.stream(client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER).getDynamicChildren()).filter(Objects::nonNull).filter(x -> x.getItemId() == 452).collect(Collectors.toList());
                    if (runiteOre.stream().count() == 0 && Widgets.search().hiddenState(false).withText("Runite ore").empty() && Widgets.search().hiddenState(false).withTextContains("Choose an item").empty())
                    {
                        System.out.println("Place buy offer");
                        mousePackets.queueClickPacket();
                        widgetPackets.queueWidgetActionPacket(1, 30474247, -1, 3);
                        timeout = 10;
                        return;
                    }
                    if (!Widgets.search().hiddenState(false).withTextContains("What would you like to buy").empty())
                    {
                        if (Widgets.search().hiddenState(false).withTextContains("runite ore").empty())
                        {

                            client.runScript(754, 451, 84);
                            timeout = 10;
                            return;
                        }
                    }
                    if (!Widgets.search().hiddenState(false).withText("1").empty())
                    {
                        System.out.println("adding values");
                        if (coinAmount == 0)
                        {
                            mousePackets.queueClickPacket();
                            MovementPackets.queueMovement(atGe);
                            timeout = 3;
                            needCoinAmount = true;
                            System.out.println("Closing ge");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474242, -1, 11);
                            return;
                        }
                        int runiteAmount = (coinAmount/11500);
                        runiteAmount = runiteAmount - (runiteAmount%27);
                        int thousands = runiteAmount/1000;
                        int hundreds = (runiteAmount % 1000)/100;
                        int tens = ((runiteAmount % 1000) %100) /10;
                        int ones = (((runiteAmount % 1000) %100) %10);
                        System.out.println(coinAmount);
                        System.out.println (thousands + " " + hundreds + " " + tens  );
                        for (int i = 0; i < thousands; i++)
                        {
                            System.out.println("adding thousands");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474265, -1, 6);
                        }
                        for (int i = 0; i < hundreds; i++)
                        {
                            System.out.println("adding hundreds");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474265, -1, 5);
                        }
                        for (int i = 0; i < tens; i++)
                        {
                            System.out.println("adding tens");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474265, -1, 4);
                        }
                        for (int i = 0; i < ones; i++)
                        {
                            System.out.println("adding ones");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474265, -1, 3);
                        }
                        timeout = 10;
                        return;
                    }
                    if (confirm != null && !confirm.isHidden() && !Widgets.search().hiddenState(false).withTextContains("Buy offer").empty())
                    {
                        System.out.println("cofirming offer");
                        timeout = 1;
                        for (int i = 0; i < 50; i++)
                        {
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474265, -1, 9);
                        }
                        mousePackets.queueClickPacket();
                        widgetPackets.queueWidgetAction(confirm, "Confirm");
                        timeout = 10;
                        return;
                    }
                    if (!Widgets.search().hiddenState(false).withText("Runite ore").empty())
                    {
                        if (!Widgets.search().hiddenState(false).withText("Collect").empty())
                        {
                            System.out.println("Collecting runite ore");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 30474246, -1, 0);
                            timeout = 10;
                            return;
                        }
                        else
                        {
                            timeout = 10;
                            return;
                        }
                    }
                    List<Widget> runiteOreGe =
                            Arrays.stream(client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER).getDynamicChildren()).filter(Objects::nonNull).filter(x -> x.getItemId() == 452).collect(Collectors.toList());
                    if (runiteOreGe.stream().count() > 0)
                    {
                        if (geBank.isPresent())
                        {
                            System.out.println("bank is present.");
                            if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null)
                            {
                                System.out.println("clicking bank.");
                                TileObjectInteraction.interact(geBank.get(), "Bank");
                                timeout  = 3;
                                needSupplies = false;
                                needToTele = true;
                                return;
                            }
                        }
                    }

                }
                return;
            }
            if (geBank.isPresent())
            {
                if (client.getWidget(WidgetInfo.BANK_CONTAINER) != null && (!BankInventory.search().withId(995).empty()))
                {
                    BankInventoryInteraction.useItem(995, "Deposit-all");
                    BankInventoryInteraction.useItem(452, "Deposit-all");
                    BankInventoryInteraction.useItem(451, "Deposit-all");
                    timeout = 2;
                    return;
                }
                if (client.getWidget(WidgetInfo.BANK_CONTAINER) != null)
                {
                    client.runScript(131, 13);
                    timeout = 5;
                    return;
                }
                if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null)
                {
                    System.out.println("Teleporting to blast furnace");
                    client.runScript(124, 2);
                    mousePackets.queueClickPacket();
                    widgetPackets.queueWidgetActionPacket(1, 4980766, -1, 2);
                    needToTele = false;
                    timeout = 30;
                    return;
                }
            }
            if (stairs.isPresent())
            {
                if (client.getLocalPlayer().getWorldLocation().getY() <= 10185)
                {
                    System.out.println("opening door1");
                    TileObjectInteraction.interact(door1.get(), "Open");
                    timeout = 8;
                    return;
                }
                if (door2.isPresent() && client.getLocalPlayer().getWorldLocation().getY() <= 10189)
                {
                    System.out.println(door2.get().getWorldLocation());
                    System.out.println("opening door2");
                    TileObjectInteraction.interact(door2.get(), "Open");
                    timeout = 8;
                    return;
                }
                if (door2.isEmpty() && client.getLocalPlayer().getWorldLocation().getY() <= 10189)
                {
                    System.out.println("walking past door2");
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(afterDoor2);
                    timeout = 8;
                    return;
                }
                if (client.getLocalPlayer().getWorldLocation().getY() <= 10194)
                {
                    System.out.println("opening door3");
                    TileObjectInteraction.interact(door3.get(), "Open");
                    timeout = 8;
                    return;
                }
                if (client.getLocalPlayer().getWorldLocation().getY() > 10194)
                {
                    System.out.println("climbind down");
                    TileObjectInteraction.interact(stairs.get(), "Climb-down");
                    timeout = 8;
                    return;
                }
            }
            if (bankChest.isPresent())
            {
                //System.out.println("bank is present.");
                if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null)
                {
                    //System.out.println("bank is closed.");
                    //Bank isn't Open
                    if (barWidget != null)
                    {
                        System.out.println("Clicking widget2");
                        overlay.bars += 27;
                        mousePackets.queueClickPacket();
                        widgetPackets.queueResumePause(17694734, 27);
                        timeout = 1;
                        return;
                    }
                    if ((Inventory.getEmptySlots() == 27 || Inventory.getItemAmount(ItemID.RUNITE_BAR) > 0) && coalBagAmount == 0 && (runiteBars == 0 || Inventory.getEmptySlots() == 0))
                    {
                        System.out.println("clicking bank.");
                        //Need to go to bank.
                        TileObjectInteraction.interact(bankChest.get(), "Use");
                        timeout  = 10;
                    }
                    if ((Inventory.getEmptySlots() == 27 && Inventory.getItemAmount(ItemID.RUNITE_BAR) == 0) && coalBagAmount == 0 && runiteBars > 0)
                    {
                        System.out.println("clicking bars.");
                        //Need to go to bank.
                        TileObjectInteraction.interact(barDispenser.get(), "Take");
                        timeout  = 5;
                        return;
                    }
                    if (Inventory.getItemAmount(ItemID.RUNITE_ORE) > 0)
                    {
                        System.out.println("clicking conveyor.");
                        //Need to go to conveyor.
                        TileObjectInteraction.interact(conveyor.get(), "Put-ore-on");
                        timeout  = 11;
                        collectBars = true;
                    }
                    if (coalBagAmount > 0 && Inventory.getEmptySlots() == 27)
                    {
                        System.out.println("removing items from coal bag.");
                        InventoryInteraction.useItem(ItemID.COAL_BAG_12019, "Empty");
                        timeout = 1;
                    }
                    if (Inventory.getItemAmount(ItemID.COAL) > 0)
                    {
                        System.out.println("putting coal on conveyor");
                        TileObjectInteraction.interact(conveyor.get(), "Put-ore-on");
                        timeout = 1;
                    }
                }
                else
                {
                    if (BankInventory.search().withAction("Fill").empty())
                    {
                        coalBagAmount = 27;
                    }
                    if (Inventory.getItemAmount(ItemID.SUPER_ENERGY1) > 0)
                    {
                        BankInventoryInteraction.useItem(ItemID.SUPER_ENERGY1, "Drink");
                        timeout = 1;
                        return;
                    }
                    //System.out.println("Bank is open");
                    if (Inventory.getItemAmount(ItemID.RUNITE_BAR) > 0)
                    {
                        BankInventoryInteraction.useItem(ItemID.RUNITE_BAR, "Deposit-All");
                        timeout = 1;
                        return;
                    }
                    if (Bank.search().withId(ItemID.RUNITE_ORE).first().isEmpty())
                    {
                        if (client.getVarbitValue(3958) == 0)
                        {
                            System.out.println("setting notes");
                            mousePackets.queueClickPacket();
                            widgetPackets.queueWidgetActionPacket(1, 786456, -1, -1); //set notes
                            return;
                        }
                        needSupplies = true;
                        if (Inventory.getItemAmount(ItemID.RUNITE_BAR) > 0)
                        {
                            BankInventoryInteraction.useItem(ItemID.RUNITE_BAR, "Deposit-all");
                        }
                        if (Inventory.getItemAmount(ItemID.COAL) > 0)
                        {
                            BankInventoryInteraction.useItem(ItemID.COAL, "Deposit-all");
                        }
                        if (Inventory.getItemAmount(ItemID.RUNITE_BAR+1) == 0)
                        {
                            BankInteraction.useItem(ItemID.RUNITE_BAR, "Withdraw-All");
                        }
                        if (Inventory.getItemAmount(ItemID.VARROCK_TELEPORT) == 0)
                        {
                            BankInteraction.useItem(ItemID.VARROCK_TELEPORT, "Withdraw-1");
                        }
                        if (Inventory.getItemAmount(ItemID.COINS) == 0)
                        {
                            BankInteraction.useItem(ItemID.COINS, "Withdraw-All");
                        }
                        if (Bank.search().withId(ItemID.VARROCK_TELEPORT).first().isEmpty())
                        {
                            EthanApiPlugin.stopPlugin(this);
                        }
                        return;
                    }
                    if (client.getVarbitValue(Varbits.STAMINA_EFFECT) <= 2 && client.getEnergy() <= 5000 && Inventory.getItemAmount(ItemID.STAMINA_POTION1) == 0)
                    {
                        System.out.println("getting stam and energy");
                        BankInteraction.useItem(ItemID.STAMINA_POTION1, "Withdraw-1");
                        BankInteraction.useItem(ItemID.SUPER_ENERGY1, "Withdraw-1");
                        timeout = 3;
                        return;
                    }
                    if (client.getVarbitValue(Varbits.STAMINA_EFFECT) <= 2 && client.getEnergy() > 5000 && Inventory.getItemAmount(ItemID.STAMINA_POTION1) == 0)
                    {
                        System.out.println("getting stam");
                        BankInteraction.useItem(ItemID.STAMINA_POTION1, "Withdraw-1");
                        timeout = 1;
                        return;
                    }
                    if (Inventory.getItemAmount(ItemID.STAMINA_POTION1) > 0)
                    {
                        BankInventoryInteraction.useItem(ItemID.STAMINA_POTION1, "Drink");
                        timeout = 3;
                        return;
                    }
                    if (Inventory.getItemAmount(ItemID.SUPER_ENERGY1) > 0)
                    {
                        BankInventoryInteraction.useItem(ItemID.SUPER_ENERGY1, "Drink");
                        timeout = 3;
                        return;
                    }
                    if (Inventory.getItemAmount(ItemID.VIAL) > 0)
                    {
                        BankInventoryInteraction.useItem(ItemID.VIAL, "Deposit-All");
                        timeout = 1;
                        return;
                    }
                    if (Inventory.getItemAmount(ItemID.COAL_BAG_12019) == 1 && coalBagAmount < 27)
                    {
                        //todo make this only try to do this if coal bag has fill option
                        System.out.println("filling coal bag");
                        BankInventoryInteraction.useItem(ItemID.COAL_BAG_12019, "Fill");
                        if (!BankInventory.search().withAction("Fill").first().isEmpty())
                        {
                            timeout = 1;
                            return;
                        }
                    }
                    if (coalInFurnace >= 108)
                    {
                        if (Inventory.getItemAmount(ItemID.RUNITE_ORE) < 27)
                        {
                            System.out.println("Withdrawing runite ore");
                            BankInteraction.useItem(ItemID.RUNITE_ORE, "Withdraw-All");
                            if (conveyor.isPresent())
                            {
                                System.out.println("clicking conveyor.");
                                //Need to go to conveyor.
                                TileObjectInteraction.interact(conveyor.get(), "Put-ore-on");
                                timeout  = 11;
                            }
                        }
                    }
                    else
                    {
                        if (Inventory.getItemAmount(ItemID.COAL) < 27)
                        {
                            System.out.println("Withdrawing Coal");
                            BankInteraction.useItem(ItemID.COAL, "Withdraw-All");
                            timeout = 1;
                        }
                        else
                        {
                            if (conveyor.isPresent())
                            {
                                System.out.println("clicking conveyor.");
                                //Need to go to conveyor.
                                TileObjectInteraction.interact(conveyor.get(), "Put-ore-on");
                                timeout  = 11;
                            }
                        }
                    }
                }
            }
        }
        else return;
    }

    @Subscribe
    public void onChatMessage(ChatMessage e)
    {
        if (e.getType() == ChatMessageType.GAMEMESSAGE)
        {
            coalBagAmount =  CoalBag.updateAmount(e.getMessage());
        }
    }
    @Subscribe
    public void onGrandExchangeSearched(GrandExchangeSearched event)
    {

    }

    public void updateGrandExchangeResults()
    {

    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged e)
    {

    }
}
