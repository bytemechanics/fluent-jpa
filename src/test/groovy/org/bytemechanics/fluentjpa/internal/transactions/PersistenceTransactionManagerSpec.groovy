package org.bytemechanics.fluentjpa.internal.transactions

import org.bytemechanics.fluentjpa.tests.LoggingSpecification
import org.bytemechanics.fluentjpa.*
import org.bytemechanics.fluentjpa.exceptions.*
import spock.lang.*

/**
 * @author afarre
 */
class PersistenceTransactionManagerSpec extends LoggingSpecification {
	
	def "create transaction from an empty supplier should launch an exception"(){
		setup:
			def PersistenceTransactionManager transactionManager=new PersistenceTransactionManager({-> return null})
			def PersistenceTransaction transaction

		when:
			transaction=transactionManager.getTransaction()

		then:
			thrown(TransactionSupplierException)
	}
}

