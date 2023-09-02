


# RouteThreeFour
RouteThreeFour is a routing tool for Generation 3 & 4 Pokémon games speedruns (Ruby/Sapphire/Emerald/FireRed/LeafGreen & Diamond/Pearl/Platinum/HeartGold/SoulSilver).  
It is derived from a custom RouteThree, itself derived from Dabomstew & entrpntr's RouteTwo, itself derived from HRoll's RouteOne.

### 1. LATEST CHANGES
*Syntax :*    
**`[YYYY/MM/DD]` - version**  
► Compatibility-breaking update.  
○ Usual update.  
**!!** Bug fixes.  


**`[2023/09/02]` - v0.4.6**  
**!!** Selling items now gives the player half the item cost.  
**!!** Fixed an issue with wrong damage calculation when the attacking move is `Ice` type and one of the defending type is `Fire`.  
**!!** `Future Sight` and `Doom Desire` now have damage variance (as expected). 
○ Added documentation for command `pcUpdate`.  
○ Deactivated `Speed Boost` automatic Speed changes.  


**`[2023/08/03]` - v0.4.5**  
**!!** Fixed an issue in option `-IVvariation`. When the enemy had a guaranteed OHKO for a certain defensive IV value, the output was saying that the OHKO was guaranteed for all subsequent IV values, which is inaccurate is most cases.  
**!!** Fixed `Intimidate` automatic Attack drop which was triggered on the second Pokémon if the first Pokémon was negating it with its Ability.  
○ [*Gen 3*] Implemented `Facade` boost when burned, asleep, poisoned or toxic'd.  
○ [*Gen 3*] Implemented `Low Kick`.  

**`[2023/08/01]` - v0.4.4**  
**!!** [*Gen 3*] Fixed wrong Attack stat calculation when the player has `Roxanne` badge and either `Pure Power` or `Huge Power`.  
○ Added command `setHappiness`.
○ In commands `stats` and `lvRanges`, the option `-b` isn't affecting the output anymore.  
○ Improved output formatting.  
○ Improved residual effects (weather damage, status ailments, etc.).  
○ [*Gen 3*] Added `Forecast` and `Weather Ball` in damage calculation.  

**`[2023/07/30]` - v0.4.3**  
**!!** Fixed automatic `Intimidate` which could be triggered several times by the same Pokémon.  
**!!** Fixed trainers like `Tate & Liza` who where not considered as double battle by default (damage mode and experience).  
**!!** Fixed `-IVvariation` when encountering fixed damage moves.  
**!!** Fixed wrong behavior when earning EVs after reaching the 510 EV limit.  
○ Improved debugging messages.  
○ Added increasing/decreasing natures in `-IVvariation` display and speed thresholds display.  
○ In config files, in section `poke` : added support for starting EVs.  
○ In config files, in section `util` : added options `defaultOutputDetails`,  `defaultShowStatsOnLevelUp`, `defaultShowStatRangesOnLevelUp` and `defaultIvVariation`.  
○ [*Emerald*] Added `STEVEN_PARTNER` as a valid trainer.  
○ [*Emerald*] Updated Any% Glitchless route files (Mudkip, Castform, Rayquaza).  

**`[2023/07/28]` - v0.4.2**  
**!!** Fixed an issue involving `-doubleBattle` and `-singleBattle` options and shared experience.  
○ Improved debugging messages.  

**`[2023/07/27]` - v0.4.1**  
**!!** Abilities are now properly updated when evolving.  
**!!** Fury Cutter/Rollout now have the correct damage progression and numbering.  
**!!** Option `-ivvariation` now displays correct damage for every battle modifier.  
**!!** Weathers are now properly accounted for in battles.  
**!!** [*Gen 3*] The tool now stops displaying an obscure error message upon loading.  
**!!** [*Gen 3*] Move modifiers are now properly displayed in battles.  
**!!** [*Gen 3*] Fixed an issue involving moves relying on Special Attack.  
**!!** [*Gen 3*] Fixed an issue involving 0 damage cases.  
**!!** [*Gen 3*] Pursuit + switch out is properly calculated.  
**!!** [*Gen 3*] Shared exp is now accurate for natural double battles.  
**!!** [*HeartGold/SoulSilver*] Routes are now parsed by the tool without randomly crashing.  
**!!** [*HeartGold/SoulSilver*] Trainer natures are now correct.  
○ Added Rough Skin damage.  
○ [*Gen 4*] Added Rivalry effect.  
○ Added a `Contributors` section at the end of the readme.  

**`[2023/07/26]` - v0.4.0**  
○ Added Diamond/Pearl/Platinum/HeartGold/SoulSilver support.  
► Changed syntax for a variety of commands, refer to the detailled documentation below.  
► Changed syntax for a variety of trainer aliases.  
	**Users of former versions are advised to install this update in another folder if they don't want to modify their old route files.**

<details><summary> Show/hide previous updates </summary>  

**`[2022/01/17]` - v0.3.1**  
○ Added `-noDoubleBattle`/`-nodouble` command to force single battles.  
‼ Fixed a bug involving the `-double` battle modifier.  

**`[2022/01/16]` - v0.3**  
○ Added FireRed/LeafGreen support, and Fire Red Squirtle High Exp route file.  
○ Added Emerald abraful route file.  
○ Added battle option `-doublebattle` to force a double battle. Updated the .xml file accordingly.  
○ Added most residual damages : poisoned, badly poisoned, burned, trapped, confused, seeded, nightmared and cursed.  
‼ Fixed a bug involving Special Defense of opponents not being modified properly.  

