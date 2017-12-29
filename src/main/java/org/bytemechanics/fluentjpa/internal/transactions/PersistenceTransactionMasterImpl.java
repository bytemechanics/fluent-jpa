package org.bytemechanics.fluentjpa.internal.transactions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityTransaction;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public class PersistenceTransactionMasterImpl extends PersistenceTransactionDelegateImpl {
	
	private final static String KIND="MASTER";
	private final static Logger logger=Logger.getLogger(PersistenceTransactionMasterImpl.class.getSimpleName());

	protected final PersistenceTransactionManager transactionManager;
	private boolean started;
	private boolean finalized;

	public PersistenceTransactionMasterImpl(final EntityTransaction _underlayingEntityTransaction,final PersistenceTransactionManager _transactionManager){
		super(_underlayingEntityTransaction);
		this.transactionManager=_transactionManager;
		this.started=false;
		this.finalized=false;
	}

	@Override
	protected String getKind(){
		return KIND;
	}
	
	@Override
	public void begin() {		
		super.begin();
		this.started=true;
	}
	@Override
	public void commit() {
		try{
			super.commit();
		}catch(Throwable e){
			logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-transaction[{}]::commit::{}::commit::failed",KIND,this));
		}
		try{
			this.transactionManager.removeTransaction();
		}catch(Throwable e){
			logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-transaction[{}]::commit::{}::remove::failed",KIND,this));
		}
		this.finalized=true;
	}
	@Override
	public void rollback() {
		try{
			super.rollback();
		}catch(Throwable e){
			logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-transaction[{}]::rollback::{}::rollback::failed",KIND,this));
		}
		try{
			this.transactionManager.removeTransaction();
		}catch(Throwable e){
			logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-transaction[{}]::rollback::{}::remove::failed",KIND,this));
		}
		this.finalized=true;
	}

	@Override
	public void close() {
		logger.log(Level.FINER,SimpleFormat.supplier("persistence-transaction[{}]::close::{}",KIND,this));
		if(!this.finalized){
			if(this.started){
				logger.log(Level.INFO,SimpleFormat.supplier("persistence-transaction[{}]::close::{}::auto-closing-by-rollback",KIND,this));
				try{
					super.rollback();
				}catch(Throwable e){
					logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-transaction[{}]::close::{}::rollback::failed",KIND,this));
				}
			}
			try{
				this.transactionManager.removeTransaction();
			}catch(Throwable e){
				logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-transaction[{}]::close::{}::remove::failed",KIND,this));
			}
		}
	}
	
	@Override
	public String toString() {
		return SimpleFormat.format("PersistenceTransactionMasterImpl[transaction={}]",this.transaction);
	}	
}
