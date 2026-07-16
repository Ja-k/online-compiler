export interface SavedFile {
  id: number;
  filename: string;
  language: string;
  version: string;
  /** Omitted (null) in list responses to keep the payload light; present when fetching a single file. */
  code: string | null;
  createdAt: string;
  updatedAt: string;
}
