package com.shadowcs.optimizer.sc2data.evo.action.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcSettings;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;
import com.shadowcs.optimizer.sc2data.evo.action.EcActionMineGas;
import com.shadowcs.optimizer.sc2data.evo.action.EcActionMineMineral;

public class EcActionBuildExtractor extends EcAction implements Serializable
{
	@Override
	public void execute(final EcBuildOrder s, final EcEvolver e)
	{
		s.minerals -= 25;
		s.drones -= 1;
		s.dronesOnMinerals -= 1;
		s.supplyUsed -= 1;
		s.extractorsBuilding++;
		s.addFutureAction(30, new Runnable()
		{
			@Override
			public void run()
			{
				if (s.extractorsBuilding == 0)
					return;
				if (e.debug)
					e.obtained(s, " Extractor+1");
				s.gasExtractors += 1;
				if (EcSettings.pullWorkersFromGas == false)
				{
					s.dronesOnMinerals -= 3;
					s.dronesOnGas += 3;
				}
				s.extractorsBuilding--;
			}
		});
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.gasExtractors + s.extractorsBuilding >= s.extractors())
			return true;
		if(s.supplyUsed < EcSettings.minimumExtractorSupply)
			return true;
		return false;
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.minerals < 25)
			return false;
		if (s.drones < 1)
			return false;
		return true;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		if (EcSettings.pullWorkersFromGas)
		{
			l.add(new EcActionMineGas());
			l.add(new EcActionMineMineral());
		}
		return l;
	}

}
