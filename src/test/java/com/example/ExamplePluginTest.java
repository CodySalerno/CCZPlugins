package com.example;
import com.example.BlackJacker.BlackJackPlugin;
import com.example.Crafting.CraftingPlugin;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.NexFight.NexPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PrayerTrainer.PrayerTrainerPlugin;
import com.example.WidgetFinder.WidgetFinder;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EthanApiPlugin.class, PacketUtilsPlugin.class, NexPlugin.class,
                CraftingPlugin.class, BlackJackPlugin.class, CraftingPlugin.class, PrayerTrainerPlugin.class,
                WidgetFinder.class);
        RuneLite.main(args);
    }
}