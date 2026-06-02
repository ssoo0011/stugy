export async function request(url, options) {
  const response = await fetch(url, options)
  const result = response.status === 204 ? null : await response.json()

  if (!response.ok) {
    throw new Error(result?.message || '요청을 처리하지 못했습니다.')
  }

  return result
}
