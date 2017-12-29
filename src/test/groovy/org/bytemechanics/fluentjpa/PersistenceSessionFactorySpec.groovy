package org.bytemechanics.fluentjpa

import org.bytemechanics.fluentjpa.tests.LoggingSpecification
import org.bytemechanics.fluentjpa.mocks.*
import spock.lang.*

/**
 * @author afarre
 */
class PersistenceSessionFactorySpec extends LoggingSpecification {

	def "Instantiate new enum session factory from an known persistence unit"(){
		setup:
			def PersistenceSessionFactory sessionFactory
			
		when:
			sessionFactory=PersistenceSessionMock.WITH_PERSISTENCE_UNIT
			
		then:
			sessionFactory!=null
	}
	def "Instantiate new session factory from a persistence session factory should return a valid session"(){
		setup:
			def PersistenceSessionFactory sessionFactory=PersistenceSessionMock.WITH_PERSISTENCE_UNIT
			def PersistenceSession persistenceSession
			
		when:
			persistenceSession=sessionFactory.getSession()
			
		then:
			persistenceSession!=null
	}
}

