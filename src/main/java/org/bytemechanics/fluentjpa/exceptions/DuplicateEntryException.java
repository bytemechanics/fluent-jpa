/*
 * Copyright 2017 Byte Mechanics.
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
package org.bytemechanics.fluentjpa.exceptions;

import javax.persistence.PersistenceException;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public class DuplicateEntryException extends PersistenceException{

	protected static final String MESSAGE="Unable to create {}, the id {} already exist";
	
	public DuplicateEntryException(final String _entityName,final Object _identifier) {
		super(SimpleFormat.format(MESSAGE,_entityName,_identifier));
	}
	public DuplicateEntryException(final String _entityName,final Object _identifier,final Throwable _cause) {
		super(SimpleFormat.format(MESSAGE,_entityName,_identifier),_cause);
	}
}
