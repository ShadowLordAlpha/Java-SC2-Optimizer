package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildNydusNetwork extends EcAction implements Serializable
{
	private static final int	time		= 50;
	private static final int	minerals	= 150;
	private static final int	gas			= 200;

	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= minerals;
		s.gas -= gas;
		s.drones -= 1;
		s.dronesOnMinerals -= 1;
		s.supplyUsed -= 1;
		s.addFutureAction(time, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s, " Nydus Network+1");
				s.nydusNetwork += 1;
			}
		});
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < minerals)
			return false;
		if (s.gas < gas)
			return false;
		if (s.drones < 1)
			return false;
		return true;
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.lairs == 0 && s.evolvingLairs == 0 && s.hives == 0 && s.evolvingHives == 0)
			return true;
		if (s.nydusNetwork == 2)
			return true;
		return super.isInvalid(s);
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildLair());
		return l;
	}
}
