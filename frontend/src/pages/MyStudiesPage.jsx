import { useEffect, useState } from 'react'
import { getMyStudyGroups } from '../api/studyGroupApi.js'
import PageHeader from '../components/PageHeader.jsx'

const applicationLabels = {
  REQUESTED: '승인 대기',
  ACCEPTED: '참여 중',
  REJECTED: '거절됨',
  WITHDRAWN: '탈퇴',
}

function StudyCard({ group, badge, onStudyGroup }) {
  return (
    <article className="study-card my-study-card" role="button" tabIndex="0" onClick={() => onStudyGroup(group)} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') onStudyGroup(group) }}>
      <div className="card-top">
        <span className="category-label">{group.category}</span>
        <span className={`my-study-status ${group.applicationStatus === 'REJECTED' ? 'rejected' : ''}`}>{badge}</span>
      </div>
      <h3>{group.title}</h3>
      <p className="description">{group.description}</p>
      <div className="meta">
        <span>{group.location}</span>
        <span>{group.schedule}</span>
      </div>
    </article>
  )
}

function StudySection({ title, groups, emptyMessage, badge, onStudyGroup }) {
  return (
    <section className="my-study-section">
      <div className="section-heading">
        <h2>{title}</h2>
        <span>{groups.length}개</span>
      </div>
      {groups.length === 0 ? (
        <p className="my-study-empty">{emptyMessage}</p>
      ) : (
        <div className="study-list">
          {groups.map((group) => <StudyCard key={group.id} group={group} badge={badge(group)} onStudyGroup={onStudyGroup} />)}
        </div>
      )}
    </section>
  )
}

function MyStudiesPage({ onBack, onStudyGroup }) {
  const [groups, setGroups] = useState({ createdGroups: [], appliedGroups: [] })
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  useEffect(() => {
    getMyStudyGroups()
      .then(setGroups)
      .catch((error) => setMessage(error.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="app-shell my-studies-page">
      <PageHeader onBack={onBack} />
      <section className="my-studies-intro">
        <p className="eyebrow">MY STUDIES</p>
        <h1>내 스터디</h1>
        <p>내가 만든 그룹과 참가 신청 현황을 확인해 보세요.</p>
      </section>
      {loading && <p className="status-message">내 스터디를 불러오고 있어요.</p>}
      {message && <p className="status-message">{message}</p>}
      {!loading && !message && (
        <div className="my-studies-content">
          <StudySection title="내가 만든 스터디" groups={groups.createdGroups} emptyMessage="아직 만든 스터디가 없어요." badge={() => '그룹장'} onStudyGroup={onStudyGroup} />
          <StudySection title="신청한 스터디" groups={groups.appliedGroups} emptyMessage="아직 신청한 스터디가 없어요." badge={(group) => applicationLabels[group.applicationStatus] || '신청'} onStudyGroup={onStudyGroup} />
        </div>
      )}
    </main>
  )
}

export default MyStudiesPage
