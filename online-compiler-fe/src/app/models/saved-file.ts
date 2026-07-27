export interface SavedFile {
  id: number;
  filename: string;
  language: string;
  version: string;
  /** Omitted (null) in list responses to keep the payload light; present when fetching a single file. */
  code: string | null;
  /** Null when the file lives at the root (not inside any folder). */
  folderId: number | null;
  createdAt: string;
  updatedAt: string;
}
