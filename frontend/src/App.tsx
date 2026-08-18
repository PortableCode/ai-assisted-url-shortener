import { useState } from 'react'
import type { FormEvent } from 'react'
import { createLink, deleteLink, getAnalytics, getLink } from './api/links'
import { getApiErrorMessage } from './api/client'
import type {
  CreateLinkResponse,
  LinkAnalyticsResponse,
  LinkMetadataResponse,
} from './types/link'
import './App.css'

function App() {
  const [originalUrl, setOriginalUrl] = useState('')
  const [expiresAt, setExpiresAt] = useState('')
  const [createLoading, setCreateLoading] = useState(false)
  const [createResult, setCreateResult] = useState<CreateLinkResponse | null>(null)
  const [createMessage, setCreateMessage] = useState<string | null>(null)
  const [createError, setCreateError] = useState<string | null>(null)
  const [copyMessage, setCopyMessage] = useState<string | null>(null)

  const [shortCode, setShortCode] = useState('')
  const [loadedShortCode, setLoadedShortCode] = useState('')
  const [metadata, setMetadata] = useState<LinkMetadataResponse | null>(null)
  const [metadataLoading, setMetadataLoading] = useState(false)
  const [metadataError, setMetadataError] = useState<string | null>(null)

  const [analytics, setAnalytics] = useState<LinkAnalyticsResponse | null>(null)
  const [analyticsLoading, setAnalyticsLoading] = useState(false)
  const [analyticsError, setAnalyticsError] = useState<string | null>(null)

  const [deleteLoading, setDeleteLoading] = useState(false)
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const currentlyLoadedShortCode = loadedShortCode

  async function handleCreateSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setCreateError(null)
    setCreateMessage(null)
    setCopyMessage(null)

    const trimmedOriginalUrl = originalUrl.trim()
    if (!trimmedOriginalUrl) {
      setCreateError('Original URL is required.')
      return
    }

    const normalizedExpiresAt = normalizeDatetimeLocal(expiresAt)
    if (expiresAt && !normalizedExpiresAt) {
      setCreateError('Expiration must be a valid date and time.')
      return
    }

    setCreateLoading(true)
    try {
      const response = await createLink({
        originalUrl: trimmedOriginalUrl,
        expiresAt: normalizedExpiresAt ?? undefined,
      })
      setCreateResult(response)
      setCreateMessage('Short link created successfully.')
    } catch (error) {
      setCreateError(getApiErrorMessage(error))
    } finally {
      setCreateLoading(false)
    }
  }

  async function handleLookupMetadata() {
    const trimmedShortCode = shortCode.trim()
    setMetadataError(null)
    setDeleteMessage(null)
    setDeleteError(null)

    if (!trimmedShortCode) {
      setMetadataError('Short code is required.')
      return
    }

    const switchingCodes = loadedShortCode !== '' && loadedShortCode !== trimmedShortCode

    if (switchingCodes) {
      setAnalytics(null)
      setMetadata(null)
      setLoadedShortCode('')
    }

    setMetadataLoading(true)
    try {
      const response = await getLink(trimmedShortCode)
      setMetadata(response)
      setLoadedShortCode(trimmedShortCode)
    } catch (error) {
      setMetadata(null)
      setAnalytics(null)
      setMetadataError(getApiErrorMessage(error))
    } finally {
      setMetadataLoading(false)
    }
  }

  async function handleLookupAnalytics() {
    const trimmedShortCode = shortCode.trim()
    setAnalyticsError(null)
    setDeleteMessage(null)
    setDeleteError(null)

    if (!trimmedShortCode) {
      setAnalyticsError('Short code is required.')
      return
    }

    const switchingCodes = loadedShortCode !== '' && loadedShortCode !== trimmedShortCode

    if (switchingCodes) {
      setMetadata(null)
      setAnalytics(null)
      setLoadedShortCode('')
    }

    setAnalyticsLoading(true)
    try {
      const response = await getAnalytics(trimmedShortCode)
      setAnalytics(response)
      setLoadedShortCode(trimmedShortCode)
    } catch (error) {
      setAnalytics(null)
      setAnalyticsError(getApiErrorMessage(error))
    } finally {
      setAnalyticsLoading(false)
    }
  }

  async function handleDeleteCurrentLink() {
    if (!currentlyLoadedShortCode) {
      setDeleteError('Load a link first to delete it.')
      return
    }

    const confirmed = window.confirm(`Delete short code ${currentlyLoadedShortCode}?`)
    if (!confirmed) {
      return
    }

    setDeleteError(null)
    setDeleteMessage(null)
    setMetadataError(null)
    setAnalyticsError(null)
    setDeleteLoading(true)
    try {
      await deleteLink(currentlyLoadedShortCode)
      setMetadata(null)
      setAnalytics(null)
      setLoadedShortCode('')
      setDeleteMessage(`Deleted ${currentlyLoadedShortCode}.`)
    } catch (error) {
      setDeleteError(getApiErrorMessage(error))
    } finally {
      setDeleteLoading(false)
    }
  }

  async function handleCopyShortUrl() {
    if (!createResult) {
      return
    }

    try {
      await navigator.clipboard.writeText(createResult.shortUrl)
      setCopyMessage('Copied to clipboard.')
    } catch {
      setCopyMessage('Copy failed. You can copy the short URL manually.')
    }
  }

  return (
    <main className="app-shell">
      <header className="hero">
        <p className="eyebrow">URL Shortener</p>
        <h1>Evaluator-facing demo</h1>
        <p className="lead">
          Create short links, inspect metadata, review analytics, and delete the current link.
        </p>
      </header>

      <div className="layout">
        <section className="card">
          <div className="card__header">
            <div>
              <p className="card__eyebrow">Create</p>
              <h2>Shorten a URL</h2>
            </div>
          </div>

          <form className="stack" onSubmit={handleCreateSubmit}>
            <div className="field">
              <label htmlFor="originalUrl">Original URL</label>
              <input
                id="originalUrl"
                name="originalUrl"
                type="text"
                value={originalUrl}
                onChange={(event) => setOriginalUrl(event.target.value)}
                placeholder="https://example.com/article"
                autoComplete="url"
                spellCheck={false}
                required
              />
            </div>

            <div className="field">
              <label htmlFor="expiresAt">Expiration (optional)</label>
              <input
                id="expiresAt"
                name="expiresAt"
                type="datetime-local"
                value={expiresAt}
                onChange={(event) => setExpiresAt(event.target.value)}
              />
            </div>

            <button className="primary-button" type="submit" disabled={createLoading}>
              {createLoading ? 'Shortening…' : 'Shorten'}
            </button>
          </form>

          {createError ? <Alert tone="error" message={createError} /> : null}
          {createMessage ? <Alert tone="success" message={createMessage} /> : null}

          {createResult ? (
            <div className="result-panel" aria-live="polite">
              <div className="result-row">
                <div>
                  <span className="result-label">Short URL</span>
                  <a className="result-link" href={createResult.shortUrl}>
                    {createResult.shortUrl}
                  </a>
                </div>
                <button className="secondary-button" type="button" onClick={handleCopyShortUrl}>
                  Copy
                </button>
              </div>
              {copyMessage ? <p className="muted">{copyMessage}</p> : null}
              <dl className="result-grid">
                <ResultField label="Created at" value={formatTimestamp(createResult.createdAt)} />
                <ResultField
                  label="Expires at"
                  value={formatNullableTimestamp(createResult.expiresAt)}
                />
              </dl>
            </div>
          ) : null}
        </section>

        <section className="card">
          <div className="card__header">
            <div>
              <p className="card__eyebrow">Inspect</p>
              <h2>Lookup metadata and analytics</h2>
            </div>
          </div>

          <div className="stack">
            <div className="field">
              <label htmlFor="shortCode">Short code</label>
              <input
                id="shortCode"
                name="shortCode"
                type="text"
                value={shortCode}
                onChange={(event) => setShortCode(event.target.value)}
                placeholder="ABC1234"
                maxLength={7}
                autoComplete="off"
              />
            </div>

            <div className="button-row">
              <button
                className="primary-button"
                type="button"
                onClick={handleLookupMetadata}
                disabled={metadataLoading}
              >
                {metadataLoading ? 'Loading…' : 'Load metadata'}
              </button>
              <button
                className="secondary-button"
                type="button"
                onClick={handleLookupAnalytics}
                disabled={analyticsLoading}
              >
                {analyticsLoading ? 'Loading…' : 'Load analytics'}
              </button>
            </div>
          </div>

          {metadataError ? <Alert tone="error" message={metadataError} /> : null}
          {analyticsError ? <Alert tone="error" message={analyticsError} /> : null}

          <div className="subgrid">
            <div className="result-panel">
              <h3>Metadata</h3>
              {metadata ? (
                <dl className="result-grid">
                  <ResultField label="Original URL" value={metadata.originalUrl} />
                  <ResultField label="Created at" value={formatTimestamp(metadata.createdAt)} />
                  <ResultField
                    label="Expires at"
                    value={formatNullableTimestamp(metadata.expiresAt)}
                  />
                </dl>
              ) : (
                <p className="muted">No metadata loaded yet.</p>
              )}
            </div>

            <div className="result-panel">
              <h3>Analytics</h3>
              {analytics ? (
                <dl className="result-grid">
                  <ResultField label="Click count" value={String(analytics.clickCount)} />
                  <ResultField
                    label="Last accessed at"
                    value={formatNullableTimestamp(analytics.lastAccessedAt)}
                  />
                </dl>
              ) : (
                <p className="muted">No analytics loaded yet.</p>
              )}
            </div>
          </div>

          <div className="delete-panel">
            <div>
              <p className="card__eyebrow">Delete</p>
              <h3>Remove the loaded link</h3>
              <p className="muted">
                {currentlyLoadedShortCode
                  ? `Current link: ${currentlyLoadedShortCode}`
                  : 'Load a link before deleting it.'}
              </p>
            </div>
            <button
              className="danger-button"
              type="button"
              onClick={handleDeleteCurrentLink}
              disabled={deleteLoading || !currentlyLoadedShortCode}
            >
              {deleteLoading ? 'Deleting…' : 'Delete loaded link'}
            </button>
          </div>

          {deleteError ? <Alert tone="error" message={deleteError} /> : null}
          {deleteMessage ? <Alert tone="success" message={deleteMessage} /> : null}
        </section>
      </div>
    </main>
  )
}

type AlertProps = {
  tone: 'error' | 'success'
  message: string
}

function Alert({ tone, message }: AlertProps) {
  return <p className={`alert alert--${tone}`}>{message}</p>
}

type ResultFieldProps = {
  label: string
  value: string
}

function ResultField({ label, value }: ResultFieldProps) {
  return (
    <>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </>
  )
}

function formatTimestamp(value: string) {
  return new Date(value).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function formatNullableTimestamp(value: string | null) {
  return value ? formatTimestamp(value) : '—'
}

function normalizeDatetimeLocal(value: string) {
  if (!value) {
    return null
  }

  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? null : parsed.toISOString()
}

export default App
