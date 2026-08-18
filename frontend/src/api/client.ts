import axios from 'axios'
import type { ApiProblemDetail } from '../types/link'

function getApiBaseUrl() {
  const configured = import.meta.env.VITE_API_BASE_URL?.trim()
  if (configured) {
    return configured.replace(/\/+$/, '')
  }

  return '/'
}

export const apiClient = axios.create({
  baseURL: getApiBaseUrl(),
  headers: {
    Accept: 'application/json',
  },
})

export function isProblemDetail(error: unknown): error is ApiProblemDetail {
  if (!axios.isAxiosError(error)) {
    return false
  }

  const data = error.response?.data
  if (!data || typeof data !== 'object') {
    return false
  }

  return (
    typeof (data as Partial<ApiProblemDetail>).status === 'number' &&
    typeof (data as Partial<ApiProblemDetail>).title === 'string' &&
    typeof (data as Partial<ApiProblemDetail>).detail === 'string'
  )
}

export function getApiErrorMessage(error: unknown) {
  if (isProblemDetail(error)) {
    const validationMessages = error.errors
      ? Object.entries(error.errors)
          .map(([field, message]) => `${field}: ${message}`)
          .join(', ')
      : ''

    return [error.detail, validationMessages].filter(Boolean).join(' ')
  }

  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Unable to reach the API. Check that the backend is running.'
    }

    return error.message || 'Request failed.'
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return 'Request failed.'
}
