package de.dustplanet.compasscommand;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class CompassCommandListener
        implements Listener
{
    private CompassCommand plugin;

    public CompassCommandListener(CompassCommand instance)
    {
        this.plugin = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getInventory().getTitle().equals(this.plugin.compassInv.getTitle())) {
            fireGUI(event, this.plugin.itemsAndCommands);
        } else if (event.getInventory().getTitle().equals(this.plugin.compassInvAdmin.getTitle())) {
            fireGUI(event, this.plugin.itemsAndCommandsAdmin);
        }
    }

    private void fireGUI(InventoryClickEvent event, HashMap<String, String> map)
    {
        Player player = (Player)event.getWhoClicked();
        if (event.getCurrentItem() == null)
        {
            event.setCancelled(true);
            return;
        }
        String item = event.getCurrentItem().getType().name();
        if (map.containsKey(item))
        {
            String command = (String)map.get(item);

            event.setCancelled(true);
            player.closeInventory();
            if (!command.startsWith("server"))
            {
                player.performCommand(command);
            }
            else
            {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                String server = command.substring(command.indexOf(' ') + 1);
                try
                {
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                }
                catch (IOException localIOException) {}
                player.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if(event.getPlayer().isOp())
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if ((event.hasItem()) && ((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)))
        {
            Player player = event.getPlayer();
            if (event.getItem().getType() == Material.COMPASS) {
                player.openInventory(this.plugin.compassInv);
            }
        }
    }
}
