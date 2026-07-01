import PageHeader from '../components/PageHeader.jsx'
import noProfileImage from '../assets/images/no-profile.png'

function MyPage({ user, onBack, onEditProfile, onMyStudies, onLogout }) {
  return (
    <main className="app-shell my-page">
      <PageHeader onBack={onBack} />
      <section className="my-page-banner">
        <p className="eyebrow">MY PAGE</p>
        <h1>Profile</h1>
      </section>
      <section className="profile-card">
        <img className="profile-avatar" src={user.profileThumbnailUrl || noProfileImage} alt={`${user.nickname} 프로필`} />
        <h2>{user.nickname}</h2>
        <p>@{user.loginId}</p>
        <button className="profile-edit-link" type="button" onClick={onEditProfile}>내 정보 수정</button>
      </section>
      <section className="my-menu">
        <button type="button" onClick={onMyStudies}><span className="menu-icon">▣</span><span>내 스터디</span><span className="menu-arrow">›</span></button>
        <button type="button"><span className="menu-icon">●</span><span>계정 설정</span><span className="menu-arrow">›</span></button>
        <button type="button"><span className="menu-icon">◆</span><span>서비스 안내</span><span className="menu-arrow">›</span></button>
        <button className="logout-button" type="button" onClick={onLogout}><span className="menu-icon">↪</span><span>로그아웃</span><span className="menu-arrow">›</span></button>
      </section>
    </main>
  )
}

export default MyPage
