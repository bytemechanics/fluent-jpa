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

/**
 * @author afarre
 */
public class TransactionSupplierException extends PersistenceException{

	protected static final String MESSAGE="Unable to create transaction from persistence session, transaction supplier returned null";
	
	public TransactionSupplierException() {
		super(MESSAGE);
	}
	public TransactionSupplierException(final Throwable _cause) {
		super(MESSAGE,_cause);
	}
}
