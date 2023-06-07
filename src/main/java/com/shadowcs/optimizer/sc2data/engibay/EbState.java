package com.shadowcs.optimizer.sc2data.engibay;

import gnu.trove.TIntIntHashMap;
import io.netty.util.collection.IntObjectHashMap;

import java.util.Map;

/**
 * This class represents our current state and is one of several "types" Specifically the starting state, our simulation
 * state, or out goal state. Each state may also have a list of waypoints that they are supposed to hit and are
 * considered to be part of that state instead of their own standalone state.
 */
public class EbState {

    private TIntIntHashMap unitCountMap = new TIntIntHashMap();
}
