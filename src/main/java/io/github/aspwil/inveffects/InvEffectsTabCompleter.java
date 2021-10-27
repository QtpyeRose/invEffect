package io.github.aspwil.inveffects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class InvEffectsTabCompleter implements TabCompleter {

    private final List<String> types = new ArrayList<>(Arrays.asList("inv", "hand", "offhand", "hotbar", "armor"));
    private final List<String> effects = new ArrayList<>(Arrays.asList("SPEED", "SLOW", "FAST_DIGGING", "SLOW_DIGGING", "INCREASE_DAMAGE", "HEAL", "HARM", "JUMP", "CONFUSION", "REGENERATION", "DAMAGE_RESISTANCE", "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY", "BLINDNESS", "NIGHT_VISION", "HUNGER", "WEAKNESS", "POISON", "WITHER", "HEALTH_BOOST", "ABSORPTION", "SATURATION", "GLOWING", "LEVITATION", "LUCK", "UNLUCK", "SLOW_FALLING", "CONDUIT_POWER", "DOLPHINS_GRACE", "BAD_OMEN", "HERO_OF_THE_VILLAGE"));
    private final List<String> subCommands = new ArrayList<>(Arrays.asList("clear", "set", "remove", "types", "effects", "list"));
    private final List<String> number = new ArrayList<>(Arrays.asList("<number>"));

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        switch (args.length) {
            case 1:
                return match(subCommands, args[0]);
            case 2:
                if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove")){
                    return match(types, args[1]);
                } else {
                    return new ArrayList<>();
                }
            case 3:
                if((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove") && types.contains(args[1]))){
                    return match(effects, args[2]);
                } else {
                    return new ArrayList<>();
                }
            case 4:
                if((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove") && types.contains(args[1]) && effects.contains(args[2]))){
                    return number;
                } else {
                    return new ArrayList<>();
                }
            default:
                return new ArrayList<>();
        }


    }
    private List<String> match(List<String> list, String start){
        List<String> matchingObjects = list.stream().
                filter(s -> s.toLowerCase().startsWith(start.toLowerCase())).
                collect(Collectors.toList());
        Collections.sort(matchingObjects);
        return matchingObjects;
    }
}
