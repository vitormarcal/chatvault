/* Optional offline enhancements. No requests, external dependencies or message HTML. */
(() => {
  'use strict';
  const root = document.body.dataset.root || '';
  const chatId = document.body.dataset.chatId;
  const chats = Array.isArray(globalThis.CHATVAULT_CHATS) ? globalThis.CHATVAULT_CHATS : [];
  const current = chats.find(chat => String(chat.id) === chatId);
  const list = document.querySelector('.conversation-list');
  if (list && chats.length) {
    list.replaceChildren();
    chats.forEach(chat => {
      const link = document.createElement('a');
      link.className = 'conversation-link';
      link.href = root + chat.html;
      link.dataset.name = chat.name.toLocaleLowerCase();
      if (String(chat.id) === chatId) { link.classList.add('active'); link.setAttribute('aria-current', 'page'); }
      const avatar = document.createElement(chat.profileFile ? 'img' : 'span');
      avatar.className = 'avatar';
      if (chat.profileFile) { avatar.src = root + chat.profileFile; avatar.alt = ''; avatar.loading = 'lazy'; }
      else { avatar.classList.add('avatar-placeholder'); avatar.textContent = [...chat.name][0]?.toLocaleUpperCase() || '?'; }
      const text = document.createElement('span');
      text.className = 'conversation-label';
      const name = document.createElement('strong');
      name.textContent = chat.name;
      const meta = document.createElement('small');
      meta.textContent = `${chat.messageCount.toLocaleString('pt-BR')} mensagens`;
      text.append(name, meta);
      link.append(avatar, text);
      list.append(link);
    });
    const search = document.querySelector('.chat-search');
    search.hidden = false;
    search.querySelector('input').addEventListener('input', event => {
      const query = event.target.value.trim().toLocaleLowerCase();
      list.querySelectorAll('.conversation-link').forEach(link => { link.hidden = !link.dataset.name.includes(query); });
    });
  }

  const toggle = document.querySelector('.sidebar-toggle');
  toggle.hidden = false;
  toggle.addEventListener('click', () => {
    const open = document.body.classList.toggle('sidebar-open');
    toggle.setAttribute('aria-expanded', String(open));
    if (open) document.querySelector('.chat-search input')?.focus();
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') { document.body.classList.remove('sidebar-open'); toggle.setAttribute('aria-expanded', 'false'); }
  });

  const control = document.querySelector('.author-control');
  if (control && current?.participants?.length) {
    const select = control.querySelector('select');
    current.participants.forEach(author => {
      const option = document.createElement('option');
      option.value = author;
      option.textContent = author;
      select.append(option);
    });
    control.hidden = false;
    const key = `chatvault.archive.author.${chatId}`;
    try { select.value = localStorage.getItem(key) || ''; } catch { /* Storage can be restricted on file URLs. */ }
    const align = () => {
      document.querySelectorAll('article.message').forEach(message => {
        message.classList.toggle('outgoing', !!select.value && message.dataset.author === select.value);
      });
      try { localStorage.setItem(key, select.value); } catch { /* Alignment still works for this page. */ }
    };
    select.addEventListener('change', align);
    align();
  }

  // Native dialog supplies Escape handling and keyboard focus containment.
  if (typeof HTMLDialogElement === 'undefined') return;
  const dialog = document.createElement('dialog');
  dialog.className = 'media-viewer';
  dialog.setAttribute('aria-label', 'Pré-visualização do anexo');
  const toolbar = document.createElement('div');
  toolbar.className = 'viewer-toolbar';
  const title = document.createElement('strong');
  const download = document.createElement('a');
  download.className = 'button';
  download.textContent = '↓ Baixar';
  const close = document.createElement('button');
  close.type = 'button';
  close.textContent = 'Fechar ✕';
  close.addEventListener('click', () => dialog.close());
  toolbar.append(title, download, close);
  const viewport = document.createElement('div');
  viewport.className = 'viewer-viewport';
  const navigation = document.createElement('div');
  navigation.className = 'viewer-navigation';
  const previous = document.createElement('button');
  previous.type = 'button'; previous.textContent = '← Anterior';
  const position = document.createElement('span');
  const next = document.createElement('button');
  next.type = 'button'; next.textContent = 'Próximo →';
  navigation.append(previous, position, next);
  dialog.append(toolbar, viewport, navigation);
  document.body.append(dialog);
  const previews = [...document.querySelectorAll('a[data-preview]')];
  let index = 0;
  let opener;
  function show(indexToShow) {
    viewport.querySelector('video')?.pause();
    index = indexToShow;
    const link = previews[index];
    title.textContent = link.dataset.name;
    download.href = link.href;
    download.download = link.dataset.name;
    const media = document.createElement(link.dataset.preview === 'videos' ? 'video' : 'img');
    media.src = link.href;
    if (media.tagName === 'VIDEO') { media.controls = true; media.preload = 'metadata'; }
    else media.alt = link.dataset.name;
    viewport.replaceChildren(media);
    position.textContent = `${index + 1} de ${previews.length} nesta página`;
    previous.disabled = index === 0;
    next.disabled = index === previews.length - 1;
  }
  previews.forEach((link, previewIndex) => link.addEventListener('click', event => {
    // Preserve open-in-new-tab, save-link and other native browser actions.
    if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey || event.button !== 0) return;
    event.preventDefault();
    opener = link;
    show(previewIndex);
    dialog.showModal();
  }));
  previous.addEventListener('click', () => { if (index > 0) show(index - 1); });
  next.addEventListener('click', () => { if (index < previews.length - 1) show(index + 1); });
  dialog.addEventListener('keydown', event => {
    if (event.key === 'ArrowLeft' && index > 0) { event.preventDefault(); show(index - 1); }
    if (event.key === 'ArrowRight' && index < previews.length - 1) { event.preventDefault(); show(index + 1); }
  });
  dialog.addEventListener('close', () => { viewport.querySelector('video')?.pause(); viewport.replaceChildren(); opener?.focus(); });
  dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); });
})();
