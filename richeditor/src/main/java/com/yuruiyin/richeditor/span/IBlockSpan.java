package com.yuruiyin.richeditor.span;

import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;

/**
 * 获取段落标签的类型
 *
 * @author admin
 */
public interface IBlockSpan {

    /**
     * 获取块级标签的类型，比如图片、自定义、段落等
     *
     * @return 块级标签类型
     */
    @BlockSpanEnum
    String getType();

}
