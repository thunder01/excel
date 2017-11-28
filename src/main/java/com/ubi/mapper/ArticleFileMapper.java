package com.ubi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author 冯志宇
 * Created by 冯志宇 on 2017/11/26.
 */
@Mapper
public interface ArticleFileMapper {

    boolean addFile(@Param("articleid")Long articleid, @Param("fileid")String fileid,
                    @Param("filename")String filename, @Param("filepath")String filepath);
}
