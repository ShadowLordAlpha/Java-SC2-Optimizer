package com.shadowcs.optimizer.sc2data.evo;

import java.util.Map;

import com.shadowcs.optimizer.sc2data.evo.action.EcAction;
import com.shadowcs.optimizer.sc2data.evo.action.EcActionExtractorTrick;
import com.shadowcs.optimizer.sc2data.evo.action.EcActionWait;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildBaneling;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildBanelingNest;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildBroodLord;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildCorruptor;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildDrone;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildEvolutionChamber;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildExtractor;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildGreaterSpire;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildHatchery;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildHive;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildHydralisk;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildHydraliskDen;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildInfestationPit;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildInfestor;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildLair;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildMutalisk;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildNydusNetwork;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildNydusWorm;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildOverlord;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildOverseer;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildQueen;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildRoach;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildRoachWarren;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSpawningPool;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSpineCrawler;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSpire;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildSporeCrawler;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildUltralisk;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildUltraliskCavern;
import com.shadowcs.optimizer.sc2data.evo.action.build.EcActionBuildZergling;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeAdrenalGlands;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeBurrow;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeCarapace1;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeCarapace2;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeCarapace3;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeCentrifugalHooks;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeChitinousPlating;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeFlyerArmor1;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeFlyerArmor2;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeFlyerArmor3;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeFlyerAttacks1;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeFlyerAttacks2;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeFlyerAttacks3;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeGlialReconstitution;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeGroovedSpines;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMelee1;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMelee2;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMelee3;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMetabolicBoost;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMissile1;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMissile2;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeMissile3;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeNeuralParasite;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradePathogenGlands;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradePneumatizedCarapace;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeTunnelingClaws;
import com.shadowcs.optimizer.sc2data.evo.action.upgrade.EcActionUpgradeVentralSacs;

public class EcRequirementTree
{
	static int max;
	public static void execute(EcState destination)
	{
		max = 0;
		Map<Integer, Class> actions = EcAction.actions;
		actions.clear();

//		add(actions,new EcActionDoNothing(),destination);
		add(actions,new EcActionWait(), destination);
		add(actions,new EcActionBuildQueen(), destination);
		add(actions,new EcActionBuildDrone(), destination);
		if (EcSettings.useExtractorTrick)
			add(actions,new EcActionExtractorTrick(), destination);
		add(actions,new EcActionBuildHatchery(), destination);
		add(actions,new EcActionBuildOverlord(), destination);
		add(actions,new EcActionBuildSpawningPool(), destination);
		
		actions(destination, actions);

		for (Class a : actions.values())
			System.out.println(a.getSimpleName());

	}

