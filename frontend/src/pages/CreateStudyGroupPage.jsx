import { useState } from 'react'
import Swal from 'sweetalert2'
import { createStudyGroup } from '../api/studyGroupApi.js'
import PageHeader from '../components/PageHeader.jsx'
import { REGIONS } from '../constants/regions.js'
import { STUDY_DIFFICULTIES, STUDY_GROUP_CATEGORIES, STUDY_METHODS } from '../constants/studyGroups.js'

const initialForm = {
  title: '',
  category: '',
  studyMethod: '',
  region: '',
  place: '',
  capacity: 4,
  difficulty: '',
  meetingDays: '',
  meetingTime: '',
  duration: '',
  goal: '',
  participationRequirements: '',
  description: '',
  contactLink: '',
}

const requiredFields = {
  title: '제목을 작성해주세요.',
  category: '카테고리를 선택해주세요.',
  studyMethod: '진행 방식을 선택해주세요.',
  region: '지역을 선택해주세요.',
  capacity: '모집 인원을 작성해주세요.',
  difficulty: '난이도를 선택해주세요.',
  meetingDays: '요일을 작성해주세요.',
  meetingTime: '시간을 작성해주세요.',
  duration: '기간을 작성해주세요.',
  goal: '목표를 작성해주세요.',
  description: '소개글을 작성해주세요.',
}

function CreateStudyGroupPage({ onBack, onCreated }) {
  const [form, setForm] = useState(initialForm)
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const updateField = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: name === 'capacity' ? Number(value) : value }))
    setErrors((current) => ({ ...current, [name]: '' }))
  }

  const submit = async (event) => {
    event.preventDefault()
    const nextErrors = Object.fromEntries(
      Object.entries(requiredFields)
        .filter(([name]) => !form[name])
        .map(([name, errorMessage]) => [name, errorMessage]),
    )
    if (form.capacity < 2 || form.capacity > 100) {
      nextErrors.capacity = '모집 인원은 2명 이상 100명 이하로 작성해주세요.'
    }
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    const confirm = await Swal.fire({
      title: '스터디를 만들까요?',
      text: '작성한 내용으로 스터디 모집을 시작합니다.',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: '만들기',
      cancelButtonText: '취소',
      confirmButtonColor: '#725cff',
      cancelButtonColor: '#a5afbf',
      customClass: { popup: 'stugy-alert' },
    })
    if (!confirm.isConfirmed) return

    setSubmitting(true)
    setMessage('')
    try {
      await createStudyGroup(form)
      await Swal.fire({
        title: '스터디를 만들었어요.',
        icon: 'success',
        confirmButtonText: '확인',
        confirmButtonColor: '#725cff',
        customClass: { popup: 'stugy-alert' },
      })
      onCreated()
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="app-shell create-study-page">
      <PageHeader onBack={onBack} />

      <form className="sign-up-form study-create-form" onSubmit={submit} noValidate>
        <label><span>제목</span><input className={errors.title ? 'input-error' : ''} name="title" value={form.title} onChange={updateField} maxLength="100" placeholder="예: 대구 Spring Boot 백엔드 스터디 모집" />{errors.title && <small className="field-error">{errors.title}</small>}</label>
        <div className="form-row">
          <label>
            <span>카테고리</span>
            <select className={errors.category ? 'input-error' : ''} name="category" value={form.category} onChange={updateField}>
              <option value="">선택</option>
              {STUDY_GROUP_CATEGORIES.filter((item) => item !== '전체').map((item) => <option key={item}>{item}</option>)}
            </select>
            {errors.category && <small className="field-error">{errors.category}</small>}
          </label>
          <label>
            <span>진행 방식</span>
            <select className={errors.studyMethod ? 'input-error' : ''} name="studyMethod" value={form.studyMethod} onChange={updateField}>
              <option value="">선택</option>
              {STUDY_METHODS.map((item) => <option key={item}>{item}</option>)}
            </select>
            {errors.studyMethod && <small className="field-error">{errors.studyMethod}</small>}
          </label>
        </div>
        <div className="form-row">
          <label>
            <span>지역</span>
            <select className={errors.region ? 'input-error' : ''} name="region" value={form.region} onChange={updateField}>
              <option value="">선택</option>
              {REGIONS.map((item) => <option key={item}>{item}</option>)}
            </select>
            {errors.region && <small className="field-error">{errors.region}</small>}
          </label>
          <label><span>모집 인원</span><input className={errors.capacity ? 'input-error' : ''} name="capacity" type="number" value={form.capacity} onChange={updateField} min="2" max="100" />{errors.capacity && <small className="field-error">{errors.capacity}</small>}</label>
        </div>
        <label><span>장소</span><input name="place" value={form.place} onChange={updateField} maxLength="100" placeholder="예: 동성로 카페, 디스코드, Zoom" /></label>
        <label>
          <span>난이도</span>
          <select className={errors.difficulty ? 'input-error' : ''} name="difficulty" value={form.difficulty} onChange={updateField}>
            <option value="">선택</option>
            {STUDY_DIFFICULTIES.map((item) => <option key={item}>{item}</option>)}
          </select>
          {errors.difficulty && <small className="field-error">{errors.difficulty}</small>}
        </label>
        <div className="form-row">
          <label><span>요일</span><input className={errors.meetingDays ? 'input-error' : ''} name="meetingDays" value={form.meetingDays} onChange={updateField} maxLength="50" placeholder="예: 월 / 수 / 금" />{errors.meetingDays && <small className="field-error">{errors.meetingDays}</small>}</label>
          <label><span>시간</span><input className={errors.meetingTime ? 'input-error' : ''} name="meetingTime" value={form.meetingTime} onChange={updateField} maxLength="30" placeholder="예: 오후 8시" />{errors.meetingTime && <small className="field-error">{errors.meetingTime}</small>}</label>
        </div>
        <label><span>기간</span><input className={errors.duration ? 'input-error' : ''} name="duration" value={form.duration} onChange={updateField} maxLength="50" placeholder="예: 4주, 3개월, 상시" />{errors.duration && <small className="field-error">{errors.duration}</small>}</label>
        <label><span>목표</span><input className={errors.goal ? 'input-error' : ''} name="goal" value={form.goal} onChange={updateField} maxLength="300" placeholder="예: 토이프로젝트 완성" />{errors.goal && <small className="field-error">{errors.goal}</small>}</label>
        <label><span>참여 조건 <small>선택</small></span><textarea name="participationRequirements" value={form.participationRequirements} onChange={updateField} maxLength="500" rows="3" placeholder="예: 노쇼 금지, 주 2회 참석 가능자" /></label>
        <label><span>소개글</span><textarea className={errors.description ? 'input-error' : ''} name="description" value={form.description} onChange={updateField} rows="5" placeholder="스터디 진행 방식과 분위기를 소개해 주세요." />{errors.description && <small className="field-error">{errors.description}</small>}</label>
        <label><span>오픈채팅 / 디스코드 링크 <small>선택</small></span><input name="contactLink" value={form.contactLink} onChange={updateField} maxLength="500" placeholder="승인된 멤버에게만 공개됩니다." /></label>

        {message && <p className="form-message">{message}</p>}
        <button className="submit-button" disabled={submitting}>{submitting ? '등록 중...' : '스터디 만들기'}</button>
      </form>
    </main>
  )
}

export default CreateStudyGroupPage
