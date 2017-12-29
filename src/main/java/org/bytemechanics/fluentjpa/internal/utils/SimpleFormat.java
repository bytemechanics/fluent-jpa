/*
 * Copyright 2017 afarre.
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
package org.bytemechanics.fluentjpa.internal.utils;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Helper class for <strong>internal use only</strong>
 * Please keep in mind that this <strong>class can be changed, renamed, deleted or extended without previous advice between fix releases, minor versions or major versions</strong>
 * @author Albert Farré Figueras
 * @since 0.0.1
 * @version 1.0.1
 */
public final class SimpleFormat {
	
	private SimpleFormat(){}
	
	/**
	 * Formatter that resplaces _message content '{}' by the giver _args per order.<br/> 
	 * The method to print the object is by calling object to string and in case the object is null is replaced by the string "null"
	 * @param _message Message to be replaced
	 * @param _args Objects to us by replacement
	 * @return the _message with the '{}' replaced by the given args or "null"
	 * @since 0.0.1
	 */
	public static final String format(final String _message, final Object... _args) {
		
		final StringBuilder builder=new StringBuilder();
		
		int lastBreak=0;
		int numArg=0;
		for(int ic1=0;ic1<_message.length();ic1++){
			final char current=_message.charAt(ic1);
			final char next=(ic1<_message.length()-1)? _message.charAt(ic1+1) : 'A';
			if((current=='{')&&(next=='}')){
				builder.append(_message.substring(lastBreak,ic1));
				builder.append(Optional.of(numArg++)
										.filter(counter -> counter<_args.length)
										.map(counter -> _args[counter])
										.map(String::valueOf)
										.orElse("null"));
				ic1=lastBreak=ic1+2;
			}
		}
		if(lastBreak<_message.length()){
			builder.append(_message.substring(lastBreak,_message.length()));
		}
		
		return builder.toString();
	}	

	/**
	 * Supplier that retrieve the message formatted that resplaces _message content '{}' by the giver _args per order.<br/> 
	 * The method to print the object is by calling object to string and in case the object is null is replaced by the string "null"
	 * @param _message Message to be replaced
	 * @param _args Objects to us by replacement
	 * @return a supplier that provides the _message with the '{}' replaced by the given args or "null"
	 * @since 1.1.0
	 */
	public static final Supplier<String> supplier(final String _message, final Object... _args) {
		return () -> format(_message, _args);
	}	
}
