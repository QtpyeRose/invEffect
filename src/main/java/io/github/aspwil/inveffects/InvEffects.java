package io.github.aspwil.inveffects;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public final class InvEffects extends JavaPlugin {
    //set up all the variables we will be using
    private int refreshRate;
    private int priorityRefreshRate;
    private final NamespacedKey invKey = new NamespacedKey(this, "InvEffects");
    private final NamespacedKey hotBarKey = new NamespacedKey(this, "HotBarEffects");
    private final NamespacedKey handKey = new NamespacedKey(this, "HandEffects");
    private final NamespacedKey offHandKey = new NamespacedKey(this, "OffHandEffects");
    private final NamespacedKey armorKey = new NamespacedKey(this, "armorEffects");
    private final NamespacedKey[] keys = {invKey, hotBarKey, handKey, offHandKey, armorKey};

    @Override
    public void onEnable() {
        //set up the auto complete
        getCommand("InvEffects").setTabCompleter(new InvEffectsTabCompleter());
        //read the config and set values
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        refreshRate = getConfig().getInt("RefreshRate");
        priorityRefreshRate = getConfig().getInt("PriorityRefreshRate");
        //set up the schedulers to repeatedly call the functions to apply effects
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::scanAndApplyEffects, 1L, refreshRate);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::ScanAndApplyHighPriorityEffects, 1L, priorityRefreshRate);

    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("invEffects")) {
            if (!(sender instanceof Player)) {
                return false;
            }
            Player player = (Player) sender;
            String help =
                    ChatColor.RED + "~ ~ ~ [InvEffects] ~ ~ ~\n" +
                            ChatColor.GOLD + "/inve add <inv/hotbar/hand/offhand/Armor> <effect_name> <power> " + ChatColor.RED + "- adds an effect onto the item in your hand\n" +
                            ChatColor.GOLD + "/inve list " + ChatColor.RED + "- lists the effects on the item in your hand\n" +
                            ChatColor.GOLD + "/inve remove <inv/hotbar/hand/offhand/armor> effect " + ChatColor.RED + "- removes the effect from the item\n" +
                            ChatColor.GOLD + "/inve clear " + ChatColor.RED + "- removes all effects on the item" +
                            ChatColor.GOLD + "/inve types " + ChatColor.RED + "- lists all the types and what they do" +
                            ChatColor.GOLD + "/inve effects " + ChatColor.RED + "- lists the name of all the potion effects";

            if (args.length == 0) {
                player.sendMessage(help);
                return true;
            }
            switch (args[0]) {
                case "set":
                    //check to make sure the player is holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "you're not holding anything silly");
                        break;
                    }
                    //check for the correct amount of args
                    if (args.length == 4) {
                        // - - - - check if its a valid type
                        //valid types
                        String[] types = {"inv", "hotbar", "hand", "offhand", "armor"};
                        //assume its not a valid type
                        boolean correctType = false;
                        //for each type
                        for (String type : types) {
                            //check if its a legit type
                            if (args[1].equalsIgnoreCase(type)) {
                                //if it is, this is a correct type
                                correctType = true;
                                break;
                            }
                        }
                        //if its not a correct type
                        if (!correctType) {
                            //tell the player its not a correct type
                            sendMessage(player, "Invalid type, use <inv/hotbar/hand/offhand/Armor>");
                            break;
                        }
                        //check if the potion effect supplied actually exists
                        if (PotionEffectType.getByName(args[2]) == null) {
                            sendMessage(player, "unknown effect name: " + args[2]);
                            break;
                        }

                        //attempt to parse the power as an int
                        try {
                            //parse the int
                            int power = Integer.parseInt(args[3]);
                            //set the value of the effect in the meta of the item in there hand
                            setItemEffectEntry(player.getInventory().getItem(EquipmentSlot.HAND), args[1], args[2], power);
                            //tell the player it was set
                            sendMessage(player, "set " + args[2] + " effect to " + power + " for "+args[1]);
                        } catch (NumberFormatException e) {
                            //tell the player they messed up the int
                            sendMessage(player, "invalid power: " + args[3]);
                            break;
                        }
                    } else {
                        //invalid number of args, tell the player the correct syntax
                        sendMessage(player, "/inve add <inv/hotbar/hand/offhand/Armor> <effect_name> <power> - adds an effect onto the item in your hand");
                    }
                    break;
                case "list":
                    //check to make sure that player is holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "you're not holding anything silly");
                        break;
                    }
                    //attept to list all effects data
                    try {
                        sendMessage(player, "effects: \n" + getItemEffectsData(player.getInventory().getItem(EquipmentSlot.HAND)));
                    } catch (NoEffectsDataException e) {
                        sendMessage(player, "no InvEffects data on item");
                    }
                    break;

                case "remove":
                    //check the play is actualy holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "you're not holding anything silly");
                        break;
                    }
                    if (args.length == 3) {
                        //check if the type is a correct one
                        if (!(args[1].equalsIgnoreCase("inv") || args[1].equalsIgnoreCase("hotbar") || args[1].equalsIgnoreCase("hand") || args[1].equalsIgnoreCase("offhand"))) {
                            sendMessage(player, "invalid type, use <inv/hotbar/hand/offhand/Armor>");
                            break;
                        }
                        //check if the player supplied a legit potion effect name
                        if (PotionEffectType.getByName(args[2]) == null) {
                            sendMessage(player, "unknown effect name: " + args[2]);
                            break;
                        }
                        try {
                            //remove the item in hands effect entry
                            removeItemEffectEntry(player.getInventory().getItem(EquipmentSlot.HAND), args[1], args[2]);
                            sendMessage(player, "removed effect "+args[2]+" for "+args[1]);
                        } catch (NoEffectsDataException e) {
                            sendMessage(player, "there is no InvEffects data on this item");
                        } catch (NoEffectEntryException e) {
                            sendMessage(player, "there is no entry for the effect: \"" + args[2] + "\" listed on this item");
                        }
                    } else {
                        //invalid number of args, tell the player the correct syntax
                        sendMessage(player, "/inve remove <inv/hotbar/hand/offhand/Armor> effect - removes the effect from the item");
                    }
                    break;
                case "clear":
                    //check the play is actually holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "you're not holding anything silly");
                        break;
                    }
                    //check if the item actually has metadata from the plugin
                    try {
                        removeAllItemEffectEntries(player.getInventory().getItem(EquipmentSlot.HAND));
                        sendMessage(player, "InvEffects data cleared for item");
                    } catch (NoEffectsDataException e) {
                        sendMessage(player, "this item has no InvEffects data");
                    }
                    break;
                case "types":
                    sendMessage(player, "Types:\n" +
                            "inv: inventory\n" +
                            "hotbar: the hotbar (the bottom 9 slots)\n" +
                            "hand: in the players hand\n" +
                            "offhand: the offhand slot (the other hand)\n" +
                            "armor: in one of the players armor slots");
                    break;
                case "effects":
                    //make a string builder
                    StringBuilder effectList = new StringBuilder(ChatColor.RED+"[");
                    //for each effect type listed in minecraft
                    for (PotionEffectType effect : PotionEffectType.values()) {
                        //add the name of the effect to the string builder and a comma
                        effectList.append(ChatColor.GOLD);
                        effectList.append(effect.getName());
                        effectList.append(ChatColor.RED);
                        effectList.append(", ");
                    }
                    //remove the last comma and space
                    effectList.setLength(effectList.length() - 2);
                    //adda closing ]
                    effectList.append("]");
                    //display the effects list to the player
                    sendMessage(player, "Effects: " + effectList.toString());
                    break;
                default:
                    player.sendMessage(help);
                    break;

            }
            return true;
        } //If this has happened the function will return true.
        // If this hasn't happened the value of false will be returned.
        return false;
    }

    private void scanAndApplyEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerInventory inventoryObject = player.getInventory();
            applyEffectsForItems(player, refreshRate + 1, invKey, inventoryObject.getContents());
            applyEffectsForItems(player, refreshRate + 1, armorKey, inventoryObject.getArmorContents());
            ItemStack[] hotBar = new ItemStack[9];
            for (int i = 0; i <= 8; i++) {
                hotBar[i] = inventoryObject.getItem(i);
            }
            applyEffectsForItems(player, refreshRate + 1, hotBarKey, hotBar);

        }
    }

    //this is for high importance items that may get changed al lot quickly, like in item in hand and offhand
    //this is important for things like keeping combat smooth if you suddenly witch to a sword
    private void ScanAndApplyHighPriorityEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerInventory inventoryObject = player.getInventory();
            applyEffectsForItems(player, priorityRefreshRate + 1, handKey, inventoryObject.getItemInMainHand());
            applyEffectsForItems(player, priorityRefreshRate + 1, offHandKey, inventoryObject.getItemInOffHand());
        }
    }

    private void applyEffectsForItems(Player player, int length, NamespacedKey typeKey, ItemStack... items) {
        for (ItemStack itemStack : items) {
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            if (!container.has(typeKey, PersistentDataType.STRING)) {
                continue;
            }
            String[] values = container.get(typeKey, PersistentDataType.STRING).split(",");
            if (values.length == 1) {
                continue;
            }
            for (int i = 0; i < values.length; i += 2) {
                if (PotionEffectType.getByName(values[i]) == null) {
                    player.sendMessage("theres an invalid InvEffects tag on this item, unknown effect name: \"" + values[i] + "\"\n please contact an admin and let them know a generated item was configured wrong");
                    continue;
                }
                try {
                    int power = Integer.parseInt(values[i + 1]);
                    player.addPotionEffect(PotionEffectType.getByName(values[i]).createEffect(length, power));
                } catch (NumberFormatException e) {
                    player.sendMessage("theres an invalid InvEffects tag on this item, invalid effect power: \"" + values[i + 1] + "\"\n please contact an admin and let them know a generated item was configured wrong");
                }

            }

        }
    }

    private void setItemEffectEntry(ItemStack item, String type, String effectName, int power) {

        NamespacedKey key = new NamespacedKey(this, type + "effects");
        //initialise the array for the entries encoded in the metadata string
        ArrayList<String> entries = new ArrayList<>();
        //get the metadata object from the item in the players inventory
        ItemMeta itemMeta = item.getItemMeta();

        //if the item does not have a metadata tag, add one
        if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "");
        }

        //get the raw effectlist string from metadata
        String rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        //if the effectslist on the item is NOT empty
        if (!rawString.equals("")) {
            //assemble the entries' arraylist by splitting the rawstring based on ","
            entries = new ArrayList<>(Arrays.asList(rawString.split(",")));
            //remove any already existing entries for this effect
            removeEffectFromArrayList(entries, effectName);

        }
        //add the effect and its power
        entries.add(effectName);
        entries.add(String.valueOf(power));

        //get the resulting effectslist string
        String alteredEffectString = arraylistToEffectString(entries);
        //set the metadata tag
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, alteredEffectString);
        //set the metadata on the item
        item.setItemMeta(itemMeta);
    }

    private String getItemEffectsData(ItemStack item) throws NoEffectsDataException {
        //get the item metadata
        ItemMeta itemMeta = item.getItemMeta();

        // check if NONE of the keys have any data
        //assume none of the keys have data
        boolean hasData = false;
        //go through each key
        for (NamespacedKey key : keys) {
            getLogger().log(Level.WARNING, key.toString());
            //check if the key has data
            if (itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                //yes, there is data on this item
                hasData = true;
                //stop the loop
                break;
            }
        }
        //if we have not found any data throw noEffectsDataException
        if (!hasData) {
            throw new NoEffectsDataException();
        }

        //string builder for the list of effects
        StringBuilder fullList = new StringBuilder();
        //the String to hold the raw data
        String rawString;
        //the arrayList that will hold all the entries
        ArrayList<String> entries;
        for (NamespacedKey key : keys) {
            if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                continue;
            }
            //get the metadata as a raw string
            rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            //change the string into an arraylist by spliting based on ","
            entries = new ArrayList<>(Arrays.asList(rawString.split(",")));
            //add the info to the list
            fullList.append(key.getKey() + ":" + entries + "\n");
        }
        //remove the last newline
        fullList.setLength(fullList.length() - 1);
        return fullList.toString();
    }

    private void removeItemEffectEntry(ItemStack item, String type, String effectName) throws NoEffectsDataException, NoEffectEntryException {

        NamespacedKey key = new NamespacedKey(this, type + "effects");
        //get the metadata object of the item in the players and
        ItemMeta itemMeta = item.getItemMeta();

        //if the item does not have a metadata entry throw NoEffectsDataException
        if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            throw new NoEffectsDataException();
        }
        //get the raw effectlist string from metadata
        String rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        //if the metadata string does not have the effect listed throw NoEffectEntryException
        if (!rawString.contains(effectName)) {
            throw new NoEffectEntryException();
        }
        //make an arraylist by splitting the raw effectlist string
        ArrayList<String> entries = new ArrayList<>(Arrays.asList(rawString.split(",")));
        //remove the effect from the arraylsit
        removeEffectFromArrayList(entries, effectName);
        //turn the arraylist back into a string
        String alteredEffectString = arraylistToEffectString(entries);
        //set the metadata tag
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, alteredEffectString);
        //set the items meta data
        item.setItemMeta(itemMeta);
    }

    private void removeAllItemEffectEntries(ItemStack item) throws NoEffectsDataException {
        //get the metadata object of the item in the players and
        ItemMeta itemMeta = item.getItemMeta();
        for (NamespacedKey key : keys) {
            //if the item does not have any metadata entries
            if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                continue;
            }
            //set the metadata tag to nothing (viola
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "");
        }
        //set the items meta data
        item.setItemMeta(itemMeta);
    }

    //removes the effect and its power from an arraylist, returns true if it finds and removes one, else returns false
    private boolean removeEffectFromArrayList(ArrayList list, String effectName) {
        //go thorough the list
        for (int i = 0; i < list.size(); i++) {
            //look for an entry with a match name
            if (list.get(i).equals(effectName)) {
                //remove the corresponding power of that effect (the entry after)
                list.remove(i + 1);
                //remove the effect name itself
                list.remove(i);
                //return true to signify that sucsefuly removed the entry
                return true;
            }
        }
        //return false to signify we could not find the entry
        return false;
    }

    private String arraylistToEffectString(ArrayList<String> list) {
        //make a new string builder
        StringBuilder sb = new StringBuilder();
        //add all the entries to the sb sperated by commas
        for (String s : list) {
            sb.append(s);
            sb.append(",");
        }
        //if the sb is not empty remove the last comma
        if (sb.length() != 0) {
            sb.setLength(sb.length() - 1);
        }
        //return the string
        return sb.toString();
    }


    private void sendMessage(Player player, String toSay) {
        player.sendMessage(ChatColor.GOLD + "[InvEffects] " + ChatColor.RED + toSay);

    }
}

class NoEffectsDataException extends Exception {
    public NoEffectsDataException() {
    }
}

class NoEffectEntryException extends Exception {
    public NoEffectEntryException() {
    }
}
