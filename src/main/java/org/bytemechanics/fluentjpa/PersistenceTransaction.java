package org.bytemechanics.fluentjpa;

import java.io.Closeable;
import javax.persistence.EntityTransaction;

/**
 * @author afarre
 */
public interface PersistenceTransaction extends EntityTransaction,Closeable{

	@Override
	public void close();
}
