package com.ubi.mapper;

import com.ubi.model.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {

    /**
     * 插入
     * @param record
     * @return
     */
    int insert(Article record);

    /**
     * 根据省的名称获取其id
     * @param provincename
     * @return
     */
    String getProvinceId(@Param("provincename")String provincename);

    /**
     * 查询事项的字典值
     * @param searchType
     * @param projectType
     * @return
     */
    String getDicvalue(@Param("searchType")String searchType,@Param("ProjrctType")String projectType);

    /**
     * 根据政策标题查询其id
     * @param title
     * @return
     */
    Long getArticleId(@Param("titile")String title);
}