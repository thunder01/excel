package com.ubi.mapper;

import com.ubi.model.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleTagMapper {

    int insert(ArticleTag record);

}