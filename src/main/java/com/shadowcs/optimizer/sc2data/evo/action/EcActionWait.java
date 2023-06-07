package com.shadowcs.optimizer.sc2data.evo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;

public class EcActionWait extends EcAction implements Serializable
{
	boolean	go	= false;

	@Override
	public void execute(EcBuildOrder s, EcEvolver e)
	{
		s.waits += 1;
	}

	@Override
	public boolean canExecute(EcBuildOrder s)
	{
		if (isPossible(s))
			return true;
		s.seconds += 1;
		Collection<Runnable> futureActions = s.getFutureActions(s.seconds);
		if (futureActions != null)
			for (Runnable r : futureActions)
			{
				r.run();
				go = true;
			}
		s.accumulateMaterials();
		return false;
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.nothingGoingToHappen(s.seconds))
			return true;
		return super.isInvalid(s);
	}
	
	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		return go;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		return l;
	}

}
