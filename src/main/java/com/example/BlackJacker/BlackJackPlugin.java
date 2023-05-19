package com.example.BlackJacker;
import com.example.EthanApiPlugin.*;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.*;
import com.example.PacketUtils.WidgetInfoExtended;
import com.google.inject.Inject;
import com.google.inject.spi.BindingScopingVisitor;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;
import java.util.stream.Collectors;

@PluginDescriptor
        (
                name = "BlackJackSal",
                enabledByDefault = false
        )
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(com.example.PacketUtils.PacketUtilsPlugin.class)
public class BlackJackPlugin extends Plugin
{
    @Inject
    MousePackets mousePackets;
    @Inject
    ObjectPackets objectPackets;
    @Inject
    Client client;
    @Inject
    WidgetPackets widgetPackets;
    int[][] rockPos = new int[][]{{3165, 2908}, {3165, 2909}, {3165, 2910}, {3167, 2911}};
    WorldPoint outsideCurtain = new WorldPoint(3364, 2999,0);
    WorldPoint insideCurtain = new WorldPoint(3364, 3000,0);
    int timeout = 0;
    int rock = 0;
    int knockout = 0;
    boolean eatToFull = false;
    @Inject
    EthanApiPlugin api;


    @Override
    @SneakyThrows
    public void startUp()
    {
        timeout = 0;
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        knockout--;
        timeout--;
        System.out.println("inside house2");
        if (eatToFull)
        {
            System.out.println("inside eat to full");
            System.out.println(timeout);
            if (timeout <= 0)
            {
                System.out.println("eating");
                InventoryInteraction.useItem(1993,"Drink");
                timeout = 3;
            }
            if (this.client.getBoostedSkillLevel(Skill.HITPOINTS)+11 >= this.client.getRealSkillLevel(Skill.HITPOINTS))
            {
                eatToFull = false;
            }
            if (Inventory.getItemAmount(1993) == 0)
            {
                eatToFull = false;
            }
            return;
        }
        if (inHouse())
        {
            List<TileObject> curtains = TileObjects.search().withId(1534).result();
            for (TileObject curtain : curtains)
            {
                if (curtain.getWorldLocation().getX() == 3364 && curtain.getWorldLocation().getY() == 2999 && Inventory.getItemAmount(1993) != 0)
                {
                    System.out.println("closing curtain inside house2.");
                    TileObjectInteraction.interact(curtain,"Close");
                    timeout = 2;
                    return;
                }
            }
            System.out.println("inside house1");
            Optional<Widget> wine = Inventory.search().withId(ItemID.JUG_OF_WINE).first();
            NPC bandit = client.getNpcs().stream().filter(x -> x.getId() == 735).min(Comparator.comparingInt
                    (x -> x.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()))).orElse(null);
            if (wine.isEmpty() && timeout <= 0)
            {
                System.out.println("open curtain");
                curtains = TileObjects.search().withId(1533).result();
                for (TileObject curtain : curtains)
                {
                    System.out.println(curtain.getWorldLocation().getX());
                    if (curtain.getWorldLocation().getX() == 3364 && curtain.getWorldLocation().getY() == 2999)
                    {
                        System.out.println("open curtain inside if");
                        TileObjectInteraction.interact(curtain,"Open");
                        timeout = 1;
                        return;
                    }
                }
                System.out.println("moving outside curtain");
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(outsideCurtain);
            }
            if (banditInHouse(bandit)) //black jacking if bandit is in house and wine is in inventory.
            {
                if (wine.isPresent())
                {
                    if (knockout <= 0)
                    {
                        MousePackets.queueClickPacket();
                        NPCPackets.queueNPCAction(bandit, "Knock-Out");
                        knockout = 6;
                        System.out.println("knockout");
                        return;
                    }
                    if (knockout == 4)
                    {
                        MousePackets.queueClickPacket();
                        NPCPackets.queueNPCAction(bandit, "Pickpocket");
                        System.out.println("pickpocket 1");
                        return;
                    }
                    if (knockout == 2)
                    {
                        MousePackets.queueClickPacket();
                        NPCPackets.queueNPCAction(bandit, "Pickpocket");
                        int hitpoints = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
                        if (hitpoints < 40)
                        {
                            eatToFull = true;
                            timeout = 0;
                        }
                        System.out.println("pickpocket 2");
                        return;
                    }
                }
            }
            else
            {
                api.stopPlugin(this);
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Bandit not in house closing", null);
                return;
            }
        }
        else
        {
            if (client.getLocalPlayer().getWorldLocation().getX() == outsideCurtain.getX() || client.getLocalPlayer().getWorldLocation().getX() == outsideCurtain.getY())
            {
                System.out.println("dropping");
                Inventory.search().idInList(List.of(1935)).first().ifPresent(item -> InventoryInteraction.useItem(item, "Drop"));
                List<Widget> Jugs = Inventory.search().idInList(List.of(1935)).result();
                for (Widget jug : Jugs)
                {
                    InventoryInteraction.useItem(jug,"Drop");
                }
                List<TileObject> curtains = TileObjects.search().withId(1534).result();
                for (TileObject curtain : curtains)
                {
                    if (curtain.getWorldLocation().getX() == 3364 && curtain.getWorldLocation().getY() == 2999 && Inventory.getItemAmount(1993) == 0)
                    {
                        System.out.println("close curtain ouside of door, no wines2.");
                        TileObjectInteraction.interact(curtain,"Close");
                        timeout = 2;
                        return;
                    }
                }
            }
            Widget npcDialogOptions = client.getWidget(219, 1);
            if (npcDialogOptions != null)
            {
                System.out.println("exchanging all");
                mousePackets.queueClickPacket();
                widgetPackets.queueResumePause(14352385, 3);
                return;
            }
            if (timeout <= 0 && Inventory.search().withId(1993).first().isEmpty())
            {
                NPC exchanger  = client.getNpcs().stream().filter(x -> x.getId() == 1615).min(Comparator.comparingInt
                        (x -> x.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()))).orElse(null);
                Optional<Widget> wine = Inventory.search().withId(1994).first();
                MousePackets.queueClickPacket();
                NPCPackets.queueWidgetOnNPC(exchanger, wine.get());
                return;
            }
            if (npcDialogOptions == null && Inventory.search().withId(1993).first().isPresent() && timeout <= 0)
            {
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(outsideCurtain);
                timeout = 5;
                return;
            }
            if (outsideCurtain.getX() == client.getLocalPlayer().getWorldLocation().getX() && Inventory.search().withId(1993).first().isPresent())
            {
                List<TileObject> curtains = TileObjects.search().withId(1533).result();
                for (TileObject curtain : curtains)
                {
                    if (curtain.getWorldLocation().getX() == 3364 && curtain.getWorldLocation().getY() == 2999)
                    {
                        System.out.println("open curtain inside if");
                        TileObjectInteraction.interact(curtain,"Open");
                        timeout = 2;
                        return;
                    }
                }
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(insideCurtain);
            }
        }

    }

    public boolean inHouse()
    {

        if (client.getLocalPlayer().getWorldLocation().getX() == 3363 || client.getLocalPlayer().getWorldLocation().getX() == 3364)
        {
            if (client.getLocalPlayer().getWorldLocation().getY() == 3000 || client.getLocalPlayer().getWorldLocation().getY() == 3001
                    || client.getLocalPlayer().getWorldLocation().getY() == 3002)
            {
                return true;
            }
        }
        return false;
    }
    public boolean banditInHouse(NPC bandit)
    {
        if (bandit.getWorldLocation().getX() == 3363 || bandit.getWorldLocation().getX() == 3364)
        {
            if (bandit.getWorldLocation().getY() == 3000 || bandit.getWorldLocation().getY() == 3001
                    || bandit.getWorldLocation().getY() == 3002)
            {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onChatMessage(ChatMessage e)
    {
        if (e.getMessage().toLowerCase().contains("your blow only glances off the bandit's head."))
        {
            System.out.println("set knockout to 0");
            knockout = 2;
            return;
        }

    }
}