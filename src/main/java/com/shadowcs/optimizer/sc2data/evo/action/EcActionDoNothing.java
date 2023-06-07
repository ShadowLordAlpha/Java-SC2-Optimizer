package com.shadowcs.optimizer.sc2data.evo.action;

import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;

public class EcActionDoNothing extends EcAction
{

	@Override
	public void execute(EcBuildOrder s, EcEvolver e)
	{
		
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		return true;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		return new ArrayList<EcAction>();
	}

}
