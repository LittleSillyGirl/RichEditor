package com.yuruiyin.richeditor.span;

import com.yuruiyin.richeditor.enumtype.InlineSpanEnum;

/**
 * 获取行内标签的类型
 *
 * @author admin
 */
public interface IInlineSpan {

    /**
     * 获取行内标签的类型，比如加粗、斜体、下划线等
     *
     * @return 行内标签类型
     */
    @InlineSpanEnum
    String getType();

}
