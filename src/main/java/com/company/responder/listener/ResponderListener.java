package com.company.responder.listener;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;

import com.company.responder.context.CONTEXT;

public class ResponderListener extends QBeanSupport implements ISORequestListener {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean process(ISOSource source, ISOMsg mRequest) {
		
		log.info(" - - - - -  process ResponderListener");
		
		Space sp = SpaceFactory.getSpace();
		Context ctx = new Context();

		// Profiler
		ctx.getLogEvent().addMessage(ctx.getProfiler());
		ctx.checkPoint("ResponderListener");

		ctx.put(CONTEXT.ISOMSG, mRequest, true);
		ctx.put(CONTEXT.ISOSOURCE, source, false);
		
		// TransactionManager
		sp.out("responderTransactionManager", ctx, 30000);

		return true;

	}
}

