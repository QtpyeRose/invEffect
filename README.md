# invEffects
this is the github for a plugin develped for minecraft spigot 1.16.5

the invEffect plugin aims to add the ability to make items give a player potion effects from anywhere within there inventory


it has 3 commands:

/inve set effect power - applies the effect to the item in your hand

/inve list - lists the effects on the item in your hand

/inve remove effect - removes the effect from the item


it has a single permission to allow all these commands 
InvEffects.use


it works by writing metadata to the items,

so if you dont want to use the commands

you could add this meta data to an item

PublicBukkitValues:{"inveffects:effectdata":"speed,0"}

all the effects are comma sperated so if you wanted to do 2 effects you would do

PublicBukkitValues:{"inveffects:effectdata":"speed,0,regeneration,0"}

a effect value of speed,0 corisponds to the effect Speed I

and a value of speed,1 would give you Speed II


