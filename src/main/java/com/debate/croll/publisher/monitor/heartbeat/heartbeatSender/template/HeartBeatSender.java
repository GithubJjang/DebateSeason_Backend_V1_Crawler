package com.debate.croll.publisher.monitor.heartbeat.heartbeatSender.template;

import com.debate.croll.publisher.monitor.heartbeat.HeartBeatScheduler;

public interface HeartBeatSender {

	void sendHeartbeat(HeartBeatScheduler heartBeatScheduler);

}
