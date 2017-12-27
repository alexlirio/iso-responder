package com.company.responder.commutator;

import java.io.Serializable;

import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;


public class SwitchTransaction extends QBeanSupport implements GroupSelector {

	public int prepare(long id, Serializable context) {
		return PREPARED;
	}

	public void commit(long id, Serializable context) {
		log.info("operation=commit");
	}

	public void abort(long id, Serializable context) {
		log.info(" - - - - -  abort SwitchTransaction");
	}

	public String select(long id, Serializable context) {
		((Context) context).checkPoint(" - - - - -  select SwitchTransaction");
		// For now, we only have this group
		log.info("status=responder.group.detected operation=select step=start");
		return "ResponderGroup";
	}
	
}