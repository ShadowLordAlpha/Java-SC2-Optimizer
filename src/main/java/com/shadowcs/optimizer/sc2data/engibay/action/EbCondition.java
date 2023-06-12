package com.shadowcs.optimizer.sc2data.engibay.action;

/**
 * A Condition represents the needed part for an action to happen. This can be many different types represented by the
 * type set of the condition and set by the data.
 *
 * @param type What type is the data field
 * @param data  The specific data, could be an amount or a unit of some kind or even an upgrade
 */
public record EbCondition(EbConditionType type, double data) {

}
