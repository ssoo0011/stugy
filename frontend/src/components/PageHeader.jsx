import logo from '../assets/images/logo2.png'
import darkLogo from '../assets/images/logo-dark.png'

function PageHeader({ onBack }) {
  return (
    <header className="sign-up-header">
      <button className="back-button" type="button" onClick={onBack} aria-label="홈으로 돌아가기">‹</button>
      <a className="sign-up-logo-frame" href="#/" aria-label="홈으로 이동">
        <img className="logo theme-logo-light" src={logo} alt="stuGy" />
        <img className="logo theme-logo-dark" src={darkLogo} alt="stuGy" />
      </a>
      <span />
    </header>
  )
}

export default PageHeader
