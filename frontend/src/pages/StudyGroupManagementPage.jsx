import { useEffect, useMemo, useState } from 'react'
import { acceptStudyGroupApplication, createStudyGroupSchedule, deleteStudyGroupSchedule, getStudyGroupManagement, rejectStudyGroupApplication, updateStudyGroupSchedule } from '../api/studyGroupApi.js'
import PageHeader from '../components/PageHeader.jsx'

const weekdays = ['일', '월', '화', '수', '목', '금', '토']
const hourOptions = Array.from({ length: 12 }, (_, index) => String(index + 1))
const minuteOptions = ['00', '10', '20', '30', '40', '50']

function parseTime(value) {
  if (!value) return { period: '', hour: '', minute: '' }
  const [rawHour, minute] = value.split(':')
  const hour = Number(rawHour)
  return {
    period: hour < 12 ? 'AM' : 'PM',
    hour: String(hour % 12 || 12),
    minute,
  }
}

function TimePicker({ value, onChangeValue }) {
  const [parts, setParts] = useState(() => parseTime(value))

  useEffect(() => {
    setParts(parseTime(value))
  }, [value])

  const updatePart = (name, nextValue) => {
    const nextParts = { ...parts, [name]: nextValue }
    setParts(nextParts)
    if (!nextParts.period || !nextParts.hour || !nextParts.minute) {
      onChangeValue('')
      return
    }
    const hour = Number(nextParts.hour) % 12 + (nextParts.period === 'PM' ? 12 : 0)
    onChangeValue(`${String(hour).padStart(2, '0')}:${nextParts.minute}`)
  }

  return (
    <div className="schedule-time-picker" aria-label="일정 시간">
      <select value={parts.period} onChange={(event) => updatePart('period', event.target.value)} required aria-label="오전 오후">
        <option value="">오전/오후</option>
        <option value="AM">오전</option>
        <option value="PM">오후</option>
      </select>
      <select value={parts.hour} onChange={(event) => updatePart('hour', event.target.value)} required aria-label="시">
        <option value="">시</option>
        {hourOptions.map((hour) => <option key={hour} value={hour}>{hour}시</option>)}
      </select>
      <select value={parts.minute} onChange={(event) => updatePart('minute', event.target.value)} required aria-label="분">
        <option value="">분</option>
        {minuteOptions.map((minute) => <option key={minute} value={minute}>{minute}분</option>)}
      </select>
    </div>
  )
}

