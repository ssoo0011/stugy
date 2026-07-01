export async function request(url, options) {
  const response = await fetch(url, options)
  const text = response.status === 204 ? '' : await response.text()
  let result = null

  if (text) {
    try {
      result = JSON.parse(text)
    } catch {
      result = { message: text }
    }
  }

  if (!response.ok) {
    throw new Error(result?.message || '요청을 처리하지 못했습니다.')
  }

  return result
}
