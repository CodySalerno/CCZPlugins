package com.example.Crafting;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Inventory;
import com.example.EthanApiPlugin.TileObjects;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketDef;
import com.example.PacketUtils.PacketReflection;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.List;


@PluginDescriptor(
        name = "Crafting",
        description = "",
        enabledByDefault = false,
        tags = {"sal"}
)
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(com.example.PacketUtils.PacketUtilsPlugin.class)
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
    int timeout = 0;
    int levelToBreak = 99;
    boolean needleWithdrawn = false;


    @Override
    @SneakyThrows
    public void startUp()
    {
        needleWithdrawn = false;
        timeout = 0;
        levelToBreak = 99;
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        currentLevel = client.getRealSkillLevel(Skill.CRAFTING);
        timeout--;
        if (levelToBreak <= client.getRealSkillLevel(Skill.CRAFTING))
        {
            timeout = 0;
        }
        if (timeout <= 0)
        {
            if (currentLevel >= 31)
            {
                EthanApiPlugin.stopPlugin(this);
            }
            else if (currentLevel >= 27)
            {
                gems(emeraldId, 31);
            }
            else if (currentLevel >= 20)
            {
                gems(sapphireId, 27);
            }
            if (currentLevel >= 14)
            {
                leather(leatherBodyId, 20);
            }
            else if (currentLevel >= 11)
            {
                leather(leatherVambracesId, 14);
            }
            else if (currentLevel >= 9)
            {
                leather(leatherCowlId, 11);
            }
            else if (currentLevel >= 7)
            {
                leather(leatherBootsId, 9);
            }
            else
            {
                leather(leatherGlovesId,7 );
            }
        }
    }

    public void gems(int id, int levelBreak)
    {

    }

    public void leather(int id, int levelBreak)
    {
        levelToBreak = levelBreak;
        if (Inventory.getItemAmount(ItemID.NEEDLE) == 0 || Inventory.getItemAmount(ItemID.THREAD) == 0)
        {
            TileObject bank = TileObjects.search().withId(10060).first().orElse(null);
            if (bank != null)
            {
                if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null)
                {
                    TileObjectInteraction.interact(bank, "Bank");
                    timeout = 3;
                    return;
                }
                else
                {
                    if (needleWithdrawn)
                    {
                        if (Inventory.getItemAmount(ItemID.NEEDLE)== 0)
                        {
                            System.out.println("No needles in bank closing plugin");
                            EthanApiPlugin.stopPlugin(this);
                        }
                    }
                    System.out.println("Withdrawing needle");
                    BankInteraction.useItem(ItemID.NEEDLE, "Withdraw-1");
                    BankInteraction.useItem(ItemID.THREAD, "Withdraw-all");
                    needleWithdrawn = true;
                    timeout = 2;
                    return;

                }
            }
            else
            {
                System.out.print("Not near bank and need supplies closing plugin.");
            }
        }
        if (Inventory.getItemAmount(leather) > 0)
        {
            Widget needle = Inventory.search().withId(ItemID.NEEDLE).first().orElse(null);
            Widget leatherWidget = Inventory.search().withId(ItemID.LEATHER).first().orElse(null);
            if (needle != null && leatherWidget != null)
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(needle, leatherWidget);
                timeout = 1;
                //timeout = (Inventory.getItemAmount(leather) * 3)+1;
            }
            Widget npcDialogOptions = client.getWidget(270, 2);
            if (npcDialogOptions != null)
            {
                Widget gloves = client.getWidget(270,14);
                int leatherAmount = Inventory.getItemAmount(ItemID.LEATHER);
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(gloves.getId(), leatherAmount);
                System.out.println("started crafting.");
                timeout = leatherAmount*3+5;
                return;
            }
        }
    }

}
