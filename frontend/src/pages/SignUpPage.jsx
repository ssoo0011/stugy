import { useState } from 'react'
import { signUp } from '../api/userApi.js'
import PageHeader from '../components/PageHeader.jsx'
import { REGIONS } from '../constants/regions.js'

const initialForm = {
  loginId: '', email: '', password: '', nickname: '', region: '', birthDate: '', phoneNumber: '', introduction: '',
}

function SignUpPage({ onBack, onSignUp }) {
  const [form, setForm] = useState(initialForm)
  const [profileImage, setProfileImage] = useState(null)
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const updateField = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')
    try {
      await signUp(form, profileImage)
      onSignUp()
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="app-shell sign-up-page">
      <PageHeader onBack={onBack} />
      <section className="sign-up-intro">
        <p className="eyebrow">JOIN STUGY</p>
        <h1>함께 공부할 준비,<br />가볍게 시작해요.</h1>
        <p>나와 잘 맞는 스터디를 찾기 위한 기본 정보를 입력해 주세요.</p>
      </section>
      <form className="sign-up-form" onSubmit={submit}>
        <label><span>아이디</span><input name="loginId" value={form.loginId} onChange={updateField} minLength="4" maxLength="30" required placeholder="영문, 숫자, 밑줄 사용 가능" /></label>
        <label><span>이메일</span><input name="email" type="email" value={form.email} onChange={updateField} required placeholder="study@example.com" /></label>
        <label><span>비밀번호</span><input name="password" type="password" value={form.password} onChange={updateField} minLength="8" required placeholder="8자 이상 입력해 주세요" /></label>
        <label><span>닉네임</span><input name="nickname" value={form.nickname} onChange={updateField} minLength="2" maxLength="30" required placeholder="스터디에서 사용할 이름" /></label>
        <label>
          <span>지역</span>
          <select name="region" value={form.region} onChange={updateField} required>
            <option value="">지역을 선택해 주세요</option>
            {REGIONS.map((region) => <option key={region}>{region}</option>)}
          </select>
        </label>
        <label><span>생년월일</span><input name="birthDate" type="date" value={form.birthDate} onChange={updateField} required /></label>
        <label><span>휴대폰 번호</span><input name="phoneNumber" type="tel" value={form.phoneNumber} onChange={updateField} required placeholder="010-1234-5678" /></label>
        <label>
          <span>프로필 이미지 <small>선택</small></span>
          <span className="file-upload">
            <input className="file-input" type="file" accept="image/*" onChange={(event) => setProfileImage(event.target.files[0] || null)} />
            <span className="file-upload-icon">+</span>
            <span className="file-upload-text">
              <strong>{profileImage ? profileImage.name : '프로필 사진 추가'}</strong>
              <small>{profileImage ? '다른 사진을 선택하려면 눌러 주세요.' : 'JPG, PNG, GIF 파일을 선택해 주세요.'}</small>
            </span>
          </span>
        </label>
        <label><span>소개글 <small>선택</small></span><textarea name="introduction" value={form.introduction} onChange={updateField} maxLength="500" rows="4" placeholder="관심 있는 공부나 목표를 간단히 소개해 주세요." /></label>
        {message && <p className="form-message">{message}</p>}
        <button className="submit-button" disabled={submitting}>{submitting ? '가입 중...' : '회원가입'}</button>
      </form>
    </main>
  )
}

export default SignUpPage
