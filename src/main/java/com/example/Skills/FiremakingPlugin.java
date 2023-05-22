package com.example.Skills;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Optional;

@PluginDescriptor(
        name = "Firemkaing",
        description = "Firemaking until 55, need to start in varrockwest bank.",
        enabledByDefault = false,
        tags = {"sal"}
)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class FiremakingPlugin extends Plugin
{

    WorldPoint line1 = new WorldPoint(3280,3429,0);
    WorldPoint line2 = new WorldPoint(3280,3428,0);
    int logs = 1511;
    int oak = 1521;
    int willow = 1519;
    int teak = 6333;
    int maple = 1517;
    int mahogany = 6332;
    TileObject bank;
    @Inject
    Client client;
    int timeout = 0;
    boolean line1bool = true;

    @Override
    @SneakyThrows
    public void startUp()
    {
        line1bool = true;
        timeout = 0;
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged)
    {
        System.out.println("on stat change");
        timeout = 3;
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        bank = TileObjects.search().withId(10583).nearestToPlayer().orElse(null);
        if (Inventory.getItemAmount(590) <= 0)
        {
            System.out.println("Inventory doesn't contain tinderbox ending.");
            EthanApiPlugin.stopPlugin(this);
        }
        if (timeout > 0)
        {
            timeout--;
        }
        if (timeout <= 0)
        {
            Firemaking();
        }
    }

    public void Firemaking()
    {
        if (client.getLocalPlayer().getAnimation() == 733)
        {
            System.out.println("waiting for fire");
            timeout = 1;
            return;
        }
        if (client.getRealSkillLevel(Skill.FIREMAKING) < 15)
        {
            if (Inventory.getItemAmount(logs) > 0)
            {
                lightLogs(logs);
            }
            else
            {
                System.out.println("trying to bank");
                bank(logs);
            }
        }
        else if (client.getRealSkillLevel(Skill.FIREMAKING) < 30)
        {
            if (Inventory.getItemAmount(oak) > 0)
            {
                lightLogs(oak);
            }
            else
            {
                bank(oak);
            }
        }
        else if (client.getRealSkillLevel(Skill.FIREMAKING) < 35)
        {
            if (Inventory.getItemAmount(willow) > 0)
            {
                lightLogs(willow);
            }
            else
            {
                bank(willow);
            }
        }
        else if (client.getRealSkillLevel(Skill.FIREMAKING) < 45)
        {
            if (Inventory.getItemAmount(teak) > 0)
            {
                lightLogs(teak);
            }
            else
            {
                bank(teak);
            }
        }
        else if (client.getRealSkillLevel(Skill.FIREMAKING) < 50)
        {
            if (Inventory.getItemAmount(maple) > 0)
            {
                lightLogs(maple);
            }
            else
            {
                bank(maple);
            }
        }
        else if (client.getRealSkillLevel(Skill.FIREMAKING) < 55)
        {
            if (Inventory.getItemAmount(mahogany) > 0)
            {
                lightLogs(mahogany);
            }
            else
            {
                bank(mahogany);
            }
        }
        if (client.getRealSkillLevel(Skill.WOODCUTTING) >= 55)
        {
            EthanApiPlugin.stopPlugin(this);
        }
    }

    public void bank(int logs)
    {
        Widget toggleRun = client.getWidget(10485787);
        if (bank != null && client.getWidget(WidgetInfo.BANK_CONTAINER) == null)
        {
            System.out.println("going to bank");
            TileObjectInteraction.interact(bank, "Bank");
            timeout = 10;
        }
        if (client.getWidget(WidgetInfo.BANK_CONTAINER) != null)
        {
            if (Inventory.getItemAmount(logs) > 0)
            {
                BankInventoryInteraction.useItem(logs,"Deposit-all");
            }
            if (Inventory.getItemAmount(oak) > 0)
            {
                BankInventoryInteraction.useItem(oak,"Deposit-all");
            }
            if (Inventory.getItemAmount(willow) > 0)
            {
                BankInventoryInteraction.useItem(willow,"Deposit-all");
            }
            if (Inventory.getItemAmount(teak) > 0)
            {
                BankInventoryInteraction.useItem(teak,"Deposit-all");
            }
            if (Inventory.getItemAmount(maple) > 0)
            {
                BankInventoryInteraction.useItem(maple,"Deposit-all");
            }
            if (Inventory.getItemAmount(mahogany) > 0)
            {
                BankInventoryInteraction.useItem(mahogany,"Deposit-all");
            }

            BankInteraction.useItem(logs, "Withdraw-all");
            if (line1bool)
            {
                if (client.getVarpValue(173) == 0)
                {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(toggleRun, "Toggle Run");
                }
                System.out.println("moving line1");
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(line1);
                line1bool = false;
                timeout = 30;
            }
            else
            {
                if (client.getVarpValue(173) == 0)
                {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(toggleRun, "Toggle Run");
                }
                System.out.println("moving line2");
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(line2);
                line1bool = true;
                timeout = 30;
            }
        }
    }

    public void lightLogs(int logId)
    {
        Optional<Widget> logsToFiremake = Inventory.search().withId(logId).first();
        Optional<Widget> tinderbox = Inventory.search().withId(ItemID.TINDERBOX).first();
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetOnWidget(logsToFiremake.get(), tinderbox.get());
        timeout = 3;
    }
}
