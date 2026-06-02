import { useEffect, useState } from 'react'
import { getCurrentUser, logout } from './api/authApi.js'
import { getStudyGroups } from './api/studyGroupApi.js'
import BottomNav from './components/BottomNav.jsx'
import CreateStudyGroupPage from './pages/CreateStudyGroupPage.jsx'
import HomePage from './pages/HomePage.jsx'
import ChatsPage from './pages/ChatsPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import MyPage from './pages/MyPage.jsx'
import MyStudiesPage from './pages/MyStudiesPage.jsx'
import SignUpPage from './pages/SignUpPage.jsx'
import StudyGroupDetailPage from './pages/StudyGroupDetailPage.jsx'
import StudyGroupManagementPage from './pages/StudyGroupManagementPage.jsx'

function readRoute() {
  const path = window.location.hash.slice(1) || '/'
  const studyDetailMatch = path.match(/^\/study\/(\d+)$/)
  const studyManagementMatch = path.match(/^\/study\/(\d+)\/management$/)

  if (studyManagementMatch) return { page: 'study-management', studyGroupId: Number(studyManagementMatch[1]) }
  if (studyDetailMatch) return { page: 'study-detail', studyGroupId: Number(studyDetailMatch[1]) }
  if (path === '/login') return { page: 'login' }
  if (path === '/sign-up') return { page: 'sign-up' }
  if (path === '/my-page') return { page: 'my-page' }
  if (path === '/my-studies') return { page: 'my-studies' }
  if (path === '/chats' || path === '/inbox') return { page: 'chats' }
  if (path === '/study/new') return { page: 'create-study' }
  return { page: 'home' }
}

function routePath(page, studyGroup) {
  if (page === 'login') return '/login'
  if (page === 'sign-up') return '/sign-up'
  if (page === 'my-page') return '/my-page'
  if (page === 'my-studies') return '/my-studies'
  if (page === 'chats') return '/chats'
  if (page === 'create-study') return '/study/new'
  if (page === 'study-detail') return `/study/${studyGroup.id}`
  if (page === 'study-management') return `/study/${studyGroup.id}/management`
  return '/'
}

