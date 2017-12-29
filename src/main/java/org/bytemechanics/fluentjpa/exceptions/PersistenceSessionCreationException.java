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

import java.util.Map;
import javax.persistence.PersistenceException;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public class PersistenceSessionCreationException extends PersistenceException{

	protected static final String MESSAGE="Unable to create persistence session from unit {} and with properties {}";
	
	public PersistenceSessionCreationException(final String _unit,final Map<String,String> _properties) {
		super(SimpleFormat.format(MESSAGE,_unit,_properties));
	}
	public PersistenceSessionCreationException(final String _unit,final Map<String,String> _properties,final Throwable _cause) {
		super(SimpleFormat.format(MESSAGE,_unit,_properties),_cause);
	}
}
