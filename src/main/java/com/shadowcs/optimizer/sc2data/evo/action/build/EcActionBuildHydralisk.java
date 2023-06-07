package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildHydralisk extends EcAction implements Serializable
{

	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 100;
		s.gas -= 50;
		s.consumeLarva(e);
		s.supplyUsed += 2;
		s.addFutureAction(33, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s," Hydralisk+1");
				s.hydralisks += 1;
			}
		});
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.hydraliskDen == 0)
			return true;
		return false;
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < 100)
			return false;
		if (s.gas < 50)
			return false;
		if (s.larva < 1)
			return false;
		if (!s.hasSupply(2))
			return false;
		return true;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildHydraliskDen());
		destination.hydraliskDen = Math.max(destination.hydraliskDen,1);
		return l;
	}
}
