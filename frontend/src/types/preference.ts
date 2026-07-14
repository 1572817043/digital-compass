export interface UserPreferenceItem {
  id: number | null;
  minBudget: number | null;
  maxBudget: number | null;
  categoryId: number | null;
  brandIds: string | null;
  usageScenes: string | null;
  priorityTags: string | null;
  avoidTags: string | null;
  remark: string | null;
}
