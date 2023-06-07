package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildZergling extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 50;
		s.consumeLarva(e);
		s.supplyUsed += 1;
		s.addFutureAction(24, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s," Zergling+2");
				s.zerglings += 2;
			}
		});
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.spawningPools == 0)
			return true;
		if (s.minerals >= 50 && !s.hasSupply(1))
			return true;
		return false;
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < 50)
			return false;
		if (s.larva < 1)
			return false;
		if (!s.hasSupply(1))
			return false;
		return true;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildSpawningPool());
		return l;
	}
}
