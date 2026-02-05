package com.debate.croll.transfer;

import org.junit.jupiter.api.Test;

import com.debate.croll.producer.monitor.sseEmitter.session.Session;

public class OffsetBasedDeltaTransferTestV2 {

	@Test
	public void offsetBasedDeltaTransfer() throws InterruptedException {

		//
		SessionContainerV2 sessionContainerV2 = new SessionContainerV2();

		//
		String userId = "userId" + 1;
		Session session = new Session(null,0,0);

		sessionContainerV2.addSession(userId,session);

		//
		Thread.sleep(15000);


		System.out.println("현재 offset은 :"+session.getProgressLogOffset());







	}

}
