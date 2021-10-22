package io.github.aspwil.inveffects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public final class InvEffects extends JavaPlugin {
    private final String help = "/inve set effect power - applies the effect to the item in your hand\n/inve list - lists the effects on the item in your hand\n/inve remove effect - removes the effect from the item";
    private int loopTime = 5;
    private NamespacedKey key;
    @Override
    public void onEnable() {
        key = new NamespacedKey(this, "effectData");
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        loopTime = getConfig().getInt("looptime");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::scanInv, 1L, 20l * loopTime);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("invEffects")) {
            if (!(sender instanceof Player)){
                return false;
            }
            Player player = (Player) sender;
            if (args.length == 0){
                player.sendMessage(help);
                return true;
            }
            ArrayList<String> entries = new ArrayList<String>();
            String rawString;
            ItemMeta itemMeta;
            PersistentDataContainer data;
            StringBuilder sb;
            switch (args[0]){
                case "set":
                    if(player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()){
                        player.sendMessage("you're not holding anything silly");
                        break;
                    }
                    if(args.length == 3) {
                        if (PotionEffectType.getByName(args[1]) == null) {
                            player.sendMessage("unknown effect name: " + args[1]);
                            break;
                        }
                        try {
                            Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            player.sendMessage("invalid power: " + args[2]);
                            break;
                        }
                        itemMeta = player.getInventory().getItem(EquipmentSlot.HAND).getItemMeta();
                        data = itemMeta.getPersistentDataContainer();

                        if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                            player.sendMessage("no data entry, adding an empty one");
                            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "");
                        }

                        rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                        //player.sendMessage("obtained raw data string: \""+rawString+"\"");

                        if(!rawString.equals("")){
                            entries = new ArrayList<String>(Arrays.asList(rawString.split(",")));
                            //if theres already a copy of this effect remove it
                            for(int i = 0; i < entries.size(); i++){
                                if(entries.get(i).equals(args[1])){
                                    entries.set(i, "");
                                    entries.set(i+1,"");
                                    break;
                                }
                            }
                        }
                        entries.add(args[1]);
                        entries.add(args[2]);
                        sb = new StringBuilder();
                        for(String s: entries){
                            if(!s.equals("")) {
                                sb.append(s);
                                sb.append(",");
                            }
                        }
                        sb.setLength(sb.length() - 1);
                        player.sendMessage("set "+args[1]+" effect to "+args[2]);

                        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, sb.toString());
                        player.getInventory().getItem(EquipmentSlot.HAND).setItemMeta(itemMeta);


                    } else {
                        player.sendMessage("/inve add <effect_name> <power>");
                    }
                    break;
                case "list":
                    if(player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()){
                        player.sendMessage("you're not holding anything silly");
                        break;
                    }

                    itemMeta = player.getInventory().getItem(EquipmentSlot.HAND).getItemMeta();
                    data = itemMeta.getPersistentDataContainer();

                    if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                        player.sendMessage("no invEffect data on this item");
                        break;
                    }

                    rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    //player.sendMessage("obtained raw data string: \""+rawString+"\"");
                    entries = new ArrayList<String>(Arrays.asList(rawString.split(",")));
                    player.sendMessage("effects: "+entries);
                    break;
                case "remove":
                    if(player.getInventory().getItem(EquipmentSlot.HAND).getType().isAir()){
                        player.sendMessage("your not holding anything silly");
                        break;
                    }
                    if(args.length == 2){
                        if (PotionEffectType.getByName(args[1]) == null) {
                            player.sendMessage("unknown effect name: " + args[1]);
                            break;
                        }
                        itemMeta = player.getInventory().getItem(EquipmentSlot.HAND).getItemMeta();
                        data = itemMeta.getPersistentDataContainer();

                        if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                            player.sendMessage("no invEffect data on this item");
                            break;
                        }

                        rawString = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                        //player.sendMessage("obtained raw data string: \""+rawString+"\"");
                        entries = new ArrayList<String>();
                        if(rawString.equals("")){
                            player.sendMessage("there are no invEffects listed on this item");
                            break;
                        } else {
                            if(!rawString.contains(args[1])){
                                player.sendMessage("this items already does not have the effect:"+args[1]);
                                break;
                            }
                            entries = new ArrayList<String>(Arrays.asList(rawString.split(",")));
                            for(int i = 0; i < entries.size(); i++){
                                if(entries.get(i).equals(args[1])){
                                    entries.set(i, "");
                                    entries.set(i+1,"");
                                    break;
                                }
                            }
                            sb = new StringBuilder();
                            for(String s: entries){
                                if(!s.equals("")) {
                                    sb.append(s);
                                    sb.append(",");
                                }
                            }
                            if(sb.length() > 0) {
                                sb.setLength(sb.length() - 1);
                            }
                            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, sb.toString());
                            player.getInventory().getItem(EquipmentSlot.HAND).setItemMeta(itemMeta);
                            player.sendMessage("removed effect: "+args[1]);
                        }
                    } else {
                        player.sendMessage("/inve remove effect - removes the effect from the item");
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

    private void scanInv(){
        for (Player player : Bukkit.getOnlinePlayers()){
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if(itemStack == null){
                    continue;
                }
                PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
                if(!container.has(key, PersistentDataType.STRING)){
                    continue;
                }
                String[] values = container.get(key, PersistentDataType.STRING).split(",");
                if(values.length == 0){
                    continue;
                }
                for(int i = 0; i < values.length; i+=2){
                    if (PotionEffectType.getByName(values[i]) == null){
                        getLogger().log(Level.WARNING, "uh oh theres am unknown effect name - invEffects");
                        continue;
                    }
                    try{
                        int power = Integer.parseInt(values[i+1]);
                        player.addPotionEffect(PotionEffectType.getByName(values[i]).createEffect(loopTime*20+20, power));
                    }catch(NumberFormatException e){
                        getLogger().log(Level.WARNING, "uh oh theres a weird metadata power value: "+values[i+1]+" - invEffects");
                        continue;
                    }

                }



            }
        }
    }


    public void tellPlayer(Player player, String toSay){
        player.sendMessage(toSay);
    }

    private void scream(String toSay){
        Bukkit.broadcastMessage(toSay);
    }
}
