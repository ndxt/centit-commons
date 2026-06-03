package com.centit.search.service;

import com.centit.search.document.ESDocument;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-6-12.
 */
@SuppressWarnings("unused")
public interface Indexer {
    /**
     * 新建 文档
     * @param document ESDocument
     * @return 返回文档的 文档ID
     */
    String saveNewDocument(ESDocument document);

    /**
     * 批量新建文档
     * @param documents 文档列表
     * @return 返回成功写入的文档数量
     */
    int saveNewDocuments(List<? extends ESDocument> documents);

    /**
     * @param  document ESDocument
     * @return 根据文档ID 删除文档
     */
    boolean deleteDocument(ESDocument document);

    /**
     * 根据文档ID 删除文档
     * @param docId String
     * @return 是否成功
     */
    boolean deleteDocument(String docId);

    /**
     * 更新文档
     * @param document ESDocument
     * @return 是否成功
     */
    int updateDocument(ESDocument document);

    /**
     * 更新文档
     * @param docId 主键
     * @param document ESDocument
     * @return 是否成功
     */
    int updateDocument(String docId,ESDocument document);

    /**
     * 合并文档
     * @param document ESDocument
     * @return  返回文档ID， 如果失败返回null
     */
    String mergeDocument(ESDocument document);

}
