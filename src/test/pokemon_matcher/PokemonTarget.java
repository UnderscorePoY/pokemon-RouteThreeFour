package test.pokemon_matcher;

import java.util.HashSet;
import java.util.Set;

import tool.Ability;
import tool.Gender;
import tool.Item;
import tool.Move;
import tool.Nature;
import tool.Pokemon;
import tool.Species;
import tool.Stat;

public class PokemonTarget {
    private Species species;
    private Gender gender;
    private Integer level;
    private Nature nature;
    private Ability ability;
    private Integer hp, atk, def, spa, spd, spe;
    private Integer hpIV, atkIV, defIV, spaIV, spdIV, speIV;
    
    private Set<Move> moves;
    private Item heldItem;
    private Integer happiness;
    
    public PokemonTarget(String speciesName, String genderName, Integer level, String natureName, String abilityName,
    		Integer hp, Integer atk, Integer def, Integer spa, Integer spd, Integer spe,
    		Integer hpIV, Integer atkIV, Integer defIV, Integer spaIV, Integer spdIV, Integer speIV,
    		String itemName, Integer happiness, String... moveNames) throws Exception {
    	species = Species.getSpeciesByName(speciesName);
    	if(species == null)
    		throw new Exception(String.format("No species '%s'.", speciesName));
    	gender = Gender.getGenderFromStr(genderName);
    	if(gender == null)
    		throw new Exception(String.format("No gender '%s'.", genderName));
    	this.level = level;
    	if(level == null)
    		throw new Exception(String.format("No level '%s'.", level));
    	nature = Nature.getNatureFromString(natureName);
    	if(nature == null)
    		throw new Exception(String.format("No nature '%s'.", natureName));
    	ability = Ability.getAbilityFromString(abilityName);
    	if(ability == null)
    		throw new Exception(String.format("No ability '%s'.", abilityName));
    	
    	this.hp = hp;
    	this.atk = atk;
    	this.def = def;
    	this.spa = spa;
    	this.spd = spd;
    	this.spe = spe;
    	this.hpIV = hpIV;
    	this.atkIV = atkIV;
    	this.defIV = defIV;
    	this.spaIV = spaIV;
    	this.spdIV = spdIV;
    	this.speIV = speIV;
    	
    	if(itemName != null) {
    		heldItem = Item.getItemByName(itemName);
    		if(heldItem == null)
	    		throw new Exception(String.format("No item '%s'.", itemName));
    	}
    	
    	this.happiness = happiness;
    	if(moveNames.length > 0) {
    		moves = new HashSet<>();
	    	for(String moveName : moveNames) {
	    		Move move = Move.getMoveByName(moveName);
		    	if(move == null)
		    		throw new Exception(String.format("No move '%s'.", moveName));
	    		moves.add(move);
	    	}
    	}
    }
    
    public PokemonTarget(String speciesName, String genderName, Integer level, String natureName, String abilityName,
    		Integer hp, Integer atk, Integer def, Integer spa, Integer spd, Integer spe,
    		Integer fixedIV,
    		String itemName, Integer happiness, String... moveNames) throws Exception {
    	this(speciesName, genderName, level, natureName, abilityName,
    		hp, atk, def, spa, spd, spe,
    		fixedIV, fixedIV, fixedIV, fixedIV, fixedIV, fixedIV,
    		itemName, happiness, moveNames);
    }
    
    
    /**
     * Returns null if the test Pokémon matches the actual Pokémon. 
     * Otherwise, returns a descriptive object of the first encountered difference.
     */
    public MatchResult tryMatch(Pokemon p) {
    	// Mandatory comparisons
    	if(species != p.getSpecies())
    		return new MatchResult(null, species.getDisplayName(), "species", species, p.getSpecies());
    	if(gender != p.getGender())
    		return new MatchResult(null, species.getDisplayName(), "gender", gender, p.getGender());
    	if(level != p.getLevel())
    		return new MatchResult(null, species.getDisplayName(), "level", level, p.getLevel());
    	if(nature != p.getNature())
    		return new MatchResult(null, species.getDisplayName(), "nature", nature, p.getNature());
    	if(ability != p.getAbility())
    		return new MatchResult(null, species.getDisplayName(), "ability", ability, p.getAbility());
    	
    	// Optional comparisons
    	if(hp != null && hp != p.getStatValue(Stat.HP))
    		return new MatchResult(null, species.getDisplayName(), "hp", hp, p.getStatValue(Stat.HP));
    	if(atk != null && atk != p.getStatValue(Stat.ATK))
    		return new MatchResult(null, species.getDisplayName(), "atk", atk, p.getStatValue(Stat.ATK));
    	if(def != null && def != p.getStatValue(Stat.DEF))
    		return new MatchResult(null, species.getDisplayName(), "def", def, p.getStatValue(Stat.DEF));
    	if(spa != null && spa != p.getStatValue(Stat.SPA))
    		return new MatchResult(null, species.getDisplayName(), "spa", spa, p.getStatValue(Stat.SPA));
    	if(spd != null && spd != p.getStatValue(Stat.SPD))
    		return new MatchResult(null, species.getDisplayName(), "spd", spd, p.getStatValue(Stat.SPD));
    	if(spe != null && spe != p.getStatValue(Stat.SPE))
    		return new MatchResult(null, species.getDisplayName(), "spe", spe, p.getStatValue(Stat.SPE));
    	
    	if(hpIV != null && hpIV != p.getIVs().get(Stat.HP))
    		return new MatchResult(null, species.getDisplayName(), "hpIV", hpIV, p.getIVs().get(Stat.HP));
    	if(atkIV != null && atkIV != p.getIVs().get(Stat.ATK))
    		return new MatchResult(null, species.getDisplayName(), "atkIV", atkIV, p.getIVs().get(Stat.ATK));
    	if(defIV != null && defIV != p.getIVs().get(Stat.DEF))
    		return new MatchResult(null, species.getDisplayName(), "defIV", defIV, p.getIVs().get(Stat.DEF));
    	if(spaIV != null && spaIV != p.getIVs().get(Stat.SPA))
    		return new MatchResult(null, species.getDisplayName(), "spaIV", spaIV, p.getIVs().get(Stat.SPA));
    	if(spdIV != null && spdIV != p.getIVs().get(Stat.SPD))
    		return new MatchResult(null, species.getDisplayName(), "spdIV", spdIV, p.getIVs().get(Stat.SPD));
    	if(speIV != null && speIV != p.getIVs().get(Stat.SPE))
    		return new MatchResult(null, species.getDisplayName(), "speIV", speIV, p.getIVs().get(Stat.SPE));
    	
    	if(happiness != null && happiness != p.getHappiness())
    		return new MatchResult(null, species.getDisplayName(), "happiness", happiness,  p.getHappiness());
    	
    	if(heldItem != null && heldItem != p.getHeldItem())
    		return new MatchResult(null, species.getDisplayName(), "item", heldItem,  p.getHeldItem());
    	
    	if(moves != null && !p.getMoveset().getSetView().equals(moves))
    		return new MatchResult(null, species.getDisplayName(), "moves", moves, p.getMoveset().getSetView());
    	
		return new MatchResult(null, species.getDisplayName()); // Valid
    }

    /*
    public void testMatches(Pokemon p) {
    	if(this.matches(p))
    		System.out.println(String.format("Succesfully matched %s's %s.", trainerName, p.getDisplayName()));
		else
    		System.out.println(String.format("No match for %s's %s.", trainerName, p.getDisplayName()));	
    }
    */
}