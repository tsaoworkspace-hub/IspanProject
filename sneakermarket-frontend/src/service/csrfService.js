const apiBaseUrl = (
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
).replace(/\/$/, '')

let csrfTokenPromise = null

export function resetCsrfToken() {
  csrfTokenPromise = null
}

export async function getCsrfToken() {
  if (!csrfTokenPromise) {
    csrfTokenPromise = fetch(`${apiBaseUrl}/auth/csrf`, {
      credentials: 'include',
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error('無法取得安全驗證 Token')
        }
        return response.json()
      })
      .catch((error) => {
        csrfTokenPromise = null
        throw error
      })
  }

  return csrfTokenPromise
}
