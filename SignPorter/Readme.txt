SignPort Readme

Created by Mag0ca <Mag0ca@yahoo.ca>

This is a Bukkit server plugin that will allow users to create signs that will teleport them to locations without needing to use any commands

the signs will be in the form

<Line1>    [SignPort]
<Line2>    Sign Name
<Line3>    Destination sign name or X,Y,Z coords
<Line4>    Cost <- not implemented yet


Line 2 and 3 are optional but you will have to have at least one to make it useful
Line 4 Defaults to 0 if you don't put a number in it

if you leave the sign name off and only have the destination then you will never be able to teleport to that sign making it one way only
useful for hidden exits to secret bases or kill traps ;)

if you leave off the destination then the sign will not teleport you anywhere it will act as a waypoint only
useful to have a fast way to get home or to your spawn point

breaking the sign will delete the waypoint from the database but will not break the signs that link to it but they will not work
recreating the waypoint again will make all the signs pointing to it start functioning again to the new waypoint location

there is also one command (conflicts with WorldEdit's //SP command. use /signporter instead)

/sp p:player        - teleports you to the location of player
/sp sign            - teleports you to "sign"

Permission Nodes

SignPort.create       - allows the creation of signs
SignPort.use          - allows the use of signs and /sp sign
SignPort.tp           - allows the use of /sp p:player
SignPort.*            - allows the use of everything
