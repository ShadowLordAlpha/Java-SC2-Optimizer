package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildRoach extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 75;
		s.gas -= 25;
		s.consumeLarva(e);
		s.supplyUsed += 2;
		s.addFutureAction(27, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s," Roach+1");
				s.roaches += 1;
			}
		});
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.roachWarrens == 0)
			return true;
		return false;
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < 75)
			return false;
		if (s.gas < 25)
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
		l.add(new EcActionBuildRoachWarren());
		l.add(new EcActionBuildExtractor());
		destination.roachWarrens = Math.max(destination.roachWarrens,1);
		return l;
	}
}
