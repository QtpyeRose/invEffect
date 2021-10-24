package io.github.aspwil.inveffects;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
    private int loopTime = 5;
    private NamespacedKey key;

    @Override
    public void onEnable() {
        key = new NamespacedKey(this, "InvEffects");
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        loopTime = getConfig().getInt("looptime");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::scanInv, 1L, 20L * loopTime);
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
                            ChatColor.GOLD +
                            "/invEffects add <effect_name> <power> - adds an effect onto the item in your hand\n" +
                            "/invEffects list - lists the effects on the item in your hand\n" +
                            "/invEffects remove <inv/hotbar/hand/offhand> effect - removes the effect from the item\n" + "" +
                            "/invEffects clear - removes all effects on the item";

            if (args.length == 0) {
                player.sendMessage(help);
                return true;
            }
            ArrayList<String> entries = new ArrayList<>();
            String rawString;
            ItemMeta itemMeta;
            StringBuilder sb;
            switch (args[0]) {
                case "set":
                    //check to make sure the player is holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "you're not holding anything silly");
                        break;
                    }
                    //check for the correct amount of args
                    if (args.length == 3) {
                        //check if the potion effect supplied actauly exists
                        if (PotionEffectType.getByName(args[1]) == null) {
                            sendMessage(player, "unknown effect name: " + args[1]);
                            break;
                        }

                        //attempt to parse the power as an int
                        try {
                            //parse the int
                            int power = Integer.parseInt(args[2]);
                            //set the value of the effect in the meta of the item in there hand
                            setItemEffectEntry(player.getInventory().getItem(EquipmentSlot.HAND), args[1], power);
                            //tell the player it was set
                            sendMessage(player, "set " + args[1] + " effect to " + power);
                        } catch (NumberFormatException e) {
                            //tell the player they messed up the int
                            sendMessage(player, "invalid power: " + args[2]);
                            break;
                        }
                    } else {
                        //invalid number of args, tell the player the correct syntax
                        sendMessage(player, "/inve add <effect_name> <power> - adds an effect onto the item in your hand");
                    }
                    break;
                case "list":
                    //check to make sure that player is holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "you're not holding anything silly");
                        break;
                    }
                    //get the item meta data
                    itemMeta = player.getInventory().getItem(EquipmentSlot.HAND).getItemMeta();

                    //check if the item has a metadata tag from this plugin
                    if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        sendMessage(player, "no invEffect data on this item");
                        break;
                    }

                    //get the metadata as a raw string
                    rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    //change the string into an arraylist by spliting based on ","
                    entries = new ArrayList<>(Arrays.asList(rawString.split(",")));
                    //tell the player the info on the item
                    sendMessage(player, "effects: " + entries);
                    break;

                case "remove":
                    //check the play is actualy holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "your not holding anything silly");
                        break;
                    }
                    if (args.length == 2) {
                        //check if the player supplied a legit potion effect name
                        if (PotionEffectType.getByName(args[1]) == null) {
                            sendMessage(player, "unknown effect name: " + args[1]);
                            break;
                        }
                        try {
                            //remove the item in hands effect entry
                            removeItemEffectEntry(player.getInventory().getItem(EquipmentSlot.HAND), args[1]);
                        } catch (NoEffectsDataException e) {
                            sendMessage(player, "there is no InvEffects data on this item");
                        } catch (NoEffectEntryException e) {
                            sendMessage(player, "there is no entry for the effect: \"" + args[1] + "\" listed on this item");
                        }
                    } else {
                        //invalid number of args, tell the player the correct syntax
                        sendMessage(player, "/inve remove effect - removes the effect from the item");
                    }
                    break;
                case "clear":
                    //check the play is actually holding something
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()) {
                        sendMessage(player, "your not holding anything silly");
                        break;
                    }
                    //check if the item actually has metadata from the plugin
                    try {
                        removeAllItemEffectEntries(player.getInventory().getItem(EquipmentSlot.HAND));
                        sendMessage(player, "EffectsData cleared for item");
                    } catch (NoEffectsDataException e) {
                        sendMessage(player, "this item has no effect data");
                    }
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

    private void scanInv() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null) {
                    continue;
                }
                PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
                if (!container.has(key, PersistentDataType.STRING)) {
                    continue;
                }
                String[] values = container.get(key, PersistentDataType.STRING).split(",");
                if (values.length == 1) {
                    continue;
                }
                for (int i = 0; i < values.length; i += 2) {
                    if (PotionEffectType.getByName(values[i]) == null) {
                        getLogger().log(Level.WARNING, "uh oh theres am unknown effect name - invEffects");
                        continue;
                    }
                    try {
                        int power = Integer.parseInt(values[i + 1]);
                        player.addPotionEffect(PotionEffectType.getByName(values[i]).createEffect(loopTime * 20 + 20, power));
                    } catch (NumberFormatException e) {
                        getLogger().log(Level.WARNING, "uh oh theres a weird metadata power value: " + values[i + 1] + " - invEffects");
                    }

                }


            }
        }
    }

    private void setItemEffectEntry(ItemStack item, String effectName, int power) {

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
            //assemble the entries arraylist by spliting the rawstring based on ","
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

    private void removeItemEffectEntry(ItemStack item, String effectName) throws NoEffectsDataException, NoEffectEntryException {

        //get the metadata object of the item in the players dand
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

        //if the item does not have any metadata entries
        if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            throw new NoEffectsDataException();
        }

        //set the metadata tag to nothing (viola
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "");
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
        player.sendMessage(ChatColor.GOLD + "[InvEffects] " + toSay);

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
