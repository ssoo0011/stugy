import { useState } from 'react'
import { login } from '../api/authApi.js'
import PageHeader from '../components/PageHeader.jsx'

const SAVED_LOGIN_ID_KEY = 'stugy-saved-login-id'

function LoginPage({ onBack, onLogin, onSignUp }) {
  const savedLoginId = localStorage.getItem(SAVED_LOGIN_ID_KEY) || ''
  const [loginId, setLoginId] = useState(savedLoginId)
  const [password, setPassword] = useState('')
  const [saveLoginId, setSaveLoginId] = useState(Boolean(savedLoginId))
  const [rememberMe, setRememberMe] = useState(false)
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')

    try {
      const user = await login({ loginId, password, rememberMe })
      if (saveLoginId) {
        localStorage.setItem(SAVED_LOGIN_ID_KEY, loginId)
      } else {
        localStorage.removeItem(SAVED_LOGIN_ID_KEY)
      }
      onLogin(user)
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="app-shell auth-page">
      <PageHeader onBack={onBack} />
      <section className="sign-up-intro">
        <p className="eyebrow">WELCOME BACK</p>
        <h1>다시 만나서<br />반가워요.</h1>
        <p>로그인하고 나에게 맞는 스터디를 이어서 찾아보세요.</p>
      </section>
      <form className="sign-up-form login-form" onSubmit={submit}>
        <label>
          <span>아이디</span>
          <input value={loginId} onChange={(event) => setLoginId(event.target.value)} required autoComplete="username" placeholder="아이디를 입력해 주세요" />
        </label>
        <label>
          <span>비밀번호</span>
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" placeholder="비밀번호를 입력해 주세요" />
        </label>
        <div className="login-options">
          <label className="login-checkbox">
            <input type="checkbox" checked={saveLoginId} onChange={(event) => setSaveLoginId(event.target.checked)} />
            <span>아이디 저장</span>
          </label>
          <label className="login-checkbox">
            <input type="checkbox" checked={rememberMe} onChange={(event) => setRememberMe(event.target.checked)} />
            <span>로그인 유지하기</span>
          </label>
        </div>
        {message && <p className="form-message">{message}</p>}
        <button className="submit-button" disabled={submitting}>{submitting ? '로그인 중...' : '로그인'}</button>
        <button className="text-button" type="button" onClick={onSignUp}>아직 회원이 아니신가요? 회원가입</button>
      </form>
    </main>
  )
}

export default LoginPage
