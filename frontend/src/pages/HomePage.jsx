import { useEffect, useMemo, useRef, useState } from 'react'
import logo from '../assets/images/logo2.png'
import darkLogo from '../assets/images/logo-dark.png'
import noProfileImage from '../assets/images/no-profile.png'
import { getStudyGroupNotifications, readStudyGroupNotifications } from '../api/studyGroupApi.js'
import { STUDY_GROUP_CATEGORIES } from '../constants/studyGroups.js'

function HeaderIcon({ badgeCount = 0, children, label, tone = 'default', onClick }) {
  return (
    <button className={`header-icon-button ${tone}`} type="button" aria-label={label} onClick={onClick}>
      {children}
      {badgeCount > 0 && <span className="header-notification-badge">{badgeCount > 99 ? '99+' : badgeCount}</span>}
    </button>
  )
}

function formatNotificationTime(value) {
  const minutes = Math.floor((Date.now() - new Date(value).getTime()) / 60000)
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}시간 전`
  return `${Math.floor(minutes / 1440)}일 전`
}

function HomePage({ currentUser, loading, studyGroups, darkMode, onToggleTheme, onHome, onLogin, onSignUp, onMyPage, onCreateStudyGroup, onStudyGroup, onManageStudyGroup }) {
  const [category, setCategory] = useState('전체')
  const [query, setQuery] = useState('')
  const [notificationCount, setNotificationCount] = useState(0)
  const [notifications, setNotifications] = useState([])
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [notificationTab, setNotificationTab] = useState('notifications')
  const notificationRef = useRef(null)

  useEffect(() => {
    if (!currentUser) {
      setNotificationCount(0)
      setNotifications([])
      return
    }
    getStudyGroupNotifications()
      .then((result) => {
        setNotificationCount(result.pendingApplicationCount)
        setNotifications(result.notifications)
      })
      .catch(() => {
        setNotificationCount(0)
        setNotifications([])
      })
  }, [currentUser])

  useEffect(() => {
    const closeNotifications = (event) => {
      if (event.key === 'Escape' || !notificationRef.current?.contains(event.target)) setNotificationsOpen(false)
    }
    document.addEventListener('pointerdown', closeNotifications)
    document.addEventListener('keydown', closeNotifications)
    return () => {
      document.removeEventListener('pointerdown', closeNotifications)
      document.removeEventListener('keydown', closeNotifications)
    }
  }, [])

  const filteredGroups = useMemo(() => {
    const keyword = query.trim().toLowerCase()
    return studyGroups.filter((group) => {
      const matchesCategory = category === '전체' || group.category === category
      const matchesKeyword = !keyword || `${group.title} ${group.tags.join(' ')}`.toLowerCase().includes(keyword)
      return matchesCategory && matchesKeyword
    })
  }, [category, query, studyGroups])

  const openNotificationGroup = (studyGroupId) => {
    const studyGroup = studyGroups.find((group) => group.id === studyGroupId)
    onManageStudyGroup(studyGroup || { id: studyGroupId })
  }

  const toggleNotifications = () => {
    setNotificationsOpen((open) => {
      const nextOpen = !open
      if (nextOpen && notificationCount > 0) {
        setNotificationCount(0)
        readStudyGroupNotifications().catch(() => {
          getStudyGroupNotifications().then((result) => setNotificationCount(result.pendingApplicationCount))
        })
      }
      return nextOpen
    })
  }

  return (
    <main className="app-shell">
      <header className="top-bar">
        <button className="logo-frame" type="button" onClick={onHome} aria-label="홈으로 이동">
          <img className="logo" src={darkMode ? darkLogo : logo} alt="stuGy" />
        </button>
        <div className="header-actions">
          {currentUser && <div className="header-utilities" aria-label="빠른 메뉴">
            <HeaderIcon label={darkMode ? '라이트 모드로 전환' : '다크 모드로 전환'} tone={darkMode ? 'moon' : 'sun'} onClick={onToggleTheme}>
              {darkMode ? (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M20.2 15.3A8.3 8.3 0 0 1 8.7 3.8 8.6 8.6 0 1 0 20.2 15.3Z" />
                </svg>
              ) : (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <circle cx="12" cy="12" r="3.5" />
                  <path d="M12 2.5v2M12 19.5v2M4.2 4.2l1.4 1.4M18.4 18.4l1.4 1.4M2.5 12h2M19.5 12h2M4.2 19.8l1.4-1.4M18.4 5.6l1.4-1.4" />
                </svg>
              )}
            </HeaderIcon>
            <div className="header-notification" ref={notificationRef}>
              <HeaderIcon label={`알림 ${notificationCount}개`} badgeCount={notificationCount} onClick={toggleNotifications}>
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M14 21h-4" />
                </svg>
              </HeaderIcon>
              {notificationsOpen && (
                <section className="notification-panel" aria-label="알림 목록">
                  <header className="notification-panel-header">
                    <div>
                      <p className="eyebrow">NOTIFICATIONS</p>
                      <h2>알림센터</h2>
                    </div>
                    <span>{notificationCount}건</span>
                  </header>
                  <div className="notification-tabs">
                    <button className={notificationTab === 'notifications' ? 'active' : ''} type="button" onClick={() => setNotificationTab('notifications')}>알림</button>
                    <button className={notificationTab === 'chats' ? 'active' : ''} type="button" onClick={() => setNotificationTab('chats')}>대화</button>
                  </div>
                  <div className="notification-list">
                    {notificationTab === 'chats' ? (
                      <div className="notification-empty">
                        <strong>아직 진행 중인 대화가 없어요.</strong>
                        <span>새로운 채팅이 시작되면 이곳에서 확인할 수 있어요.</span>
                      </div>
                    ) : notifications.length === 0 ? (
                      <p className="notification-empty">새로운 알림이 없어요.</p>
                    ) : notifications.map((notification) => (
                      <button className="notification-item" type="button" key={notification.applicationId} onClick={() => openNotificationGroup(notification.studyGroupId)}>
                        <span className="notification-avatar">{notification.applicantNickname.slice(0, 1)}</span>
                        <span className="notification-copy">
                          <strong>{notification.applicantNickname}<small>{formatNotificationTime(notification.requestedAt)}</small></strong>
                          <span>{notification.studyGroupTitle}</span>
                          <p>스터디 참가를 신청했어요. 신청 내용을 확인해 주세요.</p>
                        </span>
                      </button>
                    ))}
                  </div>
                </section>
              )}
            </div>
          </div>}
          {currentUser ? (
            <button className="header-profile-button" type="button" aria-label="내 프로필" onClick={onMyPage}>
              <img src={currentUser.profileThumbnailUrl || noProfileImage} alt="" />
            </button>
          ) : (
          <div className="auth-actions">
            <button onClick={onLogin}>로그인</button>
            <button onClick={onSignUp}>회원가입</button>
          </div>
          )}
        </div>
      </header>

      <section className="hero">
        <p className="hero-kicker">오늘의 작은 루틴</p>
        <h2>혼자 미루던 공부,<br />같이 시작해 볼까요?</h2>
        <p>관심사와 일정이 맞는 스터디를 찾아보세요.</p>
      </section>

      <section className="search-panel" aria-label="스터디 검색">
        <label className="search-box">
          <span aria-hidden="true">⌕</span>
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="어떤 공부를 찾고 있나요?" />
        </label>
        <div className="categories">
          {STUDY_GROUP_CATEGORIES.map((item) => (
            <button className={category === item ? 'category active' : 'category'} key={item} onClick={() => setCategory(item)}>
              {item}
            </button>
          ))}
        </div>
      </section>

      <section className="content">
        <div className="section-heading">
          <div>
            <p className="eyebrow">RECOMMENDED</p>
            <h2>지금 모집 중인 스터디</h2>
          </div>
          <span>{filteredGroups.length}개</span>
        </div>

        {loading && <p className="status-message">스터디를 찾고 있어요.</p>}
        {!loading && filteredGroups.length === 0 && <p className="status-message">조건에 맞는 스터디가 없어요.</p>}

        <div className="study-list">
          {filteredGroups.map((group) => (
            <article className="study-card" key={group.id} role="button" tabIndex="0" onClick={() => onStudyGroup(group)} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') onStudyGroup(group) }}>
              <div className="card-top">
                <span className="category-label">{group.category}</span>
                <span className="member-count">{group.memberCount}/{group.capacity}명</span>
              </div>
              <h3>{group.title}</h3>
              <p className="description">{group.description}</p>
              <div className="meta">
                <span>{group.location}</span>
                <span>{group.schedule}</span>
              </div>
              <div className="tag-list">
                {group.tags.map((tag) => <span key={tag}>#{tag}</span>)}
              </div>
            </article>
          ))}
        </div>
      </section>

      <button className="floating-button" onClick={onCreateStudyGroup}>+ 스터디 만들기</button>

    </main>
  )
}

export default HomePage
