package com.shadowcs.optimizer.sc2data.evo.action.upgrade;

import java.util.ArrayList;
import java.util.List;

import com.shadowcs.optimizer.sc2data.evo.EcBuildOrder;
import com.shadowcs.optimizer.sc2data.evo.EcEvolver;
import com.shadowcs.optimizer.sc2data.evo.EcState;
import com.shadowcs.optimizer.sc2data.evo.action.EcAction;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSpire;

public class EcActionUpgradeFlyerArmor2 extends EcActionUpgrade
{
	@Override
	public void init()
	{
		init(225, 225, 190, "Flyer Armor +2");
	}

	@Override
	public boolean isInvalid(EcBuildOrder s)
	{
		if (s.spire == 0)
			return true;
		if (s.lairs == 0 && s.hives == 0 && s.evolvingLairs == 0 && s.evolvingHives == 0)
			return true;
		if (s.flyerArmor1 == false)
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
		s.flyerArmor2 = true;
		s.spiresInUse--;
	}

	@Override
	public List<EcAction> requirements(EcState destination)
	{
		ArrayList<EcAction> l = new ArrayList<EcAction>();
		l.add(new EcActionBuildSpire());
		l.add(new EcActionUpgradeFlyerArmor1());
		return l;
	}
}