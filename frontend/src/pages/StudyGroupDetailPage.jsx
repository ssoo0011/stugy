import { useEffect, useRef, useState } from 'react'
import Swal from 'sweetalert2'
import { applyToStudyGroup } from '../api/studyGroupApi.js'
import PageHeader from '../components/PageHeader.jsx'

function DetailRow({ label, children }) {
  if (!children) return null
  return (
    <div className="detail-row">
      <span>{label}</span>
      <strong>{children}</strong>
    </div>
  )
}

function StudyGroupDetailPage({ studyGroup, onBack, onManage }) {
  const [ownerMenuOpen, setOwnerMenuOpen] = useState(false)
  const [applicationStatus, setApplicationStatus] = useState(studyGroup.applicationStatus)
  const [submitting, setSubmitting] = useState(false)
  const ownerMenuRef = useRef(null)
  const isFull = studyGroup.memberCount >= studyGroup.capacity
  const applicationPending = applicationStatus === 'REQUESTED'
  const participating = applicationStatus === 'ACCEPTED'

  useEffect(() => {
    const closeOwnerMenu = (event) => {
      if (!ownerMenuRef.current?.contains(event.target)) setOwnerMenuOpen(false)
    }
    document.addEventListener('pointerdown', closeOwnerMenu)
    return () => document.removeEventListener('pointerdown', closeOwnerMenu)
  }, [])

  const apply = async () => {
    setSubmitting(true)
    try {
      const result = await applyToStudyGroup(studyGroup.id)
      setApplicationStatus(result.status)
      await Swal.fire({
        title: '참가 신청을 보냈어요.',
        text: '그룹 정책에 따라 그룹장이 따로 채팅을 보낼 수 있습니다. 채팅을 잘 확인해 주세요.',
        icon: 'success',
        confirmButtonText: '확인',
        confirmButtonColor: '#725cff',
        customClass: { popup: 'stugy-alert' },
      })
    } catch (error) {
      await Swal.fire({
        title: '신청하지 못했어요.',
        text: error.message,
        icon: 'error',
        confirmButtonText: '확인',
        confirmButtonColor: '#725cff',
        customClass: { popup: 'stugy-alert' },
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="app-shell study-detail-page">
      <PageHeader onBack={onBack} />

      <article className="study-detail-content">
        <header className="study-detail-heading">
          <div className="study-detail-tags">
            <span>{studyGroup.category}</span>
            <span>{studyGroup.studyMethod}</span>
            <span>{studyGroup.recruitmentStatus}</span>
          </div>
          <div className="study-detail-title-row">
            <h1>{studyGroup.title}</h1>
            {studyGroup.ownedByCurrentUser || participating ? (
              <button className="study-owner-button" type="button" onClick={onManage}>그룹 확인하기</button>
            ) : (
              <button className="study-apply-button" type="button" disabled={isFull || applicationPending || participating || submitting} onClick={apply}>
                {isFull ? '모집 마감' : applicationPending ? '신청 완료' : participating ? '참여 중' : submitting ? '신청 중...' : '참가 신청하기'}
              </button>
            )}
          </div>
          <p>{studyGroup.description}</p>
        </header>

        <div className="study-detail-owner">
          <div className="owner-avatar">{studyGroup.ownerNickname.slice(0, 1)}</div>
          <div className="owner-profile">
            <div className="owner-profile-top">
              <strong>{studyGroup.ownerNickname}</strong>
              <span>스터디장</span>
            </div>
            <p>{studyGroup.category} · {studyGroup.studyMethod}</p>
          </div>
          <div className="owner-menu" ref={ownerMenuRef}>
            <button className="owner-menu-trigger" type="button" onClick={() => setOwnerMenuOpen((open) => !open)} aria-label="스터디장 메뉴" aria-expanded={ownerMenuOpen}>•••</button>
            {ownerMenuOpen && (
              <div className="owner-menu-dropdown">
                <button type="button">프로필 보기</button>
                <button type="button">1:1 채팅 시작하기</button>
              </div>
            )}
          </div>
        </div>

        <section className="study-summary-grid">
          <div>
            <span>일정</span>
            <strong>{studyGroup.schedule}</strong>
          </div>
          <div>
            <span>모집 인원</span>
            <strong>{studyGroup.memberCount}/{studyGroup.capacity}명</strong>
          </div>
          <div>
            <span>지역 및 장소</span>
            <strong>{studyGroup.location}{studyGroup.place ? ` · ${studyGroup.place}` : ''}</strong>
          </div>
        </section>

        <section className="study-info-list">
          <DetailRow label="카테고리">{studyGroup.category}</DetailRow>
          <DetailRow label="진행 방식">{studyGroup.studyMethod}</DetailRow>
          <DetailRow label="난이도">{studyGroup.difficulty}</DetailRow>
          <DetailRow label="기간">{studyGroup.duration}</DetailRow>
          <DetailRow label="지역">{studyGroup.location}</DetailRow>
          <DetailRow label="장소">{studyGroup.place}</DetailRow>
        </section>

        <section className="study-detail-text">
          <h2>목표</h2>
          <p>{studyGroup.goal}</p>
          {studyGroup.participationRequirements && (
            <>
              <h2>참여 조건</h2>
              <p>{studyGroup.participationRequirements}</p>
            </>
          )}
        </section>
      </article>
    </main>
  )
}

export default StudyGroupDetailPage
