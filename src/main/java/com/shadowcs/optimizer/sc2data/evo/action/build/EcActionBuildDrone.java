package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;

public class EcActionBuildDrone extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 50;
		s.consumeLarva(e);
		s.supplyUsed += 1;
		s.addFutureAction(17, new Runnable()
		{
			@Override
			public void run()
			{
				if (e.debug)
					e.obtained(s," Drone+1");
				s.drones += 1;
				s.dronesGoingOnMinerals += 1;
			}
		});
		s.addFutureAction(19, new Runnable()
		{
			@Override
			public void run()
			{
//				if (e.debug)
//					e.mining(s," +1 on mineral");
				s.dronesGoingOnMinerals--;
				s.dronesOnMinerals++;
			}
		});
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.minerals >= 50 && !s.hasSupply(1))
			return true;
		return super.isInvalid(s);
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
		return new ArrayList<EcAction>();
	}

}
