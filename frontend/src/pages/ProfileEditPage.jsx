import { useState } from 'react'
import { updateProfile } from '../api/userApi.js'
import PageHeader from '../components/PageHeader.jsx'
import { REGIONS } from '../constants/regions.js'

function ProfileEditPage({ user, onBack, onUpdated }) {
  const [form, setForm] = useState({
    email: user.email || '',
    nickname: user.nickname || '',
    region: user.region || '',
    birthDate: user.birthDate || '',
    phoneNumber: user.phoneNumber || '',
    introduction: user.introduction || '',
  })
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
      await updateProfile(form, profileImage)
      await onUpdated()
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
        <p className="eyebrow">EDIT PROFILE</p>
        <h1>내 정보 수정</h1>
        <p>스터디 활동에 사용할 프로필 정보를 수정해 주세요.</p>
      </section>
      <form className="sign-up-form" onSubmit={submit}>
        <label><span>아이디</span><input value={user.loginId} disabled /></label>
        <label><span>이메일</span><input name="email" type="email" value={form.email} onChange={updateField} required /></label>
        <label><span>닉네임</span><input name="nickname" value={form.nickname} onChange={updateField} minLength="2" maxLength="30" required /></label>
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
              <strong>{profileImage ? profileImage.name : '새 프로필 사진 선택'}</strong>
              <small>{profileImage ? '저장하면 기존 사진이 교체됩니다.' : '사진을 바꾸지 않으려면 선택하지 않아도 됩니다.'}</small>
            </span>
          </span>
        </label>
        <label><span>소개글 <small>선택</small></span><textarea name="introduction" value={form.introduction} onChange={updateField} maxLength="500" rows="4" /></label>
        {message && <p className="form-message">{message}</p>}
        <button className="submit-button" disabled={submitting}>{submitting ? '저장 중...' : '수정 저장'}</button>
      </form>
    </main>
  )
}

export default ProfileEditPage
