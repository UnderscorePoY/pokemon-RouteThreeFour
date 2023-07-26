package tool;

/**
 * Used for seeding trainers in Gen 4. Despite calling LCRandom(), the trainer generation is deterministic.
 */
public class Random {
	static int seed;
	
	public static void SetLCRNGSeed(int seed) {
		Random.seed =  seed & 0xFFFFFFFF;
	}

	// https://github.com/pret/pokediamond/blob/a96b2520c1cf04511cd375d7203d6dd402f05d3e/arm9/src/math_util.c#L610-L616
	public static int LCRandom() {
		seed = (seed * 0x41C64E6D) & 0xFFFFFFFF;
		seed = (seed + 0x6073) & 0xFFFFFFFF;
	    return ((int)(seed >>> 16)) & 0x0000FFFF; // java int is signed, so replacing the division with an unsigned bitshift to make a correct division
	}
}
