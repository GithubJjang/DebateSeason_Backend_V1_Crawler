package com.debate.croll.producer.monitor.heartbeat.sender.template;

import com.debate.croll.producer.monitor.heartbeat.scheduler.HeartBeatScheduler;

public interface HeartBeatSender {

	void sendHeartbeat(HeartBeatScheduler heartBeatScheduler);

}
