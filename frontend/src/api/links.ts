import { apiClient } from './client'
import type {
  CreateLinkRequest,
  CreateLinkResponse,
  LinkAnalyticsResponse,
  LinkMetadataResponse,
} from '../types/link'

const LINKS_PATH = '/api/v1/links'

export async function createLink(payload: CreateLinkRequest) {
  const response = await apiClient.post<CreateLinkResponse>(LINKS_PATH, payload)
  return response.data
}

export async function getLink(shortCode: string) {
  const response = await apiClient.get<LinkMetadataResponse>(`${LINKS_PATH}/${shortCode}`)
  return response.data
}

export async function getAnalytics(shortCode: string) {
  const response = await apiClient.get<LinkAnalyticsResponse>(`${LINKS_PATH}/${shortCode}/analytics`)
  return response.data
}

export async function deleteLink(shortCode: string) {
  await apiClient.delete(`${LINKS_PATH}/${shortCode}`)
}
