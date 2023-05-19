package com.example.Crafting;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Inventory;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;

public class CraftingPlugin extends Plugin
{
    @Inject
    Client client;
    int currentLevel;
    int targetLevel = 31;
    int emeraldId = ItemID.UNCUT_EMERALD;
    int sapphireId = ItemID.UNCUT_SAPPHIRE;
    int leatherBodyId = ItemID.LEATHER_BODY;
    int leatherVambracesId = ItemID.LEATHER_VAMBRACES;
    int leatherCowlId= ItemID.LEATHER_COWL;
    int leatherBootsId = ItemID.LEATHER_BOOTS;
    int leatherGlovesId = ItemID.LEATHER_GLOVES;
    int leather = ItemID.LEATHER;

    @Subscribe
    public void onGameTick(GameTick e)
    {
        currentLevel = client.getRealSkillLevel(Skill.CRAFTING);
        if (currentLevel >= 31)
        {
            EthanApiPlugin.stopPlugin(this);
        }
        else if (currentLevel >= 27)
        {
            gems(emeraldId);
        }
        else if (currentLevel >= 20)
        {
            gems(sapphireId);
        }
        else if (currentLevel >= 14)
        {
            leather(leatherBodyId);
        }
        else if (currentLevel >= 11)
        {
            leather(leatherVambracesId);
        }
        else if (currentLevel >= 9)
        {
            leather(leatherCowlId);
        }
        else if (currentLevel >= 7)
        {
            leather(leatherBootsId);
        }
        else
        {
            leather(leatherGlovesId);
        }
    }

    public void gems(int id)
    {

    }

    public void leather(int id)
    {
        if (Inventory.getItemAmount(leather) > 0)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget();
        }
    }

}
