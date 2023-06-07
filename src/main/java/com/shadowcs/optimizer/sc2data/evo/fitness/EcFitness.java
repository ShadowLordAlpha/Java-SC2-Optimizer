package com.shadowcs.optimizer.sc2data.evo.fitness;

import com.shadowcs.optimizer.sc2data.evo.EcState;

public interface EcFitness {

	//public double augmentScore(EcState current, EcState desitnation, double score, boolean waypoint);
	public double score(EcState candidate, EcState metric);
	
	
}
