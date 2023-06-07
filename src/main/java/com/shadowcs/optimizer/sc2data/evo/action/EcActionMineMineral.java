package com.shadowcs.optimizer.sc2data.evo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcSettings;
import com.shadowcs.optimizer.sc2data.evo.EcState;

public class EcActionMineMineral extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		if (EcSettings.pullThreeWorkersOnly) 
		{
			s.dronesGoingOnMinerals += 3;
			s.dronesOnGas -= 3;
		}
		else
		{
			s.dronesGoingOnMinerals += 1;
			s.dronesOnGas -= 1;
		}
		s.addFutureAction(2, new Runnable()
		{
			@Override
			public void run()
			{
				if (EcSettings.pullThreeWorkersOnly) 
				{
					if (e.debug)
						e.mining(s," +3 on minerals");
					s.dronesGoingOnMinerals -= 3;
					s.dronesOnMinerals += 3;
				}
				else
				{
					if (e.debug)
						e.mining(s," +1 on minerals");
					s.dronesGoingOnMinerals--;
					s.dronesOnMinerals++;
				}

			}
		});
	}
	
	@Override
	public boolean isInvalid(EcBuildOrder s) {
		if (s.dronesOnGas != 0)
			return false;
		return true;
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.dronesOnGas != 0)
			return true;
		return false;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		return l;
	}

}