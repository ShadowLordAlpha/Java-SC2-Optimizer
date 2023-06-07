package com.shadowcs.optimizer.sc2data.evo.action.upgrade;

import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildExtractor;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSpawningPool;

public class EcActionUpgradeMetabolicBoost extends EcActionUpgrade
{
	@Override
	public void init()
	{
		init(100, 100, 110, "Metabolic Boost");
	}

	@Override
	public void execute(EcBuildOrder s, EcEvolver e)
	{
		// TODO Auto-generated method stub
		super.execute(s, e);
		s.spawningPoolsInUse++;
	}
	
	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.spawningPools-s.spawningPoolsInUse == 0)
			return true;
		return false;
	}

	@Override
	public void afterTime(EcBuildOrder s, EcEvolver e)
	{
		s.metabolicBoost = true;
		s.spawningPoolsInUse--;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildSpawningPool());
		l.add(new EcActionBuildExtractor());
		return l;
	}
}