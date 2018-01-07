package org.bytemechanics.fluentjpa

import org.bytemechanics.fluentjpa.mocks.*
import spock.lang.*
import java.util.logging.*
import java.io.*

/**
 * @author afarre
 */
class PersistenceSessionFactorySpec extends Specification {

	def setupSpec(){
		println(">>>>> PersistenceSessionFactorySpec >>>>  setupSpec")

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

	def "Instantiate new enum session factory from an known persistence unit"(){
		println(">>>>> PersistenceSessionFactorySpec >>>>  Instantiate new enum session factory from an known persistence unit")

		setup:
			def PersistenceSessionFactory sessionFactory
			
		when:
			sessionFactory=PersistenceSessionMock.WITH_PERSISTENCE_UNIT
			
		then:
			sessionFactory!=null
	}
	def "Instantiate new session factory from a persistence session factory should return a valid session"(){
		println(">>>>> PersistenceSessionFactorySpec >>>>  Instantiate new session factory from a persistence session factory should return a valid session")

		setup:
			def PersistenceSessionFactory sessionFactory=PersistenceSessionMock.WITH_PERSISTENCE_UNIT
			def PersistenceSession persistenceSession
			
		when:
			persistenceSession=sessionFactory.getSession()
			
		then:
			persistenceSession!=null
	}
}

