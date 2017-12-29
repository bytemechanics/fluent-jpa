package org.bytemechanics.fluentjpa.internal.transactions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityTransaction;
import org.bytemechanics.fluentjpa.PersistenceTransaction;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public abstract class PersistenceTransactionDelegateImpl implements PersistenceTransaction {

	private final static Logger logger=Logger.getLogger(PersistenceTransactionDelegateImpl.class.getSimpleName());
	
	protected final EntityTransaction transaction;
	
	@SuppressWarnings({"OverridableMethodCallInConstructor", "LeakingThisInConstructor"})
	public PersistenceTransactionDelegateImpl(final EntityTransaction _underlayingEntityTransaction){
		this.transaction=_underlayingEntityTransaction;
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction(delegated)[{}]::create::{}",getKind(),this));
	}

	
	protected abstract String getKind();
	
	@Override
	public void begin() {
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction(delegated)[{}]::BEGIN::{}",getKind(),this));
		this.transaction.begin();
	}
	@Override
	public void commit() {
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction(delegated)[{}]::COMMIT::{}",getKind(),this));
		if(isActive()){
			this.transaction.commit();
		}
	}
	@Override
	public void rollback() {
		logger.log(Level.FINE,SimpleFormat.supplier("persistence-transaction(delegated)[{}]::ROLLBACK::{}",getKind(),this));
		if(isActive()){
			this.transaction.rollback();
		}
	}
	@Override
	public void setRollbackOnly() {
		logger.log(Level.FINE,SimpleFormat.supplier("persistence-transaction(delegated)[{}]::ROLLBACK-ONLY::{}",getKind(),this));
		transaction.setRollbackOnly();
	}
	@Override
	public boolean getRollbackOnly() {
		return transaction.getRollbackOnly();
	}
	@Override
	public boolean isActive() {
		return transaction.isActive();
	}	
	@Override
	public void close() {
		logger.log(Level.FINER,SimpleFormat.supplier("persistence-transaction(delegated)[{}]::close::{}",this.transaction));
	}
}
