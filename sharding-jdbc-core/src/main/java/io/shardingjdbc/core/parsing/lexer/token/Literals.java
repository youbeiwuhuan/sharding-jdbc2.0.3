/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package io.shardingjdbc.core.parsing.lexer.token;

/**
 * Literals token. 字面量
 *
 * @author zhangliang
 */
public enum Literals implements TokenType {

	/**
	 * IDENTIFIER ：标识符(关键字+表名、列名、别名等)
	 * VARIABLE ：变量 
	 * CHARS ：字符串 
	 * HEX ：十六进制  
	 * INT ：整数 
	 * FLOAT ：浮点数
	 */
	INT, FLOAT, HEX, CHARS, IDENTIFIER, VARIABLE
}
