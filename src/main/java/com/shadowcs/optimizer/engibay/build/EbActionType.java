package com.shadowcs.optimizer.engibay.build;

import com.google.gson.annotations.SerializedName;

/**
 * These represent the different types of actions that we are able to do.
 */
public enum EbActionType {

    @SerializedName("structure") STRUCTURE,
    @SerializedName("action") ACTION,
    @SerializedName("worker") WORKER, // This can be a Unit build or a morph (because zerg is strange...)
    @SerializedName("unit") UNIT,
    @SerializedName("morph") MORPH,
    @SerializedName("upgrade") UPGRADE
}