**`[2022/01/13]` - v0.2.1**  
○ Refactoring of the damage calculation and printing.  
○ Implementation of Psywave, Flail, Rage, Rollout, Fury Cutter, Magnitude, Low Kick and Future Sight.  

**`[2022/01/05]` - v0.2**  
○ Added Emerald support (and trainer data).  
○ Added Notepad++ .xml formatting file for route files.

**`[2022/01/02]` - v0.1**  
○ Initial beta release.

</details>

-- -- 
### 2. INSTALLATION
The tool is primarily an executable `.jar` file. In that regard, no proper installation is needed other than copying this file in a folder and have the necessary files for it to work with (see sections below).  
The tool as been tested and is proven to work with Java 1.7 .

#### 2.1 From the latest release
[Download the latest release from this page.](https://github.com/UnderscorePoY/RouteThree/releases)  
Run the .jar file.

#### 2.2 From source files
Download source files, and build the executable jar with Java 1.7 .  
IntelliJ IDEA is known to NOT work, as the ini4j package doesn't seem to be compatible with this IDE.

---
### 3. BRIEF DESCRIPTION OF THE LAYOUT  
Once installed, here are the files you need to setup in order to produce an output file containing valuable routing information :  

1. **master file** : specifies which config file to use ;
2. **config file** : contains the main Pokémon information ;
3. **route file** : gathers the actions to perform in the route.

If an error is encountered during the execution of the tool, it is displayed both in the specified output file and the specified debug file.

---
### 4. SETUP FILES
The setup files gather both the master file (unique) and the config files (multiple).  
They are each separated into sections, which name is provided between square brackets.  
> Example :
> `[mySection]` represents a section name called `mySection`.
  
Each section contains a certain number of key-value pairs in the following format : `key = value`.  
> Example :
> `species = MUDKIP` assigns the value `MUDKIP` to the key `species`.

When values are filenames, they are case sensitive. Otherwise, they are case insensitive. Which key-value pairs represent a filename or not are described below.  
> Examples :
> - `routeFile = myRouteFile.txt` is different from `routeFile = myroutefile.txt`.
> - But `species = PORYGON2`and `species = Porygon2` are the same.

Filenames can traverse folders/directories, which means you can access subfolders and parent folders. This can be useful to organize certains files together in a common directory.  
All paths take the folder containing the executable as their reference.
> Examples :
> - `test.txt` refers to the desired file in the same folder as the executable.
> - `routes/route1.txt` refers to the file `route1.txt` in the subfolder `routes`.
> - `../../configs/config.ini` refers to `config.ini` in the parent-parent folder `configs`.

#### 4.1. Master file
The `master.ini` file is the entry point of the program. It allows you to choose which configuration file will be loaded by the tool.
##### Section : `[master]`
  Tag | Expected value | Usage
  --------------- | -------------- | --------------
  `"configFile"`   | A filename.  | The configuration file to be loaded.
 `"debugFile"` | A filename. | The debugging file in case of loading issues.
 
#### 4.2. Configuration files
A configuration file (generally with the `.ini` extension) gathers the primary information used in the route.

##### Section : `[game]`
   Tag | Expected value | Usage
  --------------- | -------------- | --------------
  `"game"`   | `ruby`, `sapphire`, `emerald`, `firered`, `leafgreen` <br> `diamond`, `pearl`, `platinum`, `heartgold`, `soulsilver`| The name of the game.
  
##### Section : `[poke]`
   Tag | Expected value | Usage
  --------------- | -------------- | --------------
  `"level"`   | An integer between `1` and `100`.| The starting level of the main Pokémon.
  `"species"`   | A string. | The name of the main Pokémon species.
  `"nature"`   | A string. | The nature of the species. <br/>*(Defaults to a neutral species if missing.)*
  `"ability"`   | A string. | The ability of the species. <br/>*(Defaults to the first species ability if missing.)*
  `"hpIV"`   | An integer between `0` and `31`.| The HP IV (Individual Value) of the main Pokémon.
  `"atkIV"`   | An integer between `0` and `31`.| The Attack IV of the main Pokémon.
  `"defIV"`   | An integer between `0` and `31`.| The Defense IV of the main Pokémon.
  `"spaIV"`   | An integer between `0` and `31`.| The Special Attack IV of the main Pokémon.
  `"spdIV"`   | An integer between `0` and `31`.| The Special Defense IV of the main Pokémon.
  `"speIV"`   | An integer between `0` and `31`.| The Speed IV of the main Pokémon.
  `"boostedExp"`   | `true` or `false`. | Whether the main Pokémon benefits from the trading experience boost or not. <br/>*(Defaults to `false` if missing.)*
  `"pokerus"`   | `true` or `false`. | Whether the main Pokémon benefits from the Pokérus Effort Value (EV) boost or not. <br/>*(Defaults to `false` if missing.)*
  `"hpEV"`    | An integer between `0` and `252`.| The HP EV (Effort Value) of the main Pokémon. <br/>*(Defaults to `0` if missing.)*
  `"atkEV"`   | An integer between `0` and `252`.| The Attack EV of the main Pokémon. <br/>*(Defaults to `0` if missing.)*
  `"defEV"`   | An integer between `0` and `252`.| The Defense EV of the main Pokémon. <br/>*(Defaults to `0` if missing.)*
  `"spaEV"`   | An integer between `0` and `252`.| The Special Attack EV of the main Pokémon. <br/>*(Defaults to `0` if missing.)*
  `"spdEV"`   | An integer between `0` and `252`.| The Special Defense EV of the main Pokémon. <br/>*(Defaults to `0` if missing.)*
  `"speEV"`   | An integer between `0` and `252`.| The Speed EV of the main Pokémon. <br/>*(Defaults to `0` if missing.)*

  
##### Section : `[files]`
   Tag | Expected value | Usage
  --------------- | -------------- | --------------
  `"routeFile"`   | A filename. | The name of the route file the tool will read from.
  `"outputFile"`   | A filename. | The name of the output file the tool will write to. <br/> *(Defaults to `outputs/out_<nameOfRouteFile>` if missing.)*

##### Section : `[util]`
   Tag | Expected value | Usage
  --------------- | -------------- | --------------
   `"defaultOutputDetails"` | An integer between `0` and `3`. | The default detail level for the output. <br/>  `0` means close to nothing, `3` means everything. <br/>  *(Defaults to `0` if missing.)*
  `"defaultShowStatsOnLevelUp"` | `true` or `false`. | Whether all level ups should display stats or not. <br/>  *(Defaults to `false` if missing.)*
  `"defaultShowStatRangesOnLevelUp"` | `true` or `false`. | Whether all level ups should display stat ranges or not. <br/>  *(Defaults to `false` if missing.)*
  `"defaultIvVariation"` | `true` or `false`. | Whether all damage calculation should be performed with all nature and IV values possible. <br/>  *(Defaults to `false` if missing.)*
  `"overallChanceKO"`   | `true` or `false`. | Whether the tool displays overall chance of KOing the opponent or not. <br/>*(Defaults to `false` if missing.)*
  `"showGuarantees"`   | `true` or `false`. | Whether the information that n-shots are guaranteed is displayed or not. <br/>*(Defaults to `false` if missing.)*
  
-- -- 

### 5. PRELIMINARY NOTES FOR ROUTE FILES

A route file is a plain-text file where each line represents a game action to be performed. Each game action starts with a `command` name and can be specialized in two ways : through the use of arguments and/or options.

- `"command"` : Every command will be put between quotation marks in this documentation. You SHOULD NOT be writing these quotation marks in your route files.  
- `"alias"` : An alias refers to a shorter name for a given command.  
- `"CoMMAnd"` : Every command is case-incensitive. You can capitalize at will.  
-- The convention from the initial release is to keep commands lowercase, and names/moves/etc uppercase.  
-- You can disobey this convention at will.  
-- For readability purposes, the commands here will display some uppercase letters.  
- `<ARGUMENT>` : When using angle brackets, it refers to a mandatory argument. You SHOULD NOT be writing these brackets in your routing files.  
- `[ARGUMENT]` : When using square brackets, it refers to an optional argument. You can omit it if you don't need it. You SHOULD NOT be writing these brackets in your routing files. 
- `-option` : Adds an option named `option` to the game action. An option can also take arguments to work with.

The `resources` folder contains most of the data used by the tool. You can look into the different files to check on trainer names, item names, etc. Note that the files in this folder are not the files the tool specifically uses to work with : you can modify them if needed, without any impact on the tool.
-- -- 

### 5. MAKING A ROUTE FILE

#### 5.1. COMMENTS
  `"//"` or `"##"` : Starts a comment. Either at the start of a line or at the end of instructions. Used as documentation for the reader/router.  
> Example :
> ```
> ## This text is a comment...
> // ... and this one as well.
> doingSomeGameAction // Performs an action, with this text as a comment.`
> ```

#### 5.2. GENERIC OUTPUT

##### 5.2.1. Money
- `"money"` : Displays the current Player money.  
  
##### 5.2.2. Stats
- `"ranges"` : Displays a table of the main Pokemon stats for all IV and nature combinations.  <br><br>
- `"stats"`  : Displays the main Pokemon stats as they are in the Pokemon menu.  
  *Available options :*  
  `"-b"` : Factors in stat badge boosts (only has an effect in Gen 3).  

#### 5.3. PLAYER POKEMON UTILITY

##### 5.3.1. Player Pokemon

###### Species
- `"evolve <SPECIES>"` : Changes your Pokemon to `SPECIES`. `SPECIES` only considers alphanumerical characters in a case-incensitive way.  
  *aliases*: `"e"`, `"changeForm"`.
> Example :
> ```
> evolve COMBUSKEN // I'm Ryziken
> e MRMIME // This matches Mr. Mime 
> e Mr_Mime // This also matches Mr. Mime` 
> changeForm WORMADAM_TRASH // We just battled in a building
> ```
  
###### Moves
- `"learnMove <MOVE>"` : Learns move `MOVE`. `MOVE` only considers alphanumerical characters in a case-incensitive way.  
  *alias*: `"lm"`          
> Examples :
> ```
> learnMove HIDDENPOWER // "Any Pokémon is runnable with a proper Hidden Power" - Nobody, ever  
> lm FURYSWIPES // This matches Fury Swipes
> ```
  
 - `"unlearnMove <MOVE>"` : Unlearns move `MOVE`. `MOVE` only considers alphanumerical characters in a case-incensitive way.  
  *alias*: `"um"`            
> Example : 
> ```
> unlearnmove GROWL // Useless move
> ```
  
###### Vitamins
- `"rareCandy [QUANTITY]"` : Uses a Rare Candy on your Pokemon, `QUANTITY` times.  If specified, `QUANTITY` must be an integer bigger or equal to 1. If omitted, it defaults to `1`.  
  *alias*: `"rc"`  
- `"hpup [QUANTITY]"`  : Uses an HP Up on your Pokemon, `QUANTITY` times.
 - `"protein [QUANTITY]"` : Uses a Protein on your Pokemon, `QUANTITY` times.
 - `"iron [QUANTITY]"` : Uses an Iron on your Pokemon, `QUANTITY` times.  
 - `"calcium [QUANTITY]"` : Uses a Calcium on your Pokemon, `QUANTITY` times.  
 - `"zinc [QUANTITY]"` : Uses a Zinc on your Pokemon, `QUANTITY` times.  
 - `"carbos [QUANTITY]"`  : Uses a Carbos on your Pokemon, `QUANTITY` times.  
> Example : 
> ```
> rareCandy 3 // Using 3 Rare Candies  
> protein // Using 1 Protein
> ```  

###### Pokerus
- `"setPokerus"` : Infects your Pokémon with Pokérus. Allows to double EV yields until maximum values are reached.
- `"unsetPokerus"` : Deactivates Pokérus.  

###### Happiness
- `"setHappiness <VALUE>"` : Sets happiness to value `VALUE`. Must be an integer between `0` and `255`.  

###### PC
- `"pcUpdate"` : Updates current EVs (mimics depositing and withdrawing a Pokémon from the PC).  
  *alias*: `"pc"`  

##### 5.3.2. Items
- `"equipItem <ITEM>"` : Equips the item `ITEM`. If an item was already held, it is replaced by the specified one. `ITEM` only considers alphanumerical characters in a case-incensitive way.  
 *alias* : `"equip"`
> Example : 
> ```
> equip SOFTSAND // Mud Slap go brrrr
> ```

- `"unequipItem"` : Unequips the held item.  
  *alias* : `"unequip"`
  
  Here is an exhaustive list of items which effects are implemented :
###### Money
  Item name|  Multiplier  
  --------------- | -------------- 
  `"AMULET_COIN"`<br/> `"LUCK_INCENSE"` *(Gen 4)*   | x2  

###### Experience
  Item name|  Multiplier  
  --------------- | -------------- 
  `"LUCKY_EGG"`   | x1.5  

###### Species-boosting items
  Species| Item name| Boosted stats/type             | Multiplier  
  --------------- | -------------- | ------------------------- | ----------
  Clamperl | `"DEEP_SEA_SCALE"`   | Special Defense | x2  
  Clamperl | `"DEEP_SEE_TOOTH"`   | Special Attack | x2  
  Cubone<br/>Marowak | `"THICK_CLUB"`   | Attack                    | x2  
  Dialga | `"ADAMANT_ORB"` *(Gen 4)* | Dragon & Steel | x1.2
  Ditto          | `"METAL_POWDER"` | Defense | x2  
  Giratina | `"GRISEOUS_ORB"` *(Platinum onwards)* | Ghost & Dragon | x1.2
  Latias<br/>Latios | `"SOUL_DEW"`   | Special Attack & Special Defense | x1.5 <br/>*(outside Battle Tower)* 
  Palkia | `"LUSTROUS_ORB"` *(Gen 4)* | Water & Dragon | x1.2
  Pikachu        | `"LIGHT_BALL"`   | Special Attack *(Gen 3)*<br/>Attack & Special Attack *(Gen 4)*            | x2  


###### Type-boosting items  
In Generation 3 (resp. 4), these items approximately apply a multiplier of 1.1 (resp. 1.2).
Boosted type | Item name
  ---------------- | -------
Bug |   `"SILVER_POWDER"`<br/>`"INSECT_PLATE"` *(Gen 4)* | 
Dark  | `"BLACK_GLASSES"`<br/> `"DREAD_PLATE"` *(Gen 4)* | 
Dragon|  `"DRAGON_FANG"`<br/>`"DRACO_PLATE"` *(Gen 4)*  | 
Electric   | `"MAGNET"`<br/> `"ZAP_PLATE"` *(Gen 4)*       | 
Fighting  | `"BLACKBELT"`<br/> `"FIST_PLATE"` *(Gen 4)*    | 
Fire |`"CHARCOAL"`<br/> `"FLAME_PLATE"` *(Gen 4)*     | 
Flying  | `"SHARP_BEAK"`<br/> `"SKY_PLATE"` *(Gen 4)*    |
Ghost  |`"SPELL_TAG"`<br/>`"SPOOKY_PLATE"` *(Gen 4)*     | 
Grass  |`"MIRACLE_SEED"`<br/> `"ROSE_INCENSE"`, `"MEADOW_PLATE"` *(Gen 4)*  |
Ground  |`"SOFT_SAND"`<br/> `"EARTH_PLATE"` *(Gen 4)*     | 
Ice  | `"NEVER_MELT_ICE"`<br/> `"ICICLE_PLATE"` *(Gen 4)* |
Normal|`"SILK_SCARF"`    |
Poison  |`"POISON_BARB"`<br/> `"TOXIC_PLATE"` *(Gen 4)*  | 
Psychic  |`"TWISTED_SPOON"`<br/> `"ODD_INCENSE"`, `"MIND_PLATE"` *(Gen 4)* | 
Rock  |`"HARD_STONE"`<br/> `"ROCK_INCENSE"`, `"STONE_PLATE"` *(Gen 4)*    | 
Steel  |`"METAL_COAT"`<br/> `"IRON_PLATE"` *(Gen 4)*    | 
Water  |`"MYSTIC_WATER"`<br/> `"SEA_INCENSE"`, `"WAVE_INCENSE"`, `"SPLASH_PLATE"` *(Gen 4)*  | 

###### EV-boosting items
All these items stack with Pokérus and halve the speed.
Item name | Effects
-- | --
`"MACHO_BRACE"` | Doubles received EVs
`"POWER_WEIGHT"` | Adds 4 HP EVs to base EV yields *(Gen 4)*
`"POWER_BRACER"` | Adds 4 Attack EVs to base EV yields *(Gen 4)*
`"POWER_BELT"` | Adds 4 Defense EVs to base EV yields *(Gen 4)*
`"POWER_LENS"` | Adds 4 Special Attack EVs to base EV yields *(Gen 4)*
`"POWER_BAND"` | Adds 4 Special Defense EVs to base EV yields *(Gen 4)*
`"POWER_ANKLET"` | Adds 4 Speed EVs to base EV yields *(Gen 4)*

##### 5.3.3. Player money
  These commands only affect money, since there is no inventory management.  
  - `"buy [QUANTITY] <ITEM>"`  : Buys `ITEM` `QUANTITY` times. If `QUANTITY` is specified, it must be an integer bigger or equal to 1. If omitted, it defaults to `1`.  
  - `"sell [QUANTITY] <ITEM>"` : Sells `ITEM` `QUANTITY` times.  
> Example : 
> ```
> buy 46 XATTACK // Zigzagoon, go !
> sell HPUP // Sells 1 HPUP
> ```
  
-  `"addMoney <NUM>"` : Adds `NUM` to player's money.  `NUM` must be a positive integer.
-  `"spendMoney <NUM>"` : Spends `NUM` money.  
> Example : 
> ```
> addmoney 5000 // Long live the casino !  
> spendmoney 50 // Museum entrance fee ...
> ``` 
      
##### 5.3.4. Badges  
In Generation 3, defeating `Roxanne`, `Wattson`, `Norman`, `Tate & Liza` or `Brock`, `Lt. Surge`, `Koga`, `Blaine` automatically activates their respective badge boost. Note that these names are indicative and don't reflect the actual names used by the tool.

The following commands give you the desired badge without fighting its corresponding Gym Leader.  
This is useful when you route Pokémon you don't acquire/catch straight away.  
###### In Ruby/Sapphire/Emerald
  Boosted stats | Gym Leader | Command
  ---------------- | ---------- | ---------- 
  Attack | `Roxanne`| `"stoneBadge"`, `"RoxanneBadge"`
  Speed | `Wattson` |`"dynamoBadge"`, `"WattsonBadge"`     
  Defense | `Norman` | `"balanceBadge"`, `"NormanBadge"`
  Special Attack & <br/>Special Defense | `TateAndLiza` | `"mindBadge"`, `"TateAndLizaBadge"`, `"T&LBadge"`     |
  
###### In FireRed/LeafGreen
  Boosted stats | Gym Leader | Command
  ---------------- | ---------- | ---------- 
  Attack | `Brock`| `"boulderBadge"`, `"BrockBadge"`
  Speed | `Lt. Surge` |`"thunderBadge"`, `"LtSurgeBadge"`, `"SurgeBadge"`     
  Defense | `Koga` | `"soulBadge"`, `"KogaBadge"`
  Special Attack & <br/>Special Defense | `Blaine` | `"volcanoBadge"`, `"BlaineBadge"`

#### 5.4. BATTLES
##### 5.4.1. Trainers
-  `"<NAME>"`  : Triggers a trainer battle against the trainer with name `NAME`.  
  
##### 5.4.2. Wild encounters
-  `"L<NUM> <SPECIES> [NATURE] [ABILITY] [GENDER] [<HP> <ATK> <DEF> <SPA> <SPD> <SPE>]"` : Triggers a wild battle against a level `NUM` `SPECIES` with desired `NATURE` `ABILITY`, `GENDER` and IVs.  
-- if `NATURE` is ommited, it defaults to a neutral nature.  
-- if `ABILITY` is ommited, it defaults to the first species' ability.  
-- if `GENDER` is ommited, it defaults to the species' most predominent gender. Genders are either `"MALE"`, `"FEMALE"` or `"GENDERLESS"` (or their corresponding one-letter initial).  
-- if IVs are ommited, defaults to `31` IV in each stat.  
  *Wild encounters options :*  
-- `"-trainer"` : Sets the wild encounter as a trainer Pokemon. Mainly gives access to the x1.5 experience multiplier.  
  *alias*: `"-t"`  
> Examples : 
> ```L45 KYOGRE HASTY DRIZZLE G 27 31 7 25 18 26 // Fast Boi
> L17 CLEFAIRY // Level 17 Clefairy, neutral nature, Cute Charm (ability 1), Female, perfect IVs
> L31 MILOTIC GENTLE -trainer // I don't remember this trainer name
> ```

      
##### 5.4.3. Battle options
  For all battle options, `x` refers to the player, `y` refers to the enemy.  
  Any option starting with `-x` can be written starting with `-y` to have the same effect on the enemy team.  

###### 5.4.3.1. Output
-  `"-verbose <LEVEL>"` : Activates output with the detailled level `LEVEL`for the desired battle.  
-- `LEVEL` must be `NONE`, `SOME`, `MOST` or `EVERYTHING` (or their corresponding integer values `0`,`1`, `2` or `3`).  
-- By default, if the `"-verbose"` option is not specified, then the battle is **still perfomed** but **not displayed** in the output file.  
*alias* : `"-v"`  

-  `"-levelUpStats"` : Outputs player Pokemon stats when a level up occurs during a battle.  
*alias* : `"-lvStats"`
-  `"-levelUpRanges"`  : Outputs player Pokemon ranges when a level up occurs during a battle.  
*alias* : `"-lvRanges"`
- `"-scenarioName <NAME>"` : Gives name `NAME` to the current fight. Useful when performing the same battle multiple times with different battle options.  
*aliases* : `"-scenario"`, `"-name"`

- `"-IVvariation"` : Displays damage ranges for all possible IV values and natures. (Also displays crit damage ranges with verbose levels `MOST` or `EVERYTHING`).

###### 5.4.3.2. Stat boosts
In all options where the notation `<stat>` appears, it denotes `atk`, `def`, `spa` (Special Attack), `spd` (Special Defense) or `spe` (Speed).
- `"x<stat>Use <NUM>"` : Boosts the `stat` `NUM` times for the entire duration of the fight (the syntax `x<stat>` is inspired from X Items).  
-- `NUM` should be an integer between `-12` and `+12`. Positive numbers can omit the `+` sign.  
> Example : 
> ```
> LANCE -xspdUse 1 -xspaUse 2 // Uses 1 X Speed & 2 X Special Attacks for the entire fight
> ```  
  
- `"x<stat>sUse <FIRST/SECOND/...>"` : Boosts the `stat` `FIRST` times on the first Pokémon, `SECOND` times on the second Pokémon, etc.  
-- `FIRST`, `SECOND` ... should be integers between `-12` and `+12`. Positive numbers can omit the `+` sign.  
*(Notice the `"s"` after the `"<stat>"` which denotes the plural form.)*
> Example : 
> ```
> GLACIA -xspasUse 0/0/2/0/0 // Sets up 2 X Specials on her third Pokemon 
> WHITNEY -xatksUse -2/0 // Clefairy used Metronome Charm ?!
> ```

**>>Note** : These two previous options don't override any automated stat drop/boost, such as Intimidate or Speed Boost. In consequence, if you use `-xatkUse 1` in a fight where a Gyarados with Intimidate enters, your Attack stage will be `1` for all Pokémon prior to Gyarados, and will be `0` from Gyarados onwards (unless your Pokémon has an Ability that prevents such a drop).
If you want to enforce specific stat stages, use the commands below :

- `"x<stat>Set <NUM>"` : Sets `stat` to the stage `NUM` for the entire duration of the fight.  
-- `NUM` should be an integer between `-6` and `+6`. Positive numbers can omit the `+` sign.
> Example : 
> ```
> MIKEY -xatkSet 6 // Sets Attack stage to +6 ... but is this reasonable ?
> ```

- `"x<stat>sSet <FIRST/SECOND/...>"` : Sets `stat` to the stage `FIRST` for the first Pokémon, the stage `SECOND` for the second Pokémon, etc.  
-- `FIRST`, `SECOND` ... should be integers between `-6` and `+6`. Positive numbers can omit the `+` sign.  
*(Notice the `"s"` after the `"<stat>"` which denotes the plural form.)*  
> Example : 
> ```
> LUCIAN -xspasSet 0/1/1/2/2 // Enforces the Special Attack to be at stage 0 on the first Pokémon, at 1 on the second and third Pokémon, at 2 on the fourth and fifth Pokémon
> ```  

###### 5.4.3.3. Experience
-  `"-sxp <NUM>"` : Shares earned experience, effectively dividing it by `NUM`. `NUM` must be a positive integer between `1` and `6`.  
> Example :  
> ```
> GARDENIA -sxp 2 // Divides all received EXP by 2, for example EXP. SHARE
> ```

-  `"-sxps <FIRST>/<SECOND>..."` : Divides first enemy Pokemon experience by `FIRST`, the second by `SECOND`, etc.  
--  `FIRST`, `SECOND` ... must be positive integers between `0` and `6`. A value of `0` denotes that the Pokémon doesn't battle the corresponding enemy, thus doesn't earn EXP nor EVs from it.  
> Example : 
> ```
> RED -sxps 2/1/1/1/1/1 // Shares EXP on first Pokémon by 2, then fight remaining enemies normally. Could it be Shuckie strats ?
> ```

###### 5.4.3.4. Weather
-  `"-weather <WEATHER>"` : Sets the weather `WEATHER` for the entire battle.  
-- `WEATHER` must be `NONE`, `RAIN`, `SUN`, `SANDSTORM` or`HAIL` (`NONE` can be replaced by `0`).  
*alias* : `"-w"`  
> Example : 
> ```
> DALTON -weather HAIL // Damn harsh weather !
> ``` 

-  `"-weathers <FIRST>/<SECOND>/...` Sets the weather `FIRST` for the first enemy Pokémon, weather `SECOND` for the second one, etc.  
*alias*: `-ws`  
 
> Example : 
> ```
> LORELEI -weathers HAIL/0/SUN/SUN/SUN // Hail on first Pokémon, no weather on second, Sunny Day on third onwards
> ``` 

###### 5.4.3.5. Specific battle modifiers
###### Primary status
- `"-xstatus <STATUS>"` : Sets the status `STATUS` for the entire duration of the fight.  
-- `STATUS` must be `NONE`, `SLEEP`, `POISON`, `BURN`, `FREEZE`, `PARALYSIS` or `TOXIC` (`NONE` can be replaced by `0`).  
> Example : 
> ```
> BRAWLY -xstatus BURN // Guts all the way !
> ```

- `"-xstatuses <FIRST>/<SECOND>/..."` : Sets the status `FIRST` for the first Pokémon, `SECOND` for the second Pokémon, etc.  
> Example : 
> ```
> MARS -xstatuses PSN/NONE // Poisoned on first Pokémon, healing on second
> ```

###### Secondary status and field

- `"-xstatus2 <MOD1>[+MOD2+...]"` : Sets a list of battle modifiers for the entire duration of the fight.  
-- This reads as a single modifier `MOD1` or a list of `+`-separated such modifiers.  
> Example : 
> ```
> TATEANDLIZA -ystatus2 REFLECT+LIGHTSCREEN // The enemy side sets up both screens for the whole fight
> ```

- `"-xstatuses2 <FIRST>/<SECOND>/..."` : Sets a list of battle modifiers for each Pokémon : `FIRST` for the first, `SECOND` for the second, etc.  
-- For example, `<FIRST>` is either a single modifier `MOD1` or a list of `+`-separated such modifiers.  
> Example : 
> ```
> LUCIAN -ystatuses2 REFLECT+LIGHTSCREEN/0/0/0/0 // Screens only up against the first Pokémon
> ```

Here is the exhautive list of implemented modifiers :
 Modifier | Effect
---------------- | ----------
`"REFLECT"` | Sets Reflect (damage is reduced depending on the context)
`"LIGHTSCREEN"` | Sets Lightscreen (damage is reduced depending on the context)
`"CHARGED_UP"` or `"CHARGED"` | x2 damage for Electric type moves
`"SWITCHING_OUT"` | x2 damage for Pursuit
`"UNDERWATER"` | x2 damage for Surf *(Gen 3)*
`"FLASH_FIRE"` | x1.5 damage for Fire type moves
`"MUDSPORT"` | x0.5 damage for Electric type moves
`"WATERSPORT"` | x0.5 damage for Fire type moves
`"UNBURDEN"` | x2 Speed if the item is used or lost *(Gen 4)*
`"TAILWIND"` | x2 Speed
`"FLOWER_GIFT"` | x1.5 Special Attack & Special Defense if weather is `SUN` *(Gen 4)*
`"FORESIGHT"` | Normal/Fighting type moves can hit Ghosts
`"GROUNDED"` | Allows Ground moves to hit Flying types or Pokémon with Levitate *(Gen 4)*
`"CONFUSED"` | Manually triggers self-hit damage (should be automatic)
`"WRAPPED"` or `"WRAP"` | Manually triggers trapped damage (should be automatic)
`"NIGHTMARE"` | Manually triggers Nightmare damage (should be automatic)
`"CURSED"` or `"CURSE"` | Manually triggers Curse damage (should be automatic)
`"SPIKES"` | Manually triggers spikes damage (should be automatic)
`"LEECH_SEED"` | Manually triggers Leech Seed damage (should be automatic)
	
###### Current HP
- `"-xcurrHP <VALUE>"` : Sets `VALUE` as the current HP (Health Point) value.  
-- `VALUE` must be `FULL`, `HALF`, `THIRD` or a valid integer value.  
> Example :  
> ```
> BRUNO -xcurrHP 2 // Flail strats !
> LOGAN -xcurrHP THIRD // Going for Blaze
> OSCAR -ycurrHP HALF // Let's test Wring Out if the enemy is at half 
> ``` 

###### 5.4.3.6. Double battle
- `"-doubleBattle"` : Forces the battle to be considered as a double battle with 2 player Pokémon.
- `"-singleBattle"` : Forces the battle to be considered as a single battle.
- `"-multiTargetDamage"` : Forces eligible moves to hit multiple targets.
- `"-singleTargetDamage"` : Forces all moves to hit a single target.
- `"-xpartner <PLAYER_PARTNER>"` : chooses the trainer `PLAYER_PARTNER` as the player partner for a battle.
- `"-ypartner <ENEMY_PARTNER>"` : chooses the trainer `ENEMY_PARTNER` as the enemy partner for battle. Among other things, this adds the `ENEMY_PARTNER` party to the current enemy party.

**>>Big note on double battles** : 
- Certain trainers are double battles by default. That's the case for `Tate & Liza` in Ruby/Sapphire/Emerald for example. For these battles, the tool assumes that two player Pokémon are fighting two enemy Pokémon. This means :
  1. EXP is shared by `2` by default ;
  2. All moves that have decreased damage if hitting multiple targets will undergo that decrease. Additionally, Reflect and Lightscreen multipliers are the ones for double battles with two battlers alive on the side for which screens are on.

  If one wants to mitigate any of these effects, one can :
    1. Use the `"-sxp"` or `"-sxps"` options to choose the desired experience share policy ;
    2. Use the `"-singleTargetDamage"` option ;
    3. Use the `"-order"` as will be described in the next section.

- Furthermore, some double battles are actually not doubles by default from the game data perspective. This is the case for simultaneous opponent fights (Galaxy double in Lake Verity in Gen 4) or "true" double battles (Player+Steven vs. Maxie+Tabitha in Mossdeep Space Center in Emerald, or Player+Barry vs. Mars+Jupiter in Spear Pillar in DPPt). This is where the options `"-xpartner"` and `"-ypartner"` must be used to ensure the proper behaviour. These two commands have different effects depending on if they are used separately or altogether :  
-- If `"-xpartner"` is used, then the experience is not shared and the damage are calculated with multi target policy by default.  
-- Otherwise, if only `"-ypartner"` is used, then the tool assumes the fight is of the kind of the one described at the first bullet point of this section.  
  
###### 5.4.3.7. Order
- `"-order <FIRST>[/SECOND...]"` : Switches the enemy team order.  
  -- For example, if `FIRST` represents a single enemy, then it is an integer representing the index of the enemy Pokémon in the original party order. This index must be between `1` and the maximum number of Pokémon in the enemy party. If `FIRST` represents two enemies attacked simultaneously, then it must be two indices separated with a `+`.  
  
> Example : (single battle)  
> In Diamond/Pearl, the original party order of Cynthia is the following :  
> #1 Spiritomb, #2 Roserade, #3 Gastrodon, #4 Lucario, #5 Milotic, #6 Garchomp  
> However, when leading an Infernape, Cynthia sends her party in this new order :  
> #1 Spiritomb, #3 Gastrodon, #5 Milotic, #6 Garchomp, #2 Roserade, #4 Lucario  
> Consequently, the correct command for this fight is :  
> ```
> CYNTHIA -order 1/3/5/6/2/4
> ```  

> Example : (double battle)  
> In Emerald, the original party order of Tate & Liza is the following :  
> #1 Claydol, #2 Xatu, #3 Lunatone, #4 Solrock  
> When fighting with Swampert, the perfect fight scenario involves sacking all the player Pokémon other than Swampert, then take Claydol and Xatu down simultaneously, then take Solrock and Lunatone down simultaneously. This can be achieved with the following options :  
> ```
> TATE&LIZA -order 1+2/3+4 -sxp 1
> ```  

> Example : (true battle)  
> In Platinum, the player and Barry both face Mars and Jupiter in a true double battle. The original enemy party orders are the following :  
> Mars : #1 Bronzor (M), #2 Golbat (M), #3 Purugly  
> Jupiter : #1 Bronzor (J), #2 Golbat (J), #3 Skuntank  
> Let's associate the two with the `"-ypartner"` command :  
> ```
> MARS -ypartner JUPITER -xpartner BARRY
> ```
> The new enemy party is referenced by appending Jupiter's party to Mars' :  
> #1 Bronzor (M), #2 Golbat (M), #3 Purugly, #4 Bronzor (J), #5 Golbat (J), #6 Skuntank  
> However, they lead with both Bronzor. Additionally, leading with an Empoleon, they are taken down simultaneously, then Skuntank and Purugly, then both Golbats. We end up with the following command :  
> ```
> MARS -ypartner JUPITER -xpartner BARRY -order 1+4/3+6/2+5
> ```  

###### 5.4.3.8. Multiple scenarii fights
One might want to perform the same fight with multiple options. The next option provides a way to do so without performing multiple compilations :  
- `"-backtrack"` : After the current battle is performed, backtracks to the player state prior to said battle.  
> Example :
> Let's say we want damage ranges on Cyrus with 1 X Special, then with 2 X Specials. This can be done with :  
> ```
> CYRUS -xspause 1 -v 1 -scenario CYRUS_1_X_SPA -backtrack
> CYRUS -xspause 2 -v 1 -scenario CYRUS_2_X_SPA
> ```
> Note that the scenario names are not mandatory, but they help recognizing which scenario is which, both in the route file and in the output file.

###### 5.4.3.9. Incompatible options
Some options can have incompatible behaviour : they would either cancel each other out, have contradictory effects or introduce undefined behaviour from the reader's/tool's perspective. In such cases, an error is thrown and reported both in the output file and the debug file.

-- -- 	

### 6. KNOWN ISSUES
- Hopefully not many.

#### 6.1 UNKNOWN ISSUES
- Probably a lot.
-- -- 

### 7. TODOS
- Much more than this page can ever contain.

-- -- 

### 8. CONTACT INFO AND ACKNOWLEDGEMENTS

#### 8.1. COMMUNITIES
- [`PokemonSpeedruns` Discord server (archived)](https://discord.gg/0UUw8zDe2hWlwRsm)

- [`Gen 1-3 Pokemon Speedrunning` Discord server](https://discord.gg/NjQFEkc)  

- [`DS Pokémon Speedrunning` Discord server](https://discord.gg/HqRC6ZU)  

- `Pokémon French Racing Team` Discord server (semi-private)

- [`pret` Discord server](https://discord.gg/MAhw9Uxe)

- [`SpeedRunsLive`](http://speedrunslive.com) - for inspiration/awesome races  

#### 8.2. PROGRAMMERS
Big aknowledgements to these people, who either initiated this tool or helped improve and/or maintain it at a technical level.  
- [`HRoll`](https://github.com/HRoll) [(2)](http://twitch.tv/hroll) - for making the original RouteOne which contributes a solid core to this tool
 
- [`Mountebank`](http://twitch.tv/mountebank) - for contributing to the development of the original RouteOne  

- [`Dabomstew`](https://github.com/Dabomstew) - for porting RouteOne to fit Gen 2  

- [`entrpntr`](https://github.com/entrpntr) - for the attention to details and various ideas  

- [`MKDasher`](https://twitter.com/MK_Dasher) - for providing many technical tools, mostly for DS research

- [`Grogir`](https://www.twitch.tv/grogir) - for the attention to details and various ideas 

#### 8.3. CONTRIBUTORS
People who reported errors and/or provided feedback, who made the tool better in the end. Thanks to them !  
[`BillBonzai`](https://www.twitch.tv/billbonzai), [`Gabraltar`](https://github.com/gabraltar), [`Gimmy`](https://www.twitch.tv/gimmytomas), [`Graizi`](https://www.twitch.tv/graizi), [`Grogir`](https://www.twitch.tv/grogir), [`Plot`](https://www.twitch.tv/plotwyx)

-- -- 

