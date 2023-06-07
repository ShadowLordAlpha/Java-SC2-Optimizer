package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildLair extends EcAction implements Serializable
{

	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 150;
		s.gas -= 100;
		s.hatcheries -= 1;
		s.evolvingHatcheries += 1;
		s.addFutureAction(80, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s," Lairs+1");
				s.lairs += 1;
				s.evolvingHatcheries -= 1;
			}
		});
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < 150)
			return false;
		if (s.gas < 100)
			return false;
		if (s.hatcheries <= s.queensBuilding)
			return false;
		return true;
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.hatcheries == 0)
			return true;
		if (s.spawningPools == 0)
			return true;
		return super.isInvalid(s);
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildSpawningPool());
		l.add(new EcActionBuildExtractor());
		destination.gasExtractors = Math.max(destination.gasExtractors,1);
		return l;
	}

}
