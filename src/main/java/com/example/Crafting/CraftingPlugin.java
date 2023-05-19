package com.example.Crafting;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Inventory;
import com.example.EthanApiPlugin.TileObjects;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketDef;
import com.example.PacketUtils.PacketReflection;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.*;
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
    int leather = ItemID.LEATHER;
    int timeout = 0;
    int levelToBreak = 99;
    boolean needleWithdrawn = false;
    boolean chiselWithdrawn = false;
    int[] leatherIds = {ItemID.LEATHER_GLOVES, ItemID.LEATHER_BOOTS, ItemID.LEATHER_COWL, ItemID.LEATHER_VAMBRACES, ItemID.LEATHER_BODY};
    int[] reqLevels = {1,7,9,11,14};


    @Override
    @SneakyThrows
    public void startUp()
    {
        needleWithdrawn = false;
        timeout = 0;
        levelToBreak = 99;
        chiselWithdrawn = false;
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        currentLevel = client.getRealSkillLevel(Skill.CRAFTING);
        timeout--;
        if (levelToBreak <= currentLevel)
        {
            timeout = 0;
        }
        if (timeout <= 0)
        {
            if (Inventory.getItemAmount(leather) == 0 && Inventory.getItemAmount(sapphireId) == 0 && Inventory.getItemAmount(emeraldId) == 0)
            {
                bank();
            }
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
            else
            {
                int x = 0;
                for (int i = 0; i < reqLevels.length; i++)
                {
                    if (reqLevels[i] <= currentLevel)
                        x = i;
                }
                if (x == reqLevels.length-1)
                {
                    leather(x, 20);
                }

                else
                    leather(x, reqLevels[x+1]);
            }
        }
    }

    public void gems(int id, int levelBreak)
    {
        levelToBreak = levelBreak;
        if (Inventory.getItemAmount(ItemID.CHISEL) == 0)
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
                    if (chiselWithdrawn)
                    {
                        if (Inventory.getItemAmount(ItemID.CHISEL)== 0)
                        {
                            System.out.println("No chisel in bank closing plugin");
                            EthanApiPlugin.stopPlugin(this);
                        }
                    }
                    System.out.println("Withdrawing chisel");
                    BankInteraction.useItem(ItemID.CHISEL, "Withdraw-1");
                    chiselWithdrawn = true;
                    timeout = 2;
                    return;
                }
            }
            else
            {
                System.out.print("Not near bank and need supplies closing plugin.");
            }
        }
        if (Inventory.getItemAmount(id) > 0)
        {
            Widget chisel = Inventory.search().withId(ItemID.CHISEL).first().orElse(null);
            Widget gem = Inventory.search().withId(id).first().orElse(null);
            if (chisel != null && gem != null)
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(chisel, gem);
                timeout = 1;
                //timeout = (Inventory.getItemAmount(leather) * 3)+1;
            }
            Widget npcDialogOptions = client.getWidget(270, 2);
            if (npcDialogOptions != null)
            {
                Widget productWidget = client.getWidget(270,14);
                int gemAmount = Inventory.getItemAmount(id);
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(productWidget.getId(), gemAmount);
                System.out.println("started crafting gems.");
                timeout = gemAmount*2+3;
            }
        }
    }

    public void bank()
    {
        boolean gems = currentLevel >= 20;
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
                if (!gems)
                {
                    System.out.println("banking leather goods.");
                    for (int i : leatherIds)
                    {
                        BankInventoryInteraction.useItem(i, "Deposit-all");
                    }
                    BankInteraction.useItem(leather, "Withdraw-all");
                    timeout = 2;
                    return;
                }
                else
                {
                    System.out.println("banking gems goods.");
                    BankInventoryInteraction.useItem(ItemID.SAPPHIRE, "Deposit-all");
                    BankInventoryInteraction.useItem(ItemID.EMERALD, "Deposit-all");
                    if (currentLevel < 27)
                    {
                        BankInteraction.useItem(ItemID.UNCUT_SAPPHIRE, "Withdraw-all");
                    }
                    else
                    {
                        BankInteraction.useItem(ItemID.EMERALD, "Withdraw-all");
                    }
                    timeout = 2;
                    return;
                }

            }
        }
        else
        {
            System.out.print("Not near bank and need supplies closing plugin.");
        }
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
            Widget leatherWidget = Inventory.search().withId(leather).first().orElse(null);
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
                Widget productWidget = client.getWidget(270,14+id);
                int leatherAmount = Inventory.getItemAmount(leather);
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(productWidget.getId(), leatherAmount);
                System.out.println("started crafting.");
                timeout = leatherAmount*3+5;
            }
        }
    }

}
