USE digital_compass;

ALTER TABLE dc_assistant_recommendation
  ADD COLUMN explain_summary VARCHAR(240) NULL COMMENT '推荐解释摘要' AFTER risk_tip,
  ADD COLUMN matched_requirements JSON NULL COMMENT '匹配到的用户需求' AFTER explain_summary,
  ADD COLUMN tradeoff_notes JSON NULL COMMENT '推荐权衡与风险点' AFTER matched_requirements,
  ADD COLUMN knowledge_evidence JSON NULL COMMENT '命中的知识依据' AFTER tradeoff_notes;
