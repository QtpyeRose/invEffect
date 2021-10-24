# invEffects
<pre>
this is the github for a plugin developed for minecraft spigot 1.16.5

the invEffect plugin aims to add the ability to make items give a player potion effects from anywhere within there inventory


it has 4 commands:

/invEffects set [inv/hotbar/hand/offhand/Armor] [effect] [power] - applies the effect to the item in your hand

/invEffects list - lists the effects on the item in your hand

/invEffects remove [inv/hotbar/hand/offhand/Armor] [effect] - removes the effect from the item

/invEffects clear - removes all the effects on the item in your hand


inv/hotbar/hand/offhand/armor specifies where the item has to be in order for the effect to activate

inv: inventory
hotbar: the hotbar (the bottom 9 slots)
hand: in the players hand
offhand: the offhand slot (the other hand)
armor: in one of the players armor slots


it has a single permission to allow all these commands 
InvEffects.use


it works by writing metadata to the items,

so if you don't want to use the commands

you could add this meta data to an item

PublicBukkitValues:{"inveffects:InvEffects":"speed,0"}

all the effects are comma separated so if you wanted to do 2 effects you would do

PublicBukkitValues:{"inveffects:inveffects":"speed,0,regeneration,0"}

a effect value of speed,0 corresponds to the effect Speed I

and a value of speed,1 would give you Speed II

there is a corresponding tag for each set of spaces in your inventory

inv: "inveffects:inveffects"
hotbar: "inveffects:hotbareffects"
hand: "inveffects:handeffects"
offhand: "inveffects:offhandeffects"
armor: "inveffects:armoreffects"
</pre>


ALL OF THE CODE IN THIS PROJECT IS DEDICATED TO THE PUBLIC DOMAIN, and as such is free for anyone to use, distribute, and modify for any reason, commercial or noncomercal, with or without attribution to the creator(s), although attribution is smiled upon.

basicly what im trying to say is use this code for whatever you want. if i stop updating it feel free to do so yourself and re-release it, or use any part of it for what ever project you want. i made it from scratch in like 12 hours of total work, so its not much, but i hope it helps

