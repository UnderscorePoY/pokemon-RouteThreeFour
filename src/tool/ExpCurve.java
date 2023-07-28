package tool;
public enum ExpCurve {
    FLUCTUATING, SLOW, MEDIUM_SLOW, MEDIUM_FAST, FAST, ERRATIC, NONE;
    
    public static int expToNextLevel(ExpCurve curve, int currLevel, int totalExp) {
        if (curve == NONE || currLevel >= Pokemon.MAX_LEVEL)
            return 0;
        
        int nextExp = lowestExpForLevel(curve, currLevel + 1);
        
        return nextExp - totalExp;
    }
    
    public static int lowestExpForLevel(ExpCurve curve, int level) {
    	if(level > Pokemon.MAX_LEVEL)
    		level = Pokemon.MAX_LEVEL;
    	
        int n = level;
        int exp = 0;
        switch(curve) {
        case FLUCTUATING:
        	if (level < 15)
        		exp = n*n*n*(((n+1)/3)+24) / 50;
        	else if (level < 36)
        		exp = n*n*n*(n+14) / 50;
        	else
        		exp = n*n*n*((n/2)+32) / 50;
        	break;
        	
        case SLOW:
            exp = 5*n*n*n/4;
            break;
            
        case MEDIUM_SLOW:
            exp = 6*n*n*n/5 - 15*n*n + 100*n - 140;
            break;
            
        case MEDIUM_FAST:
            exp = n*n*n;
            break;
            
        case FAST:
            exp = 4*n*n*n/5;
            break;
            
        case ERRATIC:
        	if (level < 50)
        		exp = n*n*n*(100-n) / 50;
        	else if (level < 68)
        		exp = n*n*n*(150-n) / 100;
        	else if (level < 98)
        		exp = n*n*n*((1911-10*n) / 3) / 500;
        	else
        		exp = n*n*n*(160-n) / 100;
        	break;
        	
        default:
            break;
        }
        return exp;
    }
    
    /**
     * Returns the experience needed to reach next level for the given experience curve.
     */
    public static int expForLevel(ExpCurve curve, int level) {
        return lowestExpForLevel(curve, level + 1) - lowestExpForLevel(curve, level);
    }
}
