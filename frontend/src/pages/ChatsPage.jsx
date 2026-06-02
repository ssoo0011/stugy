import PageHeader from '../components/PageHeader.jsx'

function ChatsPage({ onBack }) {
  return (
    <main className="app-shell chats-page">
      <PageHeader onBack={onBack} />
      <section className="chats-intro">
        <p className="eyebrow">CHATS</p>
        <h1>채팅</h1>
        <p>스터디 멤버와 나눈 대화를 확인할 수 있어요.</p>
      </section>
      <section className="chats-empty">
        <div className="chats-empty-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24">
            <path d="M4 5.5h16v12H8l-4 3v-15Z" />
            <path d="M8 10h8M8 13.5h5" />
          </svg>
        </div>
        <h2>아직 진행 중인 채팅이 없어요.</h2>
        <p>새로운 대화가 시작되면 이곳에서 확인할 수 있어요.</p>
      </section>
    </main>
  )
}

export default ChatsPage
