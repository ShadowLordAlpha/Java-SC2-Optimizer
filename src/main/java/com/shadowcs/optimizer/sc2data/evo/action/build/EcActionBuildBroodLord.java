package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildBroodLord extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 150;
		s.gas -= 150;
		s.corruptors -= 1;
		s.supplyUsed += 2;
		s.addFutureAction(34, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s," Brood Lord+1");
				s.broodlords += 1;
			}
		});
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < 150)
			return false;
		if (s.gas < 150)
			return false;
		if (s.corruptors < 1)
			return false;
		if (!s.hasSupply(2))
			return false;
		return true;
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.hives == 0 && s.evolvingHives == 0)
			return true;
		if (s.greaterSpire == 0)
			return true;
		if (!s.hasSupply(2))
			return true;
		return super.isInvalid(s);
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildGreaterSpire());
		l.add(new EcActionBuildCorruptor());
		destination.hives = Math.max(destination.hives,1);
		destination.greaterSpire = Math.max(destination.greaterSpire,1);
		return l;
	}
}
