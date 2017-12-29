package org.bytemechanics.fluentjpa.internal.session;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityTransaction;
import org.bytemechanics.fluentjpa.PersistenceSession;
import org.bytemechanics.fluentjpa.PersistenceTransaction;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 *
 * @author afarre
 */
public class PersistenceSessionSlaveImpl extends PersistenceSessionDelegateImpl{
	
	private final static String KIND="SLAVE";
	private final static Logger logger=Logger.getLogger(PersistenceSessionSlaveImpl.class.getSimpleName());

	protected boolean open;
	
	
	public PersistenceSessionSlaveImpl(final PersistenceSession _session){
		super(_session);
		this.open=true;
	}


	@Override
	protected String getKind(){
		return KIND;
	}
	
	@Override
	public void close() {
		logger.log(Level.FINEST, SimpleFormat.supplier("persistence-session[{}]::close::{}",KIND,this));
		this.open=false;
	}

	@Override
	public boolean isOpen() {
		return this.open;
	}

	@Override
	public EntityTransaction getTransaction() {
		return this.session.getTransaction();
	}

	@Override
	public PersistenceTransaction currentTransaction() {
		return ((PersistenceSession)this.session).currentTransaction();
	}

	@Override
	public String toString() {
		return SimpleFormat.format("PersistenceSessionSlaveImpl[session={}]",this.session);
	}	
}
