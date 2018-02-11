/*
 * Copyright 2018 Byte Mechanics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bytemechanics.fluentjpa.example;

import org.bytemechanics.fluentjpa.PersistenceDao;
import org.bytemechanics.fluentjpa.PersistenceSessionFactory;
import org.bytemechanics.fluentjpa.example.entities.SimpleEntity;

/**
 *
 * @author afarre
 */
public class SimpleCrudDao implements PersistenceDao{
	
	private final PersistenceSessionFactory persistenceSessionFactory;
	
	public SimpleCrudDao(final PersistenceSessionFactory _persistenceSessionFactory){
		this.persistenceSessionFactory=_persistenceSessionFactory;
	}

	@Override
	public PersistenceSessionFactory getSessionFactory() {
		return this.persistenceSessionFactory;
	}
	
	public SimpleEntity create(SimpleEntity _simpleEntity){
		return save(SimpleEntity.class, _simpleEntity);
	}
	public SimpleEntity get(String _simpleEntityId){
		return get(SimpleEntity.class, _simpleEntityId);
	}
	public boolean exist(String _simpleEntityId){
		return contains(SimpleEntity.class, _simpleEntityId);
	}
	public SimpleEntity update(SimpleEntity _simpleEntity){
		return update(SimpleEntity.class, _simpleEntity);
	}
	public void delete(String _simpleEntityId){
		delete(SimpleEntity.class, _simpleEntityId);
	}
	public void fullTest(SimpleEntity _simpleEntity){
		transactional(SimpleEntity.class,
				(sessionManager -> save(SimpleEntity.class, _simpleEntity))
		);
	}
}
