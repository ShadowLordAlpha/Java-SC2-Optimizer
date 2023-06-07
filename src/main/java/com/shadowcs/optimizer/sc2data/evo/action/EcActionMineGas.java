package com.shadowcs.optimizer.sc2data.evo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcSettings;
import com.shadowcs.optimizer.sc2data.evo.EcState;

public class EcActionMineGas extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		if (EcSettings.pullThreeWorkersOnly) 
		{
			s.dronesGoingOnGas += 3;
			s.dronesOnMinerals -= 3;
		}
		else
		{
			s.dronesGoingOnGas += 1;
			s.dronesOnMinerals -= 1;
		}
		s.addFutureAction(2, new Runnable()
		{
			@Override
			public void run()
			{
				if (EcSettings.pullThreeWorkersOnly) 
				{
					if (e.debug)
						e.mining(s," +3 on gas");
					s.dronesGoingOnGas -= 3;
					s.dronesOnGas += 3;
				}
				else
				{
					if (e.debug)
						e.mining(s," +1 on gas");
					s.dronesGoingOnGas--;
					s.dronesOnGas++;
				}

			}
		});
	}
	
	@Override
	public boolean isInvalid(EcBuildOrder s) {
		return !isPossible(s);
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if ((s.dronesOnGas+s.dronesGoingOnGas) >= 3*s.gasExtractors)
			return false;
		if (s.dronesOnMinerals == 0)
			return false;
		return true;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		return l;
	}

}