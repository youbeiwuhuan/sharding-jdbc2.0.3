/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.lexer;

import io.shardingjdbc.core.parsing.lexer.analyzer.CharType;
import io.shardingjdbc.core.parsing.lexer.analyzer.Dictionary;
import io.shardingjdbc.core.parsing.lexer.analyzer.Tokenizer;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.Token;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lexical analysis.
 * 
 * @author zhangliang 
 */
@RequiredArgsConstructor
public class Lexer {
    
    /**
     * 输入字符串，即要解析的sql
     */
    @Getter
    private final String input;
    
    /**
     * 词典
     */
    private final Dictionary dictionary;
    
    /**
     * 当前解析到的位置
     */
    private int offset;
    
    /**
     * 当前 词法标记
     */
    @Getter
    private Token currentToken;
    
    /**
     * Analyse next token.
     */
    public final void nextToken() {
    	/**
    	 * 使用 #skipIgnoredToken() 方法跳过忽略的 Token，
    	 * 通过 #isXXXX() 方法判断好下一个 Token 的类型后，交给 Tokenizer 进行分词返回 Token
    	 */
        skipIgnoredToken();
        if (isVariableBegin()) {// 变量
            currentToken = new Tokenizer(input, dictionary, offset).scanVariable();
        } else if (isNCharBegin()) {// NChar类型
            currentToken = new Tokenizer(input, dictionary, ++offset).scanChars();
        } else if (isIdentifierBegin()) {// 唯一标识符   Keyword + Literals.IDENTIFIER
            currentToken = new Tokenizer(input, dictionary, offset).scanIdentifier();
        } else if (isHexDecimalBegin()) {// 十六进制
            currentToken = new Tokenizer(input, dictionary, offset).scanHexDecimal();
        } else if (isNumberBegin()) {// 数字（整数+浮点数）
            currentToken = new Tokenizer(input, dictionary, offset).scanNumber();
        } else if (isSymbolBegin()) {// 符号
            currentToken = new Tokenizer(input, dictionary, offset).scanSymbol();
        } else if (isCharsBegin()) {// 字符串，例如："abc"
            currentToken = new Tokenizer(input, dictionary, offset).scanChars();
        } else if (isEnd()) { // 结束
            currentToken = new Token(Assist.END, "", offset);
        } else {// 分析错误，无符合条件的词法标记
            throw new SQLParsingException(this, Assist.ERROR);
        }
        offset = currentToken.getEndPosition();
    }
    
    /**
     * 跳过忽略的词法标记
     * 1. 空格
     * 2. SQL Hint
     * 3. SQL 注释
     * 
     */
    private void skipIgnoredToken() {
        offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        while (isHintBegin()) {
            offset = new Tokenizer(input, dictionary, offset).skipHint();
            offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        }
        while (isCommentBegin()) {
            offset = new Tokenizer(input, dictionary, offset).skipComment();
            offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        }
    }
    
    /**
     * 
     * 
     * @return
     */
    protected boolean isHintBegin() {
        return false;
    }
    
    /**
     * 是否是注释开始
     * 
     * @return
     */
    protected boolean isCommentBegin() {
        char current = getCurrentChar(0);
        char next = getCurrentChar(1);
        return '/' == current && '/' == next || '-' == current && '-' == next || '/' == current && '*' == next;
    }
    
    /**
     * 是否是变量开始
     * 
     * @return
     */
    protected boolean isVariableBegin() {
        return false;
    }
    
    /**
     * 是否支持NChar类型
     * 
     * @return
     */
    protected boolean isSupportNChars() {
        return false;
    }
    
    /**
     * 是否是NChar类型开始
     * 
     * @return
     */
    private boolean isNCharBegin() {
        return isSupportNChars() && 'N' == getCurrentChar(0) && '\'' == getCurrentChar(1);
    }
    
    /**
     * 是否是 唯一标识符   Keyword + Literals.IDENTIFIER  开始
     * 
     * @return
     */
    private boolean isIdentifierBegin() {
        return isIdentifierBegin(getCurrentChar(0));
    }
    
    private boolean isIdentifierBegin(final char ch) {
        return CharType.isAlphabet(ch) || '`' == ch || '_' == ch || '$' == ch;
    }
    
    /**
     * 是否是 十六进制数开始
     * 
     * @return
     */
    private boolean isHexDecimalBegin() {
        return '0' == getCurrentChar(0) && 'x' == getCurrentChar(1);
    }
    
    /**
     * 数字开始
     * 
     * @return
     */
    private boolean isNumberBegin() {
        return CharType.isDigital(getCurrentChar(0)) || ('.' == getCurrentChar(0) && CharType.isDigital(getCurrentChar(1)) && !isIdentifierBegin(getCurrentChar(-1))
                || ('-' == getCurrentChar(0) && ('.' == getCurrentChar(1) || CharType.isDigital(getCurrentChar(1)))));
    }
    
    /**
     * 是否符号开始
     * 
     * @return
     */
    private boolean isSymbolBegin() {
        return CharType.isSymbol(getCurrentChar(0));
    }
    
    /**
     * 是否字符串开始
     * 
     * @return
     */
    private boolean isCharsBegin() {
        return '\'' == getCurrentChar(0) || '\"' == getCurrentChar(0);
    }
    
    /**
     * 是否到达要解析字符串末尾
     * 
     * @return
     */
    private boolean isEnd() {
        return offset >= input.length();
    }
    
    /**
     * 基于当前位置获取字符串
     * 
     * @param offset 偏移量，可为负数
     * @return
     */
    protected final char getCurrentChar(final int offset) {
        return this.offset + offset >= input.length() ? (char) CharType.EOI : input.charAt(this.offset + offset);
    }
}
