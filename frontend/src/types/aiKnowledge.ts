export interface AiKnowledgeItem {
  id: number;
  categoryId: number | null;
  productId: number | null;
  title: string;
  content: string;
  knowledgeType: string;
  tags: string | null;
  source: string | null;
  status: number;
}

export interface AiKnowledgeChunkItem {
  id: number;
  knowledgeId: number | null;
  productId: number | null;
  categoryId: number | null;
  chunkIndex: number;
  title: string;
  content: string;
  charCount: number;
  status: number;
}

export interface AiKnowledgeSearchResult {
  chunkId: number;
  knowledgeId: number | null;
  productId: number | null;
  title: string;
  content: string;
  score: number;
  retrievalSource: 'VECTOR' | 'KEYWORD' | 'HYBRID' | string;
  embeddingModel: string | null;
}
