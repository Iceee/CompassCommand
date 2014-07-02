package de.dustplanet.compasscommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;

public class CompassCommand
        extends JavaPlugin
        implements CommandExecutor, Listener
{
    public Inventory compassInv;
    public Inventory compassInvAdmin;
    public ItemStack compassItem;
    public ItemStack compassItemAdmin;
    public HashMap<String, String> itemsAndCommands;
    public HashMap<String, String> itemsAndCommandsAdmin = new HashMap();
    public FileConfiguration config;
    public FileConfiguration admin;
    private File adminFile;
    private File configFile;

    public void onEnable()
    {
        loadConfig();

        this.config = getConfig();
        saveConfig();

        this.admin = YamlConfiguration.loadConfiguration(this.adminFile);
        try
        {
            this.admin.save(this.adminFile);
        }
        catch (IOException e)
        {
            getLogger().warning("Failed to save the admin.yml");
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new CompassCommandListener(this), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");


        this.compassInv = makeInventory(this.config);
        this.compassInvAdmin = makeInventory(this.admin);


        this.compassItem = makeItem(this.config);
        this.compassItemAdmin = makeItem(this.admin);


        this.itemsAndCommands = makeCommandList(this.config, this.compassInv);
        this.itemsAndCommandsAdmin = makeCommandList(this.admin, this.compassInvAdmin);
    }

    private void loadConfig()
    {
        this.configFile = new File(getDataFolder(), "config.yml");
        if ((!this.configFile.exists()) && (!getDataFolder().exists())) {
            if (!getDataFolder().mkdirs())
            {
                getLogger().severe("The config folder could NOT be created, make sure it's writable!");
                getLogger().severe("Disabling now!");
                setEnabled(false);
                return;
            }
        }
        if (!this.configFile.exists()) {
            copy(getResource("config.yml"), this.configFile);
        }
        this.adminFile = new File(getDataFolder(), "admin.yml");
        if (!this.adminFile.exists()) {
            copy(getResource("admin.yml"), this.adminFile);
        }
    }

    private Inventory makeInventory(FileConfiguration fileConf)
    {
        int slots = fileConf.getInt("global.slots");
        if (slots % 9 != 0) {
            slots = 9;
        }
        String title = ChatColor.translateAlternateColorCodes('&', fileConf.getString("global.invTitle"));

        return getServer().createInventory(null, slots, title);
    }

    private ItemStack makeItem(FileConfiguration fileConf)
    {
        ItemStack item = new ItemStack(Material.COMPASS, 1);

        Material mat = Material.matchMaterial(fileConf.getString("item.type"));
        if (mat != null) {
            item.setType(mat);
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fileConf.getString("item.title")));
        meta.setLore(fileConf.getStringList("item.lore"));
        item.setItemMeta(meta);
        return item;
    }

    private HashMap<String, String> makeCommandList(FileConfiguration fileConf, Inventory inv)
    {
        HashMap<String, String> map = new HashMap();
        for (String key : fileConf.getConfigurationSection("commands").getKeys(false))
        {
            Material mat = Material.matchMaterial(key.toUpperCase());
            if (mat == null)
            {
                getLogger().info("The item " + key + " is unkown and was skipped!");
            }
            else
            {
                map.put(mat.name(), fileConf.getString("commands." + key + ".command"));

                ItemStack item = new ItemStack(mat, 1);

                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fileConf.getString("commands." + key + ".title")));
                meta.setLore(fileConf.getStringList("commands." + key + ".lore"));
                item.setItemMeta(meta);
                if (inv.firstEmpty() != -1) {
                    inv.addItem(new ItemStack[] { item });
                }
            }
        }
        return map;
    }

    private void copy(InputStream in, File file)
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
        }
        catch (IOException e)
        {
            getLogger().warning("Failed to copy the default config! (I/O)");
            e.printStackTrace();
            try
            {
                if (out != null)
                {
                    out.flush();
                    out.close();
                }
            }
            catch (IOException ex)
            {
                getLogger().warning("Failed to close the streams! (I/O -> Output)");
                ex.printStackTrace();
            }
            try
            {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                getLogger().warning("Failed to close the streams! (I/O -> Input)");
                ex.printStackTrace();
            }
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.flush();
                    out.close();
                }
            }
            catch (IOException e)
            {
                getLogger().warning("Failed to close the streams! (I/O -> Output)");
                e.printStackTrace();
            }
            try
            {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e)
            {
                getLogger().warning("Failed to close the streams! (I/O -> Input)");
                e.printStackTrace();
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if ((sender instanceof Player))
        {
            Player p = (Player)sender;
            if ((args.length == 1) && (args[0].equalsIgnoreCase("admin")))
            {
                if (p.hasPermission("compasscommand.admin"))
                {
                    if (p.getInventory().firstEmpty() != -1) {
                        p.getInventory().addItem(new ItemStack[] { this.compassItemAdmin });
                    }
                    sender.sendMessage(ChatColor.GREEN + "Item added!");
                }
                else
                {
                    sender.sendMessage(ChatColor.RED + "You do not have the permission to do this!");
                }
            }
            else if (p.hasPermission("compasscommand.get"))
            {
                if (p.getInventory().firstEmpty() != -1) {
                    p.getInventory().addItem(new ItemStack[] { this.compassItem });
                }
                sender.sendMessage(ChatColor.GREEN + "Item added!");
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "You do not have the permission to do this!");
            }
        }
        else
        {
            sender.sendMessage("This command can only be used ingame");
        }
        return true;
    }
}