function App() {
  const initialRoute = readRoute()
  const [page, setPage] = useState(initialRoute.page)
  const [routeStudyGroupId, setRouteStudyGroupId] = useState(initialRoute.studyGroupId)
  const [currentUser, setCurrentUser] = useState(null)
  const [studyGroups, setStudyGroups] = useState([])
  const [selectedStudyGroup, setSelectedStudyGroup] = useState(null)
  const [loading, setLoading] = useState(true)
  const [authLoading, setAuthLoading] = useState(true)
  const [darkMode, setDarkMode] = useState(() => localStorage.getItem('stugy-theme') === 'dark')

  const withBottomNav = (content) => (
    <>
      {content}
      <BottomNav
        currentPage={page}
        currentUser={currentUser}
        onHome={() => navigate('home')}
        onExplore={() => navigate('home')}
        onMyStudies={() => navigate('my-studies')}
        onMyPage={() => navigate('my-page')}
        onLogin={() => navigate('login')}
      />
    </>
  )

  const navigate = (nextPage, studyGroup) => {
    setPage(nextPage)
    setSelectedStudyGroup(studyGroup || null)
    setRouteStudyGroupId(studyGroup?.id)
    window.location.hash = routePath(nextPage, studyGroup)
  }

  const loadStudyGroups = () => {
    setLoading(true)
    return getStudyGroups()
      .then(setStudyGroups)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    getCurrentUser()
      .then((result) => setCurrentUser(result.authenticated ? result : null))
      .finally(() => setAuthLoading(false))
  }, [])

  useEffect(() => {
    loadStudyGroups()
  }, [])

  useEffect(() => {
    document.documentElement.dataset.theme = darkMode ? 'dark' : 'light'
    localStorage.setItem('stugy-theme', darkMode ? 'dark' : 'light')
  }, [darkMode])

  useEffect(() => {
    const syncRoute = () => {
      const route = readRoute()
      setPage(route.page)
      setRouteStudyGroupId(route.studyGroupId)
      if (route.page !== 'study-detail' && route.page !== 'study-management') setSelectedStudyGroup(null)
    }
    window.addEventListener('hashchange', syncRoute)
    return () => window.removeEventListener('hashchange', syncRoute)
  }, [])

  useEffect(() => {
    if ((page !== 'study-detail' && page !== 'study-management') || !routeStudyGroupId) return
    setSelectedStudyGroup((current) => (
      studyGroups.find((studyGroup) => studyGroup.id === routeStudyGroupId)
      || (current?.id === routeStudyGroupId ? current : null)
    ))
  }, [page, routeStudyGroupId, studyGroups])

  useEffect(() => {
    if (!authLoading && !currentUser && (page === 'my-page' || page === 'my-studies' || page === 'chats' || page === 'create-study' || page === 'study-management')) {
      navigate('login')
    }
  }, [authLoading, currentUser, page])

  if (page === 'sign-up') {
    return withBottomNav(<SignUpPage onBack={() => navigate('home')} onSignUp={() => navigate('login')} />)
  }

  if (page === 'login') {
    return withBottomNav(<LoginPage onBack={() => navigate('home')} onLogin={(user) => { setCurrentUser(user); navigate('home') }} onSignUp={() => navigate('sign-up')} />)
  }

  if (page === 'my-page' && currentUser) {
    return withBottomNav(<MyPage user={currentUser} onBack={() => navigate('home')} onLogout={async () => {
      await logout()
      setCurrentUser(null)
      navigate('home')
    }} onMyStudies={() => navigate('my-studies')} />)
  }

  if (page === 'my-studies' && currentUser) {
    return withBottomNav(<MyStudiesPage onBack={() => navigate('my-page')} onStudyGroup={(studyGroup) => navigate('study-detail', studyGroup)} />)
  }

  if (page === 'chats' && currentUser) {
    return withBottomNav(<ChatsPage onBack={() => navigate('home')} />)
  }

  if (page === 'create-study' && currentUser) {
    return withBottomNav(<CreateStudyGroupPage onBack={() => navigate('home')} onCreated={async () => {
      await loadStudyGroups()
      navigate('home')
    }} />)
  }

  if (page === 'study-detail' && selectedStudyGroup) {
    return withBottomNav(<StudyGroupDetailPage studyGroup={selectedStudyGroup} onBack={() => navigate('home')} onManage={() => navigate('study-management', selectedStudyGroup)} />)
  }

  if (page === 'study-management' && currentUser && routeStudyGroupId) {
    return withBottomNav(<StudyGroupManagementPage studyGroupId={routeStudyGroupId} onBack={() => navigate('study-detail', selectedStudyGroup || { id: routeStudyGroupId })} onChat={() => navigate('chats')} />)
  }

  if ((page === 'study-detail' && loading) || ((page === 'my-page' || page === 'my-studies' || page === 'chats' || page === 'create-study' || page === 'study-management') && authLoading)) {
    return withBottomNav(<main className="app-shell"><p className="status-message">페이지를 불러오고 있어요.</p></main>)
  }

  if (page === 'study-detail' && !selectedStudyGroup) {
    return withBottomNav(<main className="app-shell"><p className="status-message">스터디를 찾을 수 없어요.</p></main>)
  }

  return withBottomNav(
    <HomePage
      currentUser={currentUser}
      loading={loading}
      studyGroups={studyGroups}
      onHome={() => navigate('home')}
      onLogin={() => navigate('login')}
      onSignUp={() => navigate('sign-up')}
      onMyPage={() => navigate('my-page')}
      darkMode={darkMode}
      onToggleTheme={() => setDarkMode((current) => !current)}
      onCreateStudyGroup={() => navigate(currentUser ? 'create-study' : 'login')}
      onStudyGroup={(studyGroup) => navigate('study-detail', studyGroup)}
      onManageStudyGroup={(studyGroup) => navigate('study-management', studyGroup)}
    />
  )
}

export default App
