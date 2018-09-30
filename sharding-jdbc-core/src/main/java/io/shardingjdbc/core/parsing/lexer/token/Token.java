package io.shardingjdbc.core.parsing.lexer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Token.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class Token {
    
    private final TokenType type;
    
    /**
     * 字面值
     */
    private final String literals;
    
    /**
     * 词在sql中结束位置
     */
    private final int endPosition;
}
