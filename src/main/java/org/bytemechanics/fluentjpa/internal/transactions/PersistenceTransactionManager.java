package org.bytemechanics.fluentjpa.internal.transactions;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityTransaction;
import org.bytemechanics.fluentjpa.PersistenceTransaction;
import org.bytemechanics.fluentjpa.exceptions.TransactionSupplierException;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public class PersistenceTransactionManager{

	private final static Logger logger=Logger.getLogger(PersistenceTransactionManager.class.getSimpleName());
	
	private PersistenceTransaction underlayingTransaction;
	private final Supplier<EntityTransaction> transactionSupplier;
	
	public PersistenceTransactionManager(final Supplier<EntityTransaction> _supplier){
		this.underlayingTransaction=null;
		this.transactionSupplier=_supplier;
	}

	private Optional<PersistenceTransaction> get(){
		return Optional.ofNullable(this.underlayingTransaction);
	}
	private PersistenceTransaction init(){
		this.underlayingTransaction=Optional.ofNullable(this.transactionSupplier.get())
											.map(persistenceTransaction -> new PersistenceTransactionMasterImpl(persistenceTransaction,this))
											.orElseThrow(() -> new TransactionSupplierException());
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction-manager::register::transaction::{}",this.underlayingTransaction));
		return this.underlayingTransaction;
	}
	

	public PersistenceTransaction getTransaction() {
		return get()
			.map(persistenceTransaction -> new PersistenceTransactionSlaveImpl(persistenceTransaction))
			.map(persistenceTransaction -> (PersistenceTransaction)persistenceTransaction)
			.orElseGet(this::init);
	}
	public void removeTransaction() {
		logger.log(Level.FINEST,SimpleFormat.supplier("persistence-transaction-manager::unregister::transaction::{}",this.underlayingTransaction));
		this.underlayingTransaction=null;
	}
	public boolean hasTransaction(){
		return this.underlayingTransaction!=null;
	}
	public PersistenceTransaction currentTransaction() {
		return this.underlayingTransaction;
	}
}
