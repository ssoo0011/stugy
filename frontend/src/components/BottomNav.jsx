function BottomNav({ currentPage, currentUser, onHome, onExplore, onMyStudies, onMyPage, onLogin }) {
  const selectedPage = currentPage === 'my-page' || currentPage === 'profile-edit'
    ? 'my-page'
    : currentPage === 'my-studies' || currentPage === 'study-detail' || currentPage === 'study-management'
      ? 'my-studies'
      : currentPage === 'home'
        ? 'home'
        : ''

  const openProtectedPage = (callback) => (event) => {
    event.preventDefault()
    currentUser ? callback() : onLogin()
  }

  return (
    <nav className="bottom-nav" aria-label="주요 메뉴">
      <a className={selectedPage === 'home' ? 'selected' : ''} href="#/" onClick={(event) => { event.preventDefault(); onHome() }}>홈</a>
      <a href="#/" onClick={(event) => { event.preventDefault(); onExplore() }}>커뮤니티</a>
      <a className={selectedPage === 'my-studies' ? 'selected' : ''} href="#/my-studies" onClick={openProtectedPage(onMyStudies)}>내 스터디</a>
      <a className={selectedPage === 'my-page' ? 'selected' : ''} href="#/my-page" onClick={openProtectedPage(onMyPage)}>프로필</a>
    </nav>
  )
}

export default BottomNav
