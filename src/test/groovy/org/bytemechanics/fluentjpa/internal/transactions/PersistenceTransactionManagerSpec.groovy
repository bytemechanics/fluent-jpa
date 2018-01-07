package org.bytemechanics.fluentjpa.internal.transactions

import org.bytemechanics.fluentjpa.*
import org.bytemechanics.fluentjpa.exceptions.*
import spock.lang.*
import java.util.logging.*
import java.io.*

/**
 * @author afarre
 */
class PersistenceTransactionManagerSpec extends Specification {

	def setupSpec(){
		println(">>>>> PersistenceTransactionManagerSpec >>>>  setupSpec")

		final InputStream inputStream = PersistenceDao.class.getResourceAsStream("/logging.properties");
		try{
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}finally{
			if(inputStream!=null)
				inputStream.close();
		}
	}
	
	def "create transaction from an empty supplier should launch an exception"(){
		println(">>>>> PersistenceTransactionManagerSpec >>>>  create transaction from an empty supplier should launch an exception")

		setup:
			def PersistenceTransactionManager transactionManager=new PersistenceTransactionManager({-> return null})
			def PersistenceTransaction transaction

		when:
			transaction=transactionManager.getTransaction()

		then:
			thrown(TransactionSupplierException)
	}
}

