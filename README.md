# Minefunk
A powerful compiler for Minecraft functions.

## Example
### my_mod.funk:
```
namespace my_mod {
  void install() {
    std::print("Installing a test mod");
    std::echo(false);
    std::removeCommandChainLimit();
    std::print("Installed a test mod");
  }
  
  void tick() {
    killAllInRange("creeper");
    keepAllAtOneDiamond();
  }
  
  const int RANGE_TO_KILL = 20;
  inline void killAllInRange(const string mobType) {
    $execute @a ~ ~ ~ kill @e[type=%mobType%,r=%RANGE_TO_KILL%]
  }
  
  void keepAllAtOneDiamond() {
    $clear @a diamond
    $give @a diamond
  }
}
```

### Compiles to:
#### my_mod/install.mcfunction:
```
tellraw @a Installing a test mod
gamerule commandBlockOutput false
gamerule logAdminCommands false
gamerule maxCommandChainLength 2147483647
tellraw @a Installed a test mod
```
#### my_mod/tick.mcfunction
```
execute @a ~ ~ ~ kill @e[type=creeper,r=20]
function my_mod:keepAllAtOneDiamond
```
#### my_mod/keepAllAtOneDiamond.mcfunction
```
clear @a diamond
give @a diamond
```

## Downloads
Downloads can be found at [the bintray page](https://bintray.com/earthcomputer/util/minefunk). More detailed download instructions can be found on [the wiki page](https://github.com/Earthcomputer/Minefunk/wiki/Downloading).

## Usage
The Minefunk compiler is meant for use on the command line. Again, details are to be found on the [wiki](https://github.com/Earthcomputer/Minefunk/wiki)
