package com.objectedge.gs.YellowFlags.alert;

public interface Alert extends Runnable {

    void run();
    String getId();
    String getSchedule();

}
