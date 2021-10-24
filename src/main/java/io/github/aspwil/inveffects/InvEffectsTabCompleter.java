package io.github.aspwil.inveffects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvEffectsTabCompleter implements TabCompleter {

    private final ArrayList<String> types = new ArrayList<>(Arrays.asList("inv", "hand", "offhand", "hotbar", "armor"));
    private final ArrayList<String> effects = new ArrayList<>(Arrays.asList("SPEED", "SLOW", "FAST_DIGGING", "SLOW_DIGGING", "INCREASE_DAMAGE", "HEAL", "HARM", "JUMP", "CONFUSION", "REGENERATION", "DAMAGE_RESISTANCE", "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY", "BLINDNESS", "NIGHT_VISION", "HUNGER", "WEAKNESS", "POISON", "WITHER", "HEALTH_BOOST", "ABSORPTION", "SATURATION", "GLOWING", "LEVITATION", "LUCK", "UNLUCK", "SLOW_FALLING", "CONDUIT_POWER", "DOLPHINS_GRACE", "BAD_OMEN", "HERO_OF_THE_VILLAGE"));
    private final ArrayList<String> subCommands = new ArrayList<>(Arrays.asList("clear", "set", "remove", "types", "effects", "list"));
    private final ArrayList<String> number = new ArrayList<>(Arrays.asList("<number>"));

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        switch (args.length) {
            case 1:
                return subCommands;
            case 2:
                if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove")){
                    return types;
                } else {
                    return new ArrayList<>();
                }
            case 3:
                if((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove") && types.contains(args[1]))){
                    return effects;
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
}
