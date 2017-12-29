package org.bytemechanics.fluentjpa;

import java.io.Closeable;
import javax.persistence.EntityManager;

/**
 * @author afarre
 */
public interface PersistenceSession extends EntityManager,Closeable{
	
	public PersistenceTransaction currentTransaction();
}