	private static void actions(EcState destination, Map<Integer, Class> actions)
	{
		if (destination.adrenalGlands)
			add(actions,new EcActionUpgradeAdrenalGlands(), destination);
		if (destination.armor1)
			add(actions,new EcActionUpgradeCarapace1(), destination);
		if (destination.armor2)
			add(actions,new EcActionUpgradeCarapace2(), destination);
		if (destination.armor3)
			add(actions,new EcActionUpgradeCarapace3(), destination);
		if (destination.burrow)
			add(actions,new EcActionUpgradeBurrow(), destination);
		if (destination.centrifugalHooks)
			add(actions,new EcActionUpgradeCentrifugalHooks(), destination);
		if (destination.chitinousPlating)
			add(actions,new EcActionUpgradeChitinousPlating(), destination);
		if (destination.flyerArmor1)
			add(actions,new EcActionUpgradeFlyerArmor1(), destination);
		if (destination.flyerArmor2)
			add(actions,new EcActionUpgradeFlyerArmor2(), destination);
		if (destination.flyerArmor3)
			add(actions,new EcActionUpgradeFlyerArmor3(), destination);
		if (destination.flyerAttack1)
			add(actions,new EcActionUpgradeFlyerAttacks1(), destination);
		if (destination.flyerAttack2)
			add(actions,new EcActionUpgradeFlyerAttacks2(), destination);
		if (destination.flyerAttack3)
			add(actions,new EcActionUpgradeFlyerAttacks3(), destination);
		if (destination.glialReconstitution)
			add(actions,new EcActionUpgradeGlialReconstitution(), destination);
		if (destination.groovedSpines)
			add(actions,new EcActionUpgradeGroovedSpines(), destination);
		if (destination.melee1)
			add(actions,new EcActionUpgradeMelee1(), destination);
		if (destination.melee2)
			add(actions,new EcActionUpgradeMelee2(), destination);
		if (destination.melee3)
			add(actions,new EcActionUpgradeMelee3(), destination);
		if (destination.metabolicBoost)
			add(actions,new EcActionUpgradeMetabolicBoost(), destination);
		if (destination.missile1)
			add(actions,new EcActionUpgradeMissile1(), destination);
		if (destination.missile2)
			add(actions,new EcActionUpgradeMissile2(), destination);
		if (destination.missile3)
			add(actions,new EcActionUpgradeMissile3(), destination);
		if (destination.neuralParasite)
			add(actions,new EcActionUpgradeNeuralParasite(), destination);
		if (destination.pathogenGlands)
			add(actions,new EcActionUpgradePathogenGlands(), destination);
		if (destination.pneumatizedCarapace)
			add(actions,new EcActionUpgradePneumatizedCarapace(), destination);
		if (destination.tunnelingClaws)
			add(actions,new EcActionUpgradeTunnelingClaws(), destination);
		if (destination.ventralSacs)
			add(actions,new EcActionUpgradeVentralSacs(), destination);
		if (destination.gasExtractors>0)
			add(actions,new EcActionBuildExtractor(), destination);
		if (destination.banelingNest > 0)
			add(actions,new EcActionBuildBanelingNest(), destination);
		if (destination.banelings > 0)
			add(actions,new EcActionBuildBaneling(), destination);
		if (destination.broodlords> 0)
			add(actions,new EcActionBuildBroodLord(), destination);
		if (destination.corruptors > 0)
			add(actions,new EcActionBuildCorruptor(), destination);
		if (destination.greaterSpire> 0)
			add(actions,new EcActionBuildGreaterSpire(), destination);
		if (destination.hives> 0)
			add(actions,new EcActionBuildHive(), destination);
		if (destination.hydraliskDen> 0)
			add(actions,new EcActionBuildHydraliskDen(), destination);
		if (destination.hydralisks> 0)
			add(actions,new EcActionBuildHydralisk(), destination);
		if (destination.infestationPit> 0)
			add(actions,new EcActionBuildInfestationPit(), destination);
		if (destination.infestors> 0)
			add(actions,new EcActionBuildInfestor(), destination);
		if (destination.lairs > 0)
			add(actions,new EcActionBuildLair(), destination);
		if (destination.mutalisks> 0)
			add(actions,new EcActionBuildMutalisk(), destination);
		if (destination.roaches> 0)
			add(actions,new EcActionBuildRoach(), destination);
		if (destination.roachWarrens > 0)
			add(actions,new EcActionBuildRoachWarren(), destination);
		if (destination.evolutionChambers > 0)
			add(actions,new EcActionBuildEvolutionChamber(), destination);
		if (destination.spire > 0)
			add(actions,new EcActionBuildSpire(), destination);
		if (destination.ultraliskCavern > 0)
			add(actions,new EcActionBuildUltraliskCavern(), destination);
		if (destination.ultralisks > 0)
			add(actions,new EcActionBuildUltralisk(), destination);
		if (destination.zerglings > 0)
			add(actions,new EcActionBuildZergling(), destination);
		if (destination.spineCrawlers > 0)
			add(actions,new EcActionBuildSpineCrawler(), destination);
		if (destination.sporeCrawlers > 0)
			add(actions,new EcActionBuildSporeCrawler(), destination);
		if (destination.overseers > 0)
			add(actions,new EcActionBuildOverseer(), destination);
		if (destination.nydusNetwork > 0)
			add(actions,new EcActionBuildNydusNetwork(), destination);
		if (destination.nydusWorm > 0)
			add(actions,new EcActionBuildNydusWorm(), destination);
		for (EcState s : destination.waypoints)
			actions(s,actions);
	}

	private static void add(Map<Integer, Class> actions, EcAction action, EcState destination)
	{
		if (!actions.containsValue(action.getClass()))
		{
			actions.put(max++, action.getClass());
			for (EcAction a : action.requirements(destination))
			{
				add(actions, a, destination);
			}
		}
	}
}
