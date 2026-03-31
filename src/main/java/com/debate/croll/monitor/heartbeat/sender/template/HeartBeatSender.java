package com.debate.croll.monitor.heartbeat.sender.template;

import com.debate.croll.monitor.heartbeat.scheduler.HeartBeatScheduler;

public interface HeartBeatSender {

	void sendHeartbeat(HeartBeatScheduler heartBeatScheduler);

}
