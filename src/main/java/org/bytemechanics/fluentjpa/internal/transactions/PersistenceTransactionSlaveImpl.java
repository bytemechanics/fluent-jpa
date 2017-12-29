package org.bytemechanics.fluentjpa.internal.transactions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytemechanics.fluentjpa.PersistenceTransaction;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public class PersistenceTransactionSlaveImpl extends PersistenceTransactionDelegateImpl {

	private final static String KIND="SLAVE";
	private final static Logger logger=Logger.getLogger(PersistenceTransactionMasterImpl.class.getSimpleName());

	private boolean started;
	private boolean commited;
	
	public PersistenceTransactionSlaveImpl(final PersistenceTransaction _underlayingEntityTransaction){
		super(_underlayingEntityTransaction);
		this.started=false;
		this.commited=false;
	}

	@Override
	protected String getKind(){
		return KIND;
	}
	
	@Override
	public void begin() {
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction[{}]::BEGIN::{}",KIND,this));
		this.started=true;
	}
	@Override
	public void commit() {
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction[{}]::COMMIT::{}",KIND,this));
		this.commited=true;
	}
	@Override
	public void rollback() {
		logger.log(Level.FINE,SimpleFormat.supplier("persistence-transaction[{}]::ROLLBACK::{}",KIND,this));
		super.setRollbackOnly();
	}

	@Override
	public void close() {
		logger.log(Level.FINER,SimpleFormat.supplier("persistence-transaction[{}]::close::{}",KIND,this));
		if((this.started)&&(!this.commited)){
			super.setRollbackOnly();
		}
	}

	@Override
	public String toString() {
		return SimpleFormat.format("PersistenceTransactionSlaveImpl[transaction={}]",this.transaction);
	}	
}
