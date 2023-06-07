package com.shadowcs.optimizer.sc2data.evo.action.upgrade;

import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildHive;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSpire;

public class EcActionUpgradeFlyerAttacks3 extends EcActionUpgrade
{
	@Override
	public void init()
	{
		init(250, 250, 220, "Flyer Attacks +3");
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.spire == 0)
			return true;
		if (s.hives == 0 && s.evolvingHives == 0)
			return true;
		if (s.flyerAttack2 == false)
			return true;
		return false;
	}

	@Override
	public void execute(EcBuildOrder s, EcEvolver e)
	{
		super.execute(s, e);
		s.spiresInUse++;
	}

	@Override
	public boolean isPossible(EcBuildOrder s)
	{
		if (s.spiresInUse == s.spire)
			return false;
		return super.isPossible(s);
	}

	@Override
	public void afterTime(EcBuildOrder s, EcEvolver e)
	{
		s.flyerAttack3 = true;
		s.spiresInUse--;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildSpire());
		l.add(new EcActionBuildHive());
		l.add(new EcActionUpgradeFlyerAttacks2());
		return l;
	}
}