package com.shadowcs.optimizer.sc2data.evo;

import com.shadowcs.optimizer.sc2data.evo.fitness.EcEconFitness;
import com.shadowcs.optimizer.sc2data.evo.fitness.EcFitness;
import com.shadowcs.optimizer.sc2data.evo.fitness.EcFitnessType;
import com.shadowcs.optimizer.sc2data.evo.fitness.EcStandardFitness;

public class EcSettings
{
	public static boolean workerParity = false;
	public static boolean overDrone = false;
	public static boolean useExtractorTrick = true;
	public static boolean pullWorkersFromGas = true;
	public static boolean pullThreeWorkersOnly = false;
	public static EcFitnessType fitnessType = EcFitnessType.STANDARD;
	public static int minimumPoolSupply = 2;
	public static int minimumExtractorSupply = 2;
	public static int minimumHatcherySupply = 2;

	
	private static EcFitness ff;
	
	public static EcFitness getFitnessFunction() {
		
		if(ff != null)
			return ff;
		
		switch(fitnessType) {
		case STANDARD:
			ff = new EcStandardFitness();
			break;
		case ECON:
			ff = new EcEconFitness();
			break;
		default:
			ff = new EcStandardFitness();
			break;
		}
		
		return ff;
		
	}
	
}
