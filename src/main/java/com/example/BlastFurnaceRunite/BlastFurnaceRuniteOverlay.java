package com.example.BlastFurnaceRunite;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class BlastFurnaceRuniteOverlay extends Overlay
{

    private final Client client;
    private final PanelComponent panelComponent = new PanelComponent();
    public long bars;
    public long startTime;

    @Inject
    private BlastFurnaceRuniteOverlay(Client client)
    {
        startTime = System.currentTimeMillis();
        bars = 0;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.client = client;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
        String overlayTitle = "Blast Furnace:";

        // Build overlay title
        panelComponent.getChildren().add(TitleComponent.builder()
                .text(overlayTitle)
                .color(Color.GREEN)
                .build());

        // Set the size of the overlay (width)
        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth(overlayTitle) + 30,
                0));

        // Add a line on the overlay for world number
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Bars:")
                .right(String.valueOf(bars))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time ran:")
                .right(formatTime((System.currentTimeMillis() - startTime)))
                .build());

        double time = (System.currentTimeMillis() - startTime)/3600000.0;
        panelComponent.getChildren().add(LineComponent.builder()
                .left("bars/hr:")
                .right(String.valueOf((int)(bars/time)))
                .build());




        // Show world type goes here ...

        return panelComponent.render(graphics);
    }
    public static String formatTime(final long ms)
    {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