function monthStart(date) {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

function toDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function todayKey() {
  return toDateKey(new Date())
}

function StudyGroupManagementPage({ studyGroupId, onBack, onChat }) {
  const [management, setManagement] = useState(null)
  const [month, setMonth] = useState(() => monthStart(new Date()))
  const [form, setForm] = useState({ scheduleDate: todayKey(), scheduleTime: '', title: '', content: '' })
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [processingApplicationId, setProcessingApplicationId] = useState(null)
  const [selectedSchedule, setSelectedSchedule] = useState(null)
  const [scheduleEditForm, setScheduleEditForm] = useState({ scheduleDate: '', scheduleTime: '', title: '', content: '' })

  useEffect(() => {
    getStudyGroupManagement(studyGroupId)
      .then(setManagement)
      .catch((error) => setMessage(error.message))
  }, [studyGroupId])

  const calendarDays = useMemo(() => {
    const days = []
    const firstWeekday = month.getDay()
    const lastDay = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate()
    for (let index = 0; index < firstWeekday; index += 1) days.push(null)
    for (let day = 1; day <= lastDay; day += 1) days.push(new Date(month.getFullYear(), month.getMonth(), day))
    while (days.length < 42) days.push(null)
    return days
  }, [month])

  const schedulesByDate = useMemo(() => {
    const result = {}
    management?.schedules.forEach((schedule) => {
      const dateKey = schedule.scheduledAt.slice(0, 10)
      result[dateKey] = [...(result[dateKey] || []), schedule]
    })
    return result
  }, [management])

  const updateField = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }))

  const addSchedule = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')
    try {
      const schedule = await createStudyGroupSchedule(studyGroupId, {
        scheduledAt: `${form.scheduleDate}T${form.scheduleTime}`,
        title: form.title,
        content: form.content,
      })
      setManagement((current) => ({
        ...current,
        schedules: [...current.schedules, schedule].sort((left, right) => left.scheduledAt.localeCompare(right.scheduledAt)),
      }))
      setMonth(monthStart(new Date(schedule.scheduledAt)))
      setForm({ scheduleDate: todayKey(), scheduleTime: '', title: '', content: '' })
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  const processApplication = async (applicationId, action) => {
    setProcessingApplicationId(applicationId)
    setMessage('')
    try {
      if (action === 'accept') {
        await acceptStudyGroupApplication(studyGroupId, applicationId)
      } else {
        await rejectStudyGroupApplication(studyGroupId, applicationId)
      }
      setManagement(await getStudyGroupManagement(studyGroupId))
    } catch (error) {
      setMessage(error.message)
    } finally {
      setProcessingApplicationId(null)
    }
  }

  const openSchedule = (schedule) => {
    setSelectedSchedule(schedule)
    setScheduleEditForm({
      scheduleDate: schedule.scheduledAt.slice(0, 10),
      scheduleTime: schedule.scheduledAt.slice(11, 16),
      title: schedule.title,
      content: schedule.content,
    })
  }

  const closeSchedule = () => setSelectedSchedule(null)

  const updateSchedule = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')
    try {
      const schedule = await updateStudyGroupSchedule(studyGroupId, selectedSchedule.id, {
        scheduledAt: `${scheduleEditForm.scheduleDate}T${scheduleEditForm.scheduleTime}`,
        title: scheduleEditForm.title,
        content: scheduleEditForm.content,
      })
      setManagement((current) => ({
        ...current,
        schedules: current.schedules
          .map((item) => item.id === schedule.id ? schedule : item)
          .sort((left, right) => left.scheduledAt.localeCompare(right.scheduledAt)),
      }))
      closeSchedule()
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  const deleteSchedule = async () => {
    setSubmitting(true)
    setMessage('')
    try {
      await deleteStudyGroupSchedule(studyGroupId, selectedSchedule.id)
      setManagement((current) => ({
        ...current,
        schedules: current.schedules.filter((schedule) => schedule.id !== selectedSchedule.id),
      }))
      closeSchedule()
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="app-shell study-management-page">
      <PageHeader onBack={onBack} />
      <section className="management-intro">
        <p className="eyebrow">GROUP MANAGEMENT</p>
        <h1>{management?.title || '그룹 관리'}</h1>
        <p>그룹원과 앞으로의 일정을 한곳에서 관리해 보세요.</p>
      </section>

      {message && <p className="status-message">{message}</p>}
      {!management && !message && <p className="status-message">그룹 정보를 불러오고 있어요.</p>}

      {management && (
        <div className="management-content">
          <section className="management-card">
            <div className="management-section-heading">
              <h2>그룹원</h2>
              <span>{management.memberCount}/{management.capacity}명</span>
            </div>
            <div className="member-list">
              {management.members.map((member) => (
                <div className="member-item" key={member.userId}>
                  <div className="member-avatar">{member.nickname.slice(0, 1)}</div>
                  <span className="member-name">
                    <strong>{member.nickname}</strong>
                    {member.owner && (
                      <span className="owner-crown" aria-label="그룹장">
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                          <path d="m4 8 4.3 3.2L12 5l3.7 6.2L20 8l-1.5 9h-13L4 8Z" />
                          <path d="M6 20h12" />
                        </svg>
                      </span>
                    )}
                  </span>
                </div>
              ))}
            </div>
          </section>

          {management.ownerView && (
            <>
              <section className="management-card">
                <div className="management-section-heading">
                  <h2>그룹 신청자</h2>
                  <span>{management.applicants.length}명</span>
                </div>
                {management.applicants.length === 0 ? (
                  <p className="applicant-empty">아직 새로운 참가 신청이 없어요.</p>
                ) : (
                  <div className="applicant-list">
                    {management.applicants.map((applicant) => (
                      <div className="applicant-item" key={applicant.applicationId}>
                        <div className="member-avatar">{applicant.nickname.slice(0, 1)}</div>
                        <strong>{applicant.nickname}</strong>
                        <div className="applicant-actions">
                          <button className="applicant-chat-button" type="button" onClick={onChat}>채팅</button>
                          <button className="applicant-accept-button" type="button" disabled={processingApplicationId === applicant.applicationId} onClick={() => processApplication(applicant.applicationId, 'accept')}>수락</button>
                          <button className="applicant-reject-button" type="button" disabled={processingApplicationId === applicant.applicationId} onClick={() => processApplication(applicant.applicationId, 'reject')}>거절</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </section>

              <section className="management-card">
                <div className="management-section-heading">
                  <h2>일정 추가</h2>
                </div>
                <form className="schedule-form" onSubmit={addSchedule}>
                  <div className="schedule-date-time-row">
                    <label className="schedule-picker-field">
                      <span>날짜</span>
                      <input name="scheduleDate" type="date" value={form.scheduleDate} onChange={updateField} required aria-label="일정 날짜" />
                    </label>
                    <label className="schedule-picker-field">
                      <span>시간</span>
                      <TimePicker value={form.scheduleTime} onChangeValue={(scheduleTime) => setForm((current) => ({ ...current, scheduleTime }))} />
                    </label>
                  </div>
                  <input name="title" value={form.title} onChange={updateField} maxLength="100" required placeholder="일정 제목" />
                  <textarea name="content" value={form.content} onChange={updateField} maxLength="1000" rows="3" required placeholder="일정 내용을 입력해 주세요." />
                  <button className="submit-button" disabled={submitting}>{submitting ? '추가 중...' : '일정 추가'}</button>
                </form>
              </section>
            </>
          )}

          <section className="management-card calendar-card">
            <div className="calendar-heading">
              <button type="button" onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1))} aria-label="이전 달">‹</button>
              <h2>{month.getFullYear()}년 {month.getMonth() + 1}월</h2>
              <button type="button" onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1))} aria-label="다음 달">›</button>
            </div>
            <div className="calendar-grid calendar-weekdays">
              {weekdays.map((weekday) => <span key={weekday}>{weekday}</span>)}
            </div>
            <div className="calendar-grid">
              {calendarDays.map((date, index) => (
                <div className={`calendar-day${date ? '' : ' empty'}`} key={date ? toDateKey(date) : `empty-${index}`}>
                  {date && (
                    <>
                      <strong>{date.getDate()}</strong>
                      {(schedulesByDate[toDateKey(date)] || []).map((schedule) => (
                        <button className="calendar-schedule" type="button" key={schedule.id} title={schedule.content} onClick={() => openSchedule(schedule)}>
                          <time>{schedule.scheduledAt.slice(11, 16)}</time>
                          <span>{schedule.title}</span>
                        </button>
                      ))}
                    </>
                  )}
                </div>
              ))}
            </div>
          </section>
        </div>
      )}

      {selectedSchedule && (
        <div className="schedule-modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) closeSchedule() }}>
          <section className="schedule-modal" role="dialog" aria-modal="true" aria-labelledby="schedule-modal-title">
            <div className="schedule-modal-heading">
              <h2 id="schedule-modal-title">일정 상세</h2>
              <button type="button" onClick={closeSchedule} aria-label="닫기">×</button>
            </div>
            {management.ownerView ? (
              <form className="schedule-form" onSubmit={updateSchedule}>
                <div className="schedule-date-time-row">
                  <label className="schedule-picker-field">
                    <span>날짜</span>
                    <input name="scheduleDate" type="date" value={scheduleEditForm.scheduleDate} onChange={(event) => setScheduleEditForm((current) => ({ ...current, scheduleDate: event.target.value }))} required aria-label="일정 날짜" />
                  </label>
                  <label className="schedule-picker-field">
                    <span>시간</span>
                    <TimePicker value={scheduleEditForm.scheduleTime} onChangeValue={(scheduleTime) => setScheduleEditForm((current) => ({ ...current, scheduleTime }))} />
                  </label>
                </div>
                <input name="title" value={scheduleEditForm.title} onChange={(event) => setScheduleEditForm((current) => ({ ...current, title: event.target.value }))} maxLength="100" required />
                <textarea name="content" value={scheduleEditForm.content} onChange={(event) => setScheduleEditForm((current) => ({ ...current, content: event.target.value }))} maxLength="1000" rows="4" required />
                <div className="schedule-modal-actions">
                  <button className="schedule-delete-button" type="button" disabled={submitting} onClick={deleteSchedule}>삭제</button>
                  <button className="schedule-save-button" disabled={submitting}>{submitting ? '저장 중...' : '수정 저장'}</button>
                </div>
              </form>
            ) : (
              <div className="schedule-modal-readonly">
                <time>{selectedSchedule.scheduledAt.replace('T', ' ').slice(0, 16)}</time>
                <h3>{selectedSchedule.title}</h3>
                <p>{selectedSchedule.content}</p>
              </div>
            )}
          </section>
        </div>
      )}
    </main>
  )
}

export default StudyGroupManagementPage
