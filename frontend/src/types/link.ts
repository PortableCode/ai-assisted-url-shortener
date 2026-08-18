export interface CreateLinkRequest {
  originalUrl: string
  expiresAt?: string
}

export interface CreateLinkResponse {
  shortCode: string
  shortUrl: string
  originalUrl: string
  createdAt: string
  expiresAt: string | null
}

export interface LinkMetadataResponse {
  shortCode: string
  originalUrl: string
  createdAt: string
  expiresAt: string | null
}

export interface LinkAnalyticsResponse {
  shortCode: string
  clickCount: number
  lastAccessedAt: string | null
}

export interface ApiProblemDetail {
  status: number
  title: string
  detail: string
  instance: string
  errors?: Record<string, string>
}
